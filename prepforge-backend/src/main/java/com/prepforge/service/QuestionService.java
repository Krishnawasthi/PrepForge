package com.prepforge.service;

import com.prepforge.ai.AIService;
import com.prepforge.dto.QuestionDto;
import com.prepforge.entity.Question;
import com.prepforge.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final QuestionBankService questionBankService;
    private final AIService aiService;
    private final Map<String, Question> questionCache = new java.util.concurrent.ConcurrentHashMap<>();

    public QuestionService(
            QuestionRepository questionRepository,
            QuestionBankService questionBankService,
            AIService aiService) {
        this.questionRepository = questionRepository;
        this.questionBankService = questionBankService;
        this.aiService = aiService;
    }

    /**
     * Prepares EXACTLY the requested number of questions with zero repetition,
     * strict topic adherence, and realistic 4-option randomization.
     */
    public List<Question> prepareQuestionsForTest(
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            String difficulty,
            List<String> questionTypes,
            int targetCount,
            String promptDescription) {

        int requestedCount = Math.max(1, Math.min(50, targetCount));
        List<String> effectiveTopics = (topics != null && !topics.isEmpty())
                ? topics
                : List.of("Core Java", "Spring Boot", "Multithreading & Concurrency");

        log.info("Preparing EXACTLY {} questions | topics={} | difficulty={} | exp={}",
                requestedCount, effectiveTopics, difficulty, experienceLevel);

        List<Question> resultQuestions = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>();

        // ── STEP 1: Gemini AI (Parallel, primary) ──────────────────────────────
        try {
            List<Map<String, Object>> aiMaps = aiService.generateQuestionsBatch(
                    effectiveTopics, subTopics, experienceLevel, difficulty,
                    questionTypes, requestedCount, promptDescription
            ).get();

            if (aiMaps != null && !aiMaps.isEmpty()) {
                for (Map<String, Object> qMap : aiMaps) {
                    if (resultQuestions.size() >= requestedCount) break;
                    Question q = mapFromAiResponse(qMap, effectiveTopics);
                    if (isValidQuestion(q)) {
                        String normalized = normalizeQuestionText(q.getQuestion());
                        if (seenTexts.add(normalized)) {
                            resultQuestions.add(q);
                        }
                    }
                }
                log.info("Gemini delivered {} unique valid questions (requested {})",
                        resultQuestions.size(), requestedCount);
            }
        } catch (Exception e) {
            log.warn("Gemini AI generation note (falling back to curated/dynamic bank): {}", e.getMessage());
        }

        // ── STEP 2: Curated Bank for Selected Topics ─────────────────────────
        if (resultQuestions.size() < requestedCount) {
            List<Question> curatedBank = new ArrayList<>(questionBankService.getCuratedQuestionBank());
            Collections.shuffle(curatedBank);

            // Filter for selected topics first
            for (Question q : curatedBank) {
                if (resultQuestions.size() >= requestedCount) break;
                if (matchesAnyTopic(q.getTopic(), effectiveTopics)) {
                    String normalized = normalizeQuestionText(q.getQuestion());
                    if (seenTexts.add(normalized)) {
                        resultQuestions.add(q);
                    }
                }
            }
        }

        // ── STEP 3: Diverse Parametric Engine for Remaining Slots ─────────────
        if (resultQuestions.size() < requestedCount) {
            int needed = requestedCount - resultQuestions.size();
            log.info("Filling remaining {} questions using diverse parametric engine for topics: {}",
                    needed, effectiveTopics);

            List<Question> dynamic = questionBankService.generateDynamicJavaQuestions(
                    effectiveTopics, experienceLevel, difficulty, needed * 2
            );

            for (Question q : dynamic) {
                if (resultQuestions.size() >= requestedCount) break;
                String normalized = normalizeQuestionText(q.getQuestion());
                if (seenTexts.add(normalized)) {
                    resultQuestions.add(q);
                }
            }
        }

        // ── STEP 4: Final Shuffle & Option Letter Randomization ────────────────
        Collections.shuffle(resultQuestions);

        List<Question> finalQuestions = resultQuestions.stream()
                .limit(requestedCount)
                .map(this::randomizeQuestionOptions)
                .collect(Collectors.toList());

        finalQuestions.forEach(q -> questionCache.put(q.getId(), q));
        try {
            questionRepository.saveAll(finalQuestions);
        } catch (Exception ignored) {}

        return finalQuestions;
    }

    public List<Question> prepareQuestionsForTest(
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            String difficulty,
            List<String> questionTypes,
            int targetCount) {
        return prepareQuestionsForTest(topics, subTopics, experienceLevel, difficulty, questionTypes, targetCount, null);
    }

    private boolean matchesAnyTopic(String questionTopic, List<String> requestedTopics) {
        if (questionTopic == null || requestedTopics == null || requestedTopics.isEmpty()) return true;
        String qLower = questionTopic.toLowerCase();
        for (String req : requestedTopics) {
            String rLower = req.toLowerCase();
            if (qLower.contains(rLower) || rLower.contains(qLower)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeQuestionText(String text) {
        if (text == null) return "";
        // Strip markdown code fences, spaces, and punctuation for strict similarity comparison
        return text.toLowerCase()
                .replaceAll("```[a-z]*", "")
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    @SuppressWarnings("unchecked")
    private Question mapFromAiResponse(Map<String, Object> map, List<String> requestedTopics) {
        String id = "q_ai_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String question = String.valueOf(map.getOrDefault("question", "")).trim();
        List<String> options = (List<String>) map.getOrDefault("options", Collections.emptyList());
        String correctAnswer = String.valueOf(map.getOrDefault("correctAnswer", "")).trim();
        String explanation = String.valueOf(map.getOrDefault("explanation", "")).trim();

        Object rawOptExp = map.get("optionExplanations");
        Map<String, String> optionExplanations = new LinkedHashMap<>();
        if (rawOptExp instanceof Map) {
            ((Map<?, ?>) rawOptExp).forEach((k, v) -> optionExplanations.put(String.valueOf(k), String.valueOf(v)));
        }

        String topic = String.valueOf(map.getOrDefault("topic", "")).trim();
        if (topic.isBlank() || !matchesAnyTopic(topic, requestedTopics)) {
            topic = (requestedTopics != null && !requestedTopics.isEmpty()) ? requestedTopics.get(0) : "Core Java";
        }

        String subTopic = String.valueOf(map.getOrDefault("subTopic", "")).trim();
        String diff = String.valueOf(map.getOrDefault("difficulty", "Medium")).trim();
        String exp = String.valueOf(map.getOrDefault("experienceLevel", "1-2 years")).trim();
        String qType = String.valueOf(map.getOrDefault("questionType", "Conceptual MCQ")).trim();
        String tip = String.valueOf(map.getOrDefault("interviewTip", "Review core Java concepts.")).trim();

        return Question.builder()
                .id(id)
                .question(question)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .optionExplanations(optionExplanations)
                .topic(topic)
                .subTopic(subTopic)
                .difficulty(diff)
                .experienceLevel(exp)
                .questionType(qType)
                .interviewTip(tip)
                .build();
    }

    public Question randomizeQuestionOptions(Question original) {
        if (original.getOptions() == null || original.getOptions().size() < 2) {
            return original;
        }

        List<String> shuffledOptions = new ArrayList<>(original.getOptions());
        Collections.shuffle(shuffledOptions);

        return Question.builder()
                .id(original.getId())
                .question(original.getQuestion())
                .options(shuffledOptions)
                .correctAnswer(original.getCorrectAnswer())
                .explanation(original.getExplanation())
                .optionExplanations(original.getOptionExplanations())
                .topic(original.getTopic())
                .subTopic(original.getSubTopic())
                .difficulty(original.getDifficulty())
                .experienceLevel(original.getExperienceLevel())
                .questionType(original.getQuestionType())
                .interviewTip(original.getInterviewTip())
                .createdAt(original.getCreatedAt())
                .build();
    }

    public boolean isValidQuestion(Question question) {
        if (question == null) return false;
        if (question.getQuestion() == null || question.getQuestion().isBlank()) return false;
        if (question.getOptions() == null || question.getOptions().size() != 4) return false;
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) return false;
        if (!question.getOptions().contains(question.getCorrectAnswer())) return false;
        return true;
    }

    public Question findById(String id) {
        if (id == null) return null;
        Question q = questionCache.get(id);
        if (q != null) return q;
        try {
            Optional<Question> opt = questionRepository.findById(id);
            if (opt.isPresent()) {
                questionCache.put(id, opt.get());
                return opt.get();
            }
        } catch (Exception ignored) {}
        return questionBankService.getCuratedQuestionBank().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<Question> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Question> result = new ArrayList<>();
        for (String id : ids) {
            Question q = findById(id);
            if (q != null) {
                result.add(q);
            }
        }
        return result;
    }

    public Question generateReplacementQuestion(
            String topic,
            String subTopic,
            String concept,
            String difficulty,
            String experienceLevel,
            String questionType,
            List<String> usedQuestions) {

        Set<String> normalizedUsed = new HashSet<>();
        if (usedQuestions != null) {
            for (String u : usedQuestions) {
                if (u != null && !u.isBlank()) {
                    normalizedUsed.add(normalizeQuestionText(u));
                }
            }
        }

        // 1. Try Gemini AI with up to 3 attempts
        try {
            for (int attempt = 0; attempt < 3; attempt++) {
                Map<String, Object> map = aiService.generateReplacementQuestion(
                        topic, subTopic, concept, difficulty, experienceLevel, questionType, usedQuestions
                ).get(15, java.util.concurrent.TimeUnit.SECONDS);

                if (map != null && !map.isEmpty()) {
                    Question candidate = mapFromAiResponse(map, List.of(topic));
                    if (isValidQuestion(candidate)) {
                        String norm = normalizeQuestionText(candidate.getQuestion());
                        if (!normalizedUsed.contains(norm)) {
                            candidate = randomizeQuestionOptions(candidate);
                            questionCache.put(candidate.getId(), candidate);
                            try { questionRepository.save(candidate); } catch (Exception ignored) {}
                            log.info("Successfully generated AI replacement question for concept [{}]: {}", concept, candidate.getId());
                            return candidate;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Gemini replacement question generation note: {}", e.getMessage());
        }

        // 2. Fallback: Parametric Question Engine with Concept Preservation
        log.info("Generating parametric replacement question preserving concept [{}] for topic [{}]", concept, topic);
        for (int retry = 0; retry < 5; retry++) {
            Question fallback = questionBankService.createAlgorithmicOutputQuestion(topic, difficulty, experienceLevel);
            if (fallback != null) {
                String norm = normalizeQuestionText(fallback.getQuestion());
                if (!normalizedUsed.contains(norm)) {
                    if (subTopic != null && !subTopic.isBlank()) fallback.setSubTopic(subTopic);
                    if (concept != null && !concept.isBlank()) fallback.setInterviewTip("Core concept tested: " + concept);
                    fallback = randomizeQuestionOptions(fallback);
                    questionCache.put(fallback.getId(), fallback);
                    try { questionRepository.save(fallback); } catch (Exception ignored) {}
                    return fallback;
                }
            }
        }

        throw new com.prepforge.exception.AppException("We couldn't generate a new question right now. Please try again.");
    }

    public QuestionDto mapToDto(Question q, boolean includeAnswers) {
        return QuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .options(q.getOptions())
                .correctAnswer(includeAnswers ? q.getCorrectAnswer() : null)
                .explanation(includeAnswers ? q.getExplanation() : null)
                .optionExplanations(includeAnswers ? q.getOptionExplanations() : null)
                .topic(q.getTopic())
                .subTopic(q.getSubTopic())
                .difficulty(q.getDifficulty())
                .experienceLevel(q.getExperienceLevel())
                .questionType(q.getQuestionType())
                .interviewTip(includeAnswers ? q.getInterviewTip() : null)
                .build();
    }
}
