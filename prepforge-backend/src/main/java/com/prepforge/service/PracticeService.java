package com.prepforge.service;

import com.prepforge.dto.*;
import com.prepforge.entity.Question;
import com.prepforge.entity.TestAttempt;
import com.prepforge.entity.TestSession;
import com.prepforge.exception.AppException;
import com.prepforge.exception.ResourceNotFoundException;
import com.prepforge.model.JavaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    private static final Logger log = LoggerFactory.getLogger(PracticeService.class);

    private final GeminiService geminiService;
    private final QuestionBankService questionBankService;

    // Fast, self-contained in-memory cache
    private final Map<String, TestSession> sessionCache = new ConcurrentHashMap<>();
    private final Map<String, Question> questionCache = new ConcurrentHashMap<>();
    private final Map<String, TestAttempt> attemptCache = new ConcurrentHashMap<>();

    public PracticeService(
            GeminiService geminiService,
            QuestionBankService questionBankService) {
        this.geminiService = geminiService;
        this.questionBankService = questionBankService;
    }

    public List<String> getTopics() {
        return JavaTopics.ALL_TOPICS;
    }

    public PracticeTestDto createPracticeTest(CreatePracticeRequest request) {
        List<String> topics = (request.getTopics() != null && !request.getTopics().isEmpty())
                ? request.getTopics()
                : JavaTopics.DEFAULT_TOPICS;

        String exp = (request.getExperienceLevel() != null && !request.getExperienceLevel().isBlank())
                ? request.getExperienceLevel().trim()
                : "Intermediate";

        int targetCount = Math.max(3, Math.min(50, request.getQuestionCount()));
        String testId = "test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        log.info("Creating practice test [{}] | topics: {} | exp: {} | count: {}", testId, topics, exp, targetCount);

        List<Question> collected = new ArrayList<>();
        Set<String> seenQuestions = new HashSet<>();

        // 1. Fetch questions from Gemini
        try {
            List<Question> aiQuestions = geminiService.generateQuestions(topics, exp, targetCount)
                    .get(45, java.util.concurrent.TimeUnit.SECONDS);

            for (Question q : aiQuestions) {
                if (collected.size() >= targetCount) break;
                if (isTopicAllowed(q.getTopic(), topics) && seenQuestions.add(normalizeText(q.getQuestion()))) {
                    collected.add(q);
                }
            }
        } catch (Exception e) {
            log.warn("Gemini batch generation note: {}. Using question bank.", e.getMessage());
        }

        // 2. Fill remaining from diverse question bank strictly for selected topics
        if (collected.size() < targetCount) {
            int needed = targetCount - collected.size();
            List<Question> bankQuestions = questionBankService.generateDynamicJavaQuestions(topics, exp, "Medium", needed);
            for (Question q : bankQuestions) {
                if (collected.size() >= targetCount) break;
                if (isTopicAllowed(q.getTopic(), topics) && seenQuestions.add(normalizeText(q.getQuestion()))) {
                    collected.add(q);
                }
            }
        }

        // 3. Fallback synthesis strictly for selected topics if still short
        int seed = 1;
        while (collected.size() < targetCount) {
            String topic = topics.get(collected.size() % topics.size());
            Question extra = questionBankService.createAlgorithmicOutputQuestion(topic, "Medium", exp);
            if (isTopicAllowed(extra.getTopic(), topics) && seenQuestions.add(normalizeText(extra.getQuestion()))) {
                collected.add(extra);
            }
            seed++;
            if (seed > 200) break;
        }

        // Cache questions
        for (Question q : collected) {
            questionCache.put(q.getId(), q);
        }

        List<String> qIds = collected.stream().map(Question::getId).collect(Collectors.toList());

        TestSession session = TestSession.builder()
                .testId(testId)
                .topics(topics)
                .experienceLevel(exp)
                .questionCount(collected.size())
                .questionIds(qIds)
                .createdAt(Instant.now())
                .build();

        sessionCache.put(testId, session);

        List<QuestionDto> dtos = collected.stream()
                .map(q -> mapToDto(q, false))
                .collect(Collectors.toList());

        return PracticeTestDto.builder()
                .testId(testId)
                .topics(topics)
                .experienceLevel(exp)
                .questionCount(collected.size())
                .timeLimitMinutes(Math.max(5, collected.size() * 2))
                .questions(dtos)
                .build();
    }

    public PracticeTestDto getPracticeTest(String testId) {
        TestSession session = getSession(testId);
        if (session == null) {
            throw new ResourceNotFoundException("Practice test not found: " + testId);
        }

        List<Question> questions = session.getQuestionIds().stream()
                .map(this::getQuestionById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<QuestionDto> dtos = questions.stream()
                .map(q -> mapToDto(q, false))
                .collect(Collectors.toList());

        return PracticeTestDto.builder()
                .testId(session.getTestId())
                .topics(session.getTopics())
                .experienceLevel(session.getExperienceLevel())
                .questionCount(questions.size())
                .timeLimitMinutes(Math.max(5, questions.size() * 2))
                .questions(dtos)
                .build();
    }

    public QuestionDto changeQuestion(String testId, String questionId, QuestionChangeRequest request) {
        TestSession session = getSession(testId);
        if (session == null) {
            throw new ResourceNotFoundException("Practice test not found: " + testId);
        }

        Question original = getQuestionById(questionId);
        String topic = (original != null && original.getTopic() != null)
                ? original.getTopic()
                : (request != null && request.getTopic() != null ? request.getTopic() : session.getTopics().get(0));

        String diff = (original != null && original.getDifficulty() != null)
                ? original.getDifficulty()
                : "Medium";

        String exp = (session.getExperienceLevel() != null)
                ? session.getExperienceLevel()
                : "Intermediate";

        List<String> previouslyUsed = new ArrayList<>();
        if (original != null) previouslyUsed.add(original.getQuestion());
        if (request != null && request.getPreviouslyUsedQuestions() != null) {
            previouslyUsed.addAll(request.getPreviouslyUsedQuestions());
        }

        Question replacement = null;
        try {
            replacement = geminiService.changeQuestion(topic, diff, exp, previouslyUsed)
                    .get(15, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        if (replacement == null || !geminiService.isValidQuestion(replacement)) {
            replacement = questionBankService.createAlgorithmicOutputQuestion(topic, diff, exp);
        }

        questionCache.put(replacement.getId(), replacement);

        // Replace questionId in session without changing question count
        int idx = session.getQuestionIds().indexOf(questionId);
        if (idx != -1) {
            session.getQuestionIds().set(idx, replacement.getId());
        } else {
            session.getQuestionIds().add(replacement.getId());
        }

        sessionCache.put(testId, session);

        return mapToDto(replacement, false);
    }

    public PracticeResultDto submitPracticeTest(String testId, SubmitPracticeRequest request) {
        TestSession session = getSession(testId);
        List<Question> questions = new ArrayList<>();
        if (session != null && session.getQuestionIds() != null) {
            for (String qId : session.getQuestionIds()) {
                Question q = getQuestionById(qId);
                if (q != null) questions.add(q);
            }
        }

        Map<String, String> answers = request != null && request.getAnswers() != null
                ? request.getAnswers()
                : Collections.emptyMap();

        int total = questions.size();
        int correct = 0;
        int incorrect = 0;
        int skipped = 0;

        Map<String, Integer> topicMistakes = new HashMap<>();
        List<QuestionDto> reviewDtos = new ArrayList<>();

        for (Question q : questions) {
            String userAns = answers.get(q.getId());
            boolean isSkipped = (userAns == null || userAns.isBlank());
            boolean isCorrect = !isSkipped && userAns.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());

            if (isCorrect) {
                correct++;
            } else if (isSkipped) {
                skipped++;
                topicMistakes.merge(q.getTopic(), 1, Integer::sum);
            } else {
                incorrect++;
                topicMistakes.merge(q.getTopic(), 1, Integer::sum);
            }

            QuestionDto dto = mapToDto(q, true);
            dto.setUserAnswer(userAns);
            dto.setIsCorrect(isCorrect);
            dto.setIsSkipped(isSkipped);
            reviewDtos.add(dto);
        }

        double percentage = total > 0 ? Math.round(((double) correct / total) * 1000.0) / 10.0 : 0.0;
        List<String> weakTopics = topicMistakes.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> revisionTips = generateRevisionTips(weakTopics);

        String attemptId = "att_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        TestAttempt attempt = TestAttempt.builder()
                .attemptId(attemptId)
                .testId(testId)
                .score(correct)
                .totalQuestions(total)
                .percentage(percentage)
                .correctCount(correct)
                .incorrectCount(incorrect)
                .skippedCount(skipped)
                .timeTakenSeconds(request != null ? request.getTimeTakenSeconds() : 0)
                .weakTopics(weakTopics)
                .revisionTips(revisionTips)
                .topicMistakes(topicMistakes)
                .userAnswers(answers)
                .completedAt(Instant.now())
                .build();

        attemptCache.put(attemptId, attempt);

        return PracticeResultDto.builder()
                .attemptId(attemptId)
                .testId(testId)
                .score(correct)
                .totalQuestions(total)
                .percentage(percentage)
                .correctCount(correct)
                .incorrectCount(incorrect)
                .skippedCount(skipped)
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .weakTopics(weakTopics)
                .revisionTips(revisionTips)
                .topicMistakes(topicMistakes)
                .questions(reviewDtos)
                .completedAt(attempt.getCompletedAt())
                .build();
    }

    public PracticeResultDto getAttemptResult(String attemptId) {
        TestAttempt attempt = attemptCache.get(attemptId);
        if (attempt == null) {
            throw new ResourceNotFoundException("Result attempt not found: " + attemptId);
        }

        TestSession session = getSession(attempt.getTestId());
        List<QuestionDto> reviewDtos = new ArrayList<>();
        if (session != null && session.getQuestionIds() != null) {
            for (String qId : session.getQuestionIds()) {
                Question q = getQuestionById(qId);
                if (q != null) {
                    String userAns = attempt.getUserAnswers() != null ? attempt.getUserAnswers().get(q.getId()) : null;
                    boolean isSkipped = userAns == null || userAns.isBlank();
                    boolean isCorrect = !isSkipped && userAns.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());

                    QuestionDto dto = mapToDto(q, true);
                    dto.setUserAnswer(userAns);
                    dto.setIsCorrect(isCorrect);
                    dto.setIsSkipped(isSkipped);
                    reviewDtos.add(dto);
                }
            }
        }

        return PracticeResultDto.builder()
                .attemptId(attempt.getAttemptId())
                .testId(attempt.getTestId())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .percentage(attempt.getPercentage())
                .correctCount(attempt.getCorrectCount())
                .incorrectCount(attempt.getIncorrectCount())
                .skippedCount(attempt.getSkippedCount())
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .weakTopics(attempt.getWeakTopics())
                .revisionTips(attempt.getRevisionTips())
                .topicMistakes(attempt.getTopicMistakes())
                .questions(reviewDtos)
                .completedAt(attempt.getCompletedAt())
                .build();
    }

    private List<String> generateRevisionTips(List<String> weakTopics) {
        List<String> tips = new ArrayList<>();
        for (String topic : weakTopics) {
            String t = topic.toLowerCase();
            if (t.contains("collection")) {
                tips.add("Collections: Review HashMap collision chaining, Iterator remove() vs for-each, and ConcurrentHashMap bucket CAS locking.");
            } else if (t.contains("stream") || t.contains("java 8") || t.contains("functional")) {
                tips.add("Streams & Java 8: Master intermediate vs terminal operations, flatMap() flattening, and lazy evaluation.");
            } else if (t.contains("thread") || t.contains("concurrency")) {
                tips.add("Multithreading: Review volatile memory barriers, AtomicInteger CAS operations, and ThreadPoolExecutor saturation policies.");
            } else if (t.contains("exception")) {
                tips.add("Exception Handling: Review try-with-resources suppressed exceptions and finally block return semantics.");
            } else if (t.contains("oop") || t.contains("inheritance") || t.contains("polymorphism")) {
                tips.add("OOP: Review method overriding rules (covariance, access modifiers), dynamic dispatch, and composition vs inheritance.");
            } else if (t.contains("jvm") || t.contains("memory")) {
                tips.add("JVM & Memory: Deep-dive into Heap vs Stack allocation, Metaspace, and Garbage Collector phases.");
            } else if (t.contains("string")) {
                tips.add("Strings: Review String constant pool, StringBuilder vs StringBuffer thread safety, and intern() mechanics.");
            } else {
                tips.add(topic + ": Focus on core syntax contracts, edge case handling, and common interview traps.");
            }
        }
        if (tips.isEmpty()) {
            tips.add("Outstanding performance! Continue solving mixed interview questions to preserve your sharp edge.");
        }
        return tips;
    }

    private TestSession getSession(String testId) {
        return sessionCache.get(testId);
    }

    private Question getQuestionById(String id) {
        if (id == null) return null;
        Question q = questionCache.get(id);
        if (q != null) return q;
        return questionBankService.getCuratedQuestionBank().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private QuestionDto mapToDto(Question q, boolean includeAnswers) {
        return QuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .options(q.getOptions())
                .correctAnswer(includeAnswers ? q.getCorrectAnswer() : null)
                .explanation(includeAnswers ? q.getExplanation() : null)
                .topic(q.getTopic())
                .difficulty(q.getDifficulty())
                .build();
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[^a-z0-9]", "").trim();
    }

    private boolean isTopicAllowed(String questionTopic, List<String> allowedTopics) {
        if (questionTopic == null || allowedTopics == null || allowedTopics.isEmpty()) return false;
        for (String allowed : allowedTopics) {
            if (allowed.equalsIgnoreCase(questionTopic) ||
                allowed.toLowerCase().contains(questionTopic.toLowerCase()) ||
                questionTopic.toLowerCase().contains(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
