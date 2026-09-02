package com.prepforge.service;

import com.prepforge.dto.QuestionResultDto;
import com.prepforge.dto.TestResultDto;
import com.prepforge.dto.TestSubmissionRequest;
import com.prepforge.entity.Question;
import com.prepforge.entity.TestAttempt;
import com.prepforge.entity.TestSession;
import com.prepforge.exception.AppException;
import com.prepforge.exception.ResourceNotFoundException;
import com.prepforge.repository.TestAttemptRepository;
import com.prepforge.repository.TestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final TestRepository testRepository;
    private final TestAttemptRepository attemptRepository;
    private final QuestionService questionService;

    // In-memory cache fallback for resilient stateless operation
    private final Map<String, TestAttempt> attemptCache = new ConcurrentHashMap<>();

    public ScoringService(
            TestRepository testRepository,
            TestAttemptRepository attemptRepository,
            QuestionService questionService) {
        this.testRepository = testRepository;
        this.attemptRepository = attemptRepository;
        this.questionService = questionService;
    }

    /**
     * Authoritative scoring calculation (Requirement #28, #29, #30).
     */
    public TestResultDto evaluateAndScoreTest(String testId, TestSubmissionRequest submission) {
        log.info("Evaluating test submission for testId: {}, attemptId: {}", testId, submission.getAttemptId());

        // Idempotency check: if attempt was already submitted, return the existing scored result
        String attemptId = submission.getAttemptId() != null && !submission.getAttemptId().isBlank()
                ? submission.getAttemptId()
                : "att_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        if (attemptCache.containsKey(attemptId)) {
            log.info("Idempotent submission detected for attemptId: {}", attemptId);
            return mapAttemptToResultDto(attemptCache.get(attemptId));
        }

        TestSession session = testRepository.findByTestId(testId)
                .orElse(null);

        List<Question> questions;
        if (session != null && session.getQuestionIds() != null && !session.getQuestionIds().isEmpty()) {
            questions = questionService.findByIds(session.getQuestionIds());
        } else {
            // If session in-memory, fetch questions based on submission keys
            questions = questionService.findByIds(new ArrayList<>(submission.getAnswers().keySet()));
        }

        if (questions.isEmpty()) {
            questions = questionService.prepareQuestionsForTest(
                    session != null ? session.getTopics() : List.of("Core Java"),
                    List.of(), "1-2 years", "Medium", List.of("Conceptual MCQ"), 10
            );
        }

        int total = questions.size();
        int correct = 0;
        int incorrect = 0;
        int skipped = 0;

        Map<String, int[]> topicScores = new HashMap<>(); // topic -> [correct, total]
        Map<String, int[]> difficultyScores = new HashMap<>(); // diff -> [correct, total]
        Map<String, int[]> typeScores = new HashMap<>(); // type -> [correct, total]

        List<QuestionResultDto> questionResults = new ArrayList<>();
        Map<String, String> userAnswers = submission.getAnswers() != null ? submission.getAnswers() : new HashMap<>();

        for (Question q : questions) {
            String userAns = userAnswers.get(q.getId());
            boolean isSkipped = userAns == null || userAns.isBlank();
            boolean isCorrect = !isSkipped && userAns.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());

            if (isCorrect) {
                correct++;
            } else if (isSkipped) {
                skipped++;
            } else {
                incorrect++;
            }

            // Topic stats
            String topic = q.getTopic() != null ? q.getTopic() : "General";
            topicScores.putIfAbsent(topic, new int[]{0, 0});
            topicScores.get(topic)[1]++;
            if (isCorrect) topicScores.get(topic)[0]++;

            // Difficulty stats
            String diff = q.getDifficulty() != null ? q.getDifficulty() : "Medium";
            difficultyScores.putIfAbsent(diff, new int[]{0, 0});
            difficultyScores.get(diff)[1]++;
            if (isCorrect) difficultyScores.get(diff)[0]++;

            // Question type stats
            String type = q.getQuestionType() != null ? q.getQuestionType() : "Conceptual MCQ";
            typeScores.putIfAbsent(type, new int[]{0, 0});
            typeScores.get(type)[1]++;
            if (isCorrect) typeScores.get(type)[0]++;

            // Build detailed question review item (Requirement #31)
            questionResults.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .question(q.getQuestion())
                    .options(q.getOptions())
                    .userAnswer(userAns)
                    .correctAnswer(q.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .isSkipped(isSkipped)
                    .explanation(q.getExplanation())
                    .optionExplanations(q.getOptionExplanations())
                    .topic(q.getTopic())
                    .subTopic(q.getSubTopic())
                    .difficulty(q.getDifficulty())
                    .questionType(q.getQuestionType())
                    .interviewTip(q.getInterviewTip())
                    .build());
        }

        double percentage = total > 0 ? ((double) correct / total) * 100.0 : 0.0;
        double roundedPct = Math.round(percentage * 10.0) / 10.0;
        double score = correct * 1.0;

        // Calculate breakdown percentages
        Map<String, Double> topicAccuracy = new HashMap<>();
        List<String> weakAreas = new ArrayList<>();
        List<String> strongAreas = new ArrayList<>();

        topicScores.forEach((t, counts) -> {
            double acc = counts[1] > 0 ? ((double) counts[0] / counts[1]) * 100.0 : 0.0;
            double rAcc = Math.round(acc * 10.0) / 10.0;
            topicAccuracy.put(t, rAcc);
            if (rAcc < 65.0) {
                weakAreas.add(t);
            } else {
                strongAreas.add(t);
            }
        });

        Map<String, Double> diffAccuracy = new HashMap<>();
        difficultyScores.forEach((d, counts) -> {
            double acc = counts[1] > 0 ? ((double) counts[0] / counts[1]) * 100.0 : 0.0;
            diffAccuracy.put(d, Math.round(acc * 10.0) / 10.0);
        });

        Map<String, Double> typeAccuracy = new HashMap<>();
        typeScores.forEach((typ, counts) -> {
            double acc = counts[1] > 0 ? ((double) counts[0] / counts[1]) * 100.0 : 0.0;
            typeAccuracy.put(typ, Math.round(acc * 10.0) / 10.0);
        });

        // Optimistic feedback message (Requirement #30)
        String feedbackMessage = generateOptimisticFeedback(roundedPct);

        TestAttempt attempt = TestAttempt.builder()
                .attemptId(attemptId)
                .testId(testId)
                .anonymousSessionId(submission.getAnonymousSessionId())
                .userAnswers(userAnswers)
                .totalQuestions(total)
                .correctAnswers(correct)
                .incorrectAnswers(incorrect)
                .skippedAnswers(skipped)
                .score(score)
                .percentage(roundedPct)
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .topicAccuracy(topicAccuracy)
                .difficultyAccuracy(diffAccuracy)
                .completed(true)
                .startedAt(Instant.now().minus(submission.getTimeTakenSeconds(), ChronoUnit.SECONDS))
                .completedAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        // Save attempt
        try {
            attemptRepository.save(attempt);
        } catch (Exception e) {
            log.warn("Attempt save in DB failed, cached in memory: {}", e.getMessage());
        }
        attemptCache.put(attemptId, attempt);

        String testTitle = session != null ? session.getTitle() : "Technical Interview Assessment";

        return TestResultDto.builder()
                .attemptId(attemptId)
                .testId(testId)
                .testTitle(testTitle)
                .totalQuestions(total)
                .correctCount(correct)
                .incorrectCount(incorrect)
                .skippedCount(skipped)
                .score(score)
                .percentage(roundedPct)
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .feedbackMessage(feedbackMessage)
                .topicAccuracy(topicAccuracy)
                .difficultyAccuracy(diffAccuracy)
                .questionTypeAccuracy(typeAccuracy)
                .weakAreas(weakAreas)
                .strongAreas(strongAreas)
                .questions(questionResults)
                .completedAt(Instant.now())
                .build();
    }

    public TestResultDto getAttemptResult(String attemptId) {
        TestAttempt attempt = attemptCache.get(attemptId);
        if (attempt == null) {
            attempt = attemptRepository.findByAttemptId(attemptId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with ID: " + attemptId));
        }
        return mapAttemptToResultDto(attempt);
    }

    private TestResultDto mapAttemptToResultDto(TestAttempt attempt) {
        TestSession session = testRepository.findByTestId(attempt.getTestId()).orElse(null);
        List<Question> questions = session != null
                ? questionService.findByIds(session.getQuestionIds())
                : questionService.findByIds(new ArrayList<>(attempt.getUserAnswers().keySet()));

        List<QuestionResultDto> questionResults = new ArrayList<>();
        Map<String, String> userAnswers = attempt.getUserAnswers() != null ? attempt.getUserAnswers() : new HashMap<>();

        for (Question q : questions) {
            String userAns = userAnswers.get(q.getId());
            boolean isSkipped = userAns == null || userAns.isBlank();
            boolean isCorrect = !isSkipped && userAns.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());

            questionResults.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .question(q.getQuestion())
                    .options(q.getOptions())
                    .userAnswer(userAns)
                    .correctAnswer(q.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .isSkipped(isSkipped)
                    .explanation(q.getExplanation())
                    .optionExplanations(q.getOptionExplanations())
                    .topic(q.getTopic())
                    .subTopic(q.getSubTopic())
                    .difficulty(q.getDifficulty())
                    .questionType(q.getQuestionType())
                    .interviewTip(q.getInterviewTip())
                    .build());
        }

        List<String> weak = new ArrayList<>();
        List<String> strong = new ArrayList<>();
        if (attempt.getTopicAccuracy() != null) {
            attempt.getTopicAccuracy().forEach((t, acc) -> {
                if (acc < 65.0) weak.add(t);
                else strong.add(t);
            });
        }

        return TestResultDto.builder()
                .attemptId(attempt.getAttemptId())
                .testId(attempt.getTestId())
                .testTitle(session != null ? session.getTitle() : "Technical Interview Assessment")
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectAnswers())
                .incorrectCount(attempt.getIncorrectAnswers())
                .skippedCount(attempt.getSkippedAnswers())
                .score(attempt.getScore())
                .percentage(attempt.getPercentage())
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .feedbackMessage(generateOptimisticFeedback(attempt.getPercentage()))
                .topicAccuracy(attempt.getTopicAccuracy())
                .difficultyAccuracy(attempt.getDifficultyAccuracy())
                .weakAreas(weak)
                .strongAreas(strong)
                .questions(questionResults)
                .completedAt(attempt.getCompletedAt())
                .build();
    }

    private String generateOptimisticFeedback(double percentage) {
        if (percentage >= 85.0) {
            return "Exceptional mastery — you demonstrate strong readiness for senior technical interview loops.";
        } else if (percentage >= 70.0) {
            return "Solid performance — you're building strong interview confidence with clear command of core patterns.";
        } else if (percentage >= 50.0) {
            return "Good progress — reviewing your growth areas below will help sharpen your interview edge.";
        } else {
            return "Great diagnostic baseline — targeted practice on the concepts below will accelerate your improvement.";
        }
    }
}
