package com.prepforge.service;

import com.prepforge.ai.AIService;
import com.prepforge.dto.*;
import com.prepforge.entity.Question;
import com.prepforge.entity.TestSession;
import com.prepforge.exception.AppException;
import com.prepforge.exception.ResourceNotFoundException;
import com.prepforge.repository.TestRepository;
import com.prepforge.util.PromptParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    private final AIService aiService;
    private final TestRepository testRepository;
    private final TopicService topicService;
    private final QuestionService questionService;
    private final ScoringService scoringService;
    private final RateLimitService rateLimitService;

    // In-memory test cache for fast retrieval and offline fallback
    private final Map<String, TestSession> testSessionCache = new ConcurrentHashMap<>();

    public TestService(
            AIService aiService,
            TestRepository testRepository,
            TopicService topicService,
            QuestionService questionService,
            ScoringService scoringService,
            RateLimitService rateLimitService) {
        this.aiService = aiService;
        this.testRepository = testRepository;
        this.topicService = topicService;
        this.questionService = questionService;
        this.scoringService = scoringService;
        this.rateLimitService = rateLimitService;
    }

    public PromptInterpretationResponse interpretUserPrompt(PromptInterpretationRequest request) {
        log.info("Interpreting user test prompt: {}", request.getPrompt());
        rateLimitService.checkAndConsumeRateLimit("anon_interpret", "interpretPrompt");
        return PromptParserUtil.parsePromptRuleBased(request.getPrompt());
    }

    public TestSession validateAndCreateTestSession(TestConfigRequest config) {
        if (config.getTopics() == null || config.getTopics().isEmpty()) {
            throw new AppException("At least one topic must be selected");
        }

        String testId = "test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String anonId = config.getAnonymousSessionId() != null && !config.getAnonymousSessionId().isBlank()
                ? config.getAnonymousSessionId()
                : "anon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        String title = config.getTitle();
        if (title == null || title.isBlank()) {
            title = String.join(", ", config.getTopics()) + " Assessment";
        }

        TestSession session = TestSession.builder()
                .testId(testId)
                .anonymousSessionId(anonId)
                .title(title)
                .promptDescription(config.getPromptDescription())
                .topics(config.getTopics())
                .subTopics(config.getSubTopics() != null ? config.getSubTopics() : new ArrayList<>())
                .experienceLevel(config.getExperienceLevel())
                .difficulty(config.getDifficulty())
                .questionTypes(config.getQuestionTypes() != null ? config.getQuestionTypes() : List.of("Conceptual MCQ", "Output-based"))
                .questionCount(config.getQuestionCount())
                .timeLimitMinutes(config.getTimeLimitMinutes())
                .questionIds(new ArrayList<>())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        testSessionCache.put(testId, session);
        try {
            TestSession saved = testRepository.save(session);
            if (saved != null) {
                return saved;
            }
        } catch (Exception e) {
            log.warn("TestSession DB save skipped, using memory store: {}", e.getMessage());
        }
        return session;
    }

    /**
     * Generates a fully populated test with randomized questions.
     */
    public TestDetailDto generateFullTest(TestConfigRequest config) {
        rateLimitService.checkAndConsumeRateLimit(config.getAnonymousSessionId(), "generateTest");

        TestSession session = validateAndCreateTestSession(config);

        // Fetch / generate randomized question batch — pass user's prompt for richer AI context
        List<Question> questions = questionService.prepareQuestionsForTest(
                session.getTopics(),
                session.getSubTopics(),
                session.getExperienceLevel(),
                session.getDifficulty(),
                session.getQuestionTypes(),
                session.getQuestionCount(),
                session.getPromptDescription()
        );

        List<String> qIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        session.setQuestionIds(qIds);
        session.setQuestionCount(questions.size());

        testSessionCache.put(session.getTestId(), session);
        try {
            testRepository.save(session);
        } catch (Exception ignored) {}

        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> questionService.mapToDto(q, false)) // Withhold correct answers during test taking
                .collect(Collectors.toList());

        return TestDetailDto.builder()
                .testId(session.getTestId())
                .title(session.getTitle())
                .promptDescription(session.getPromptDescription())
                .topics(session.getTopics())
                .experienceLevel(session.getExperienceLevel())
                .difficulty(session.getDifficulty())
                .questionCount(questions.size())
                .timeLimitMinutes(session.getTimeLimitMinutes())
                .questions(questionDtos)
                .build();
    }

    public TestDetailDto getTestDetail(String testId) {
        TestSession session = testSessionCache.get(testId);
        if (session == null) {
            session = testRepository.findByTestId(testId)
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found with ID: " + testId));
        }

        List<Question> questions = questionService.findByIds(session.getQuestionIds());
        if (questions.isEmpty()) {
            questions = questionService.prepareQuestionsForTest(
                    session.getTopics(), session.getSubTopics(),
                    session.getExperienceLevel(), session.getDifficulty(),
                    session.getQuestionTypes(), session.getQuestionCount()
            );
        }

        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> questionService.mapToDto(q, false))
                .collect(Collectors.toList());

        return TestDetailDto.builder()
                .testId(session.getTestId())
                .title(session.getTitle())
                .promptDescription(session.getPromptDescription())
                .topics(session.getTopics())
                .experienceLevel(session.getExperienceLevel())
                .difficulty(session.getDifficulty())
                .questionCount(questions.size())
                .timeLimitMinutes(session.getTimeLimitMinutes())
                .questions(questionDtos)
                .build();
    }

    public TestResultDto submitTest(String testId, TestSubmissionRequest request) {
        return scoringService.evaluateAndScoreTest(testId, request);
    }

    public TestResultDto getAttemptResult(String attemptId) {
        return scoringService.getAttemptResult(attemptId);
    }

    public TestDetailDto createWeakAreaPracticeTest(WeakAreaPracticeRequest request) {
        TestConfigRequest config = TestConfigRequest.builder()
                .anonymousSessionId(request.getAnonymousSessionId())
                .title("Weak Area Focus Practice: " + String.join(", ", request.getWeakTopics()))
                .topics(request.getWeakTopics())
                .experienceLevel("1-2 years")
                .difficulty("Medium")
                .questionTypes(List.of("Conceptual MCQ", "Output-based", "Scenario-based"))
                .questionCount(request.getQuestionCount())
                .timeLimitMinutes(Math.max(10, request.getQuestionCount()))
                .build();

        return generateFullTest(config);
    }
}
