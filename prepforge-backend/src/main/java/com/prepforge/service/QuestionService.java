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

    public QuestionService(
            QuestionRepository questionRepository,
            QuestionBankService questionBankService,
            AIService aiService) {
        this.questionRepository = questionRepository;
        this.questionBankService = questionBankService;
        this.aiService = aiService;
    }

    /**
     * Prepares EXACTLY the requested number of questions.
     * PRIMARY: Gemini AI (live LLM) generates unique, interview-critical questions.
     * FALLBACK: Dynamic parametric engine if Gemini is unavailable.
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
        log.info("Preparing EXACTLY {} questions via Gemini AI | topics={} | difficulty={} | exp={}",
                requestedCount, topics, difficulty, experienceLevel);

        List<Question> resultQuestions = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>();

        // ── STEP 1: Gemini AI (primary) ───────────────────────────────────────
        try {
            List<Map<String, Object>> aiMaps = aiService.generateQuestionsBatch(
                    topics, subTopics, experienceLevel, difficulty,
                    questionTypes, requestedCount, promptDescription
            ).get();

            if (aiMaps != null && !aiMaps.isEmpty()) {
                for (Map<String, Object> qMap : aiMaps) {
                    if (resultQuestions.size() >= requestedCount) break;
                    Question q = mapFromAiResponse(qMap);
                    if (isValidQuestion(q) && seenTexts.add(q.getQuestion().trim().toLowerCase())) {
                        resultQuestions.add(q);
                    }
                }
                log.info("Gemini generated {} valid questions (requested {})", resultQuestions.size(), requestedCount);
            }
        } catch (Exception e) {
            log.warn("Gemini AI generation failed, using fallback: {}", e.getMessage());
        }

        // ── STEP 2: Dynamic parametric fallback (if Gemini returned fewer) ────
        if (resultQuestions.size() < requestedCount) {
            int needed = requestedCount - resultQuestions.size();
            log.info("Gemini short by {}. Using dynamic parametric fallback.", needed);

            List<Question> dynamic = questionBankService.generateDynamicJavaQuestions(
                    topics, experienceLevel, difficulty, needed * 2 // Ask for extra to account for deduplication
            );

            for (Question q : dynamic) {
                if (resultQuestions.size() >= requestedCount) break;
                if (seenTexts.add(q.getQuestion().trim().toLowerCase())) {
                    resultQuestions.add(q);
                }
            }
        }

        // ── STEP 3: Final shuffle + option randomization ──────────────────────
        Collections.shuffle(resultQuestions);

        return resultQuestions.stream()
                .limit(requestedCount)
                .map(this::randomizeQuestionOptions)
                .collect(Collectors.toList());
    }

    // Overload for backwards compatibility (no promptDescription)
    public List<Question> prepareQuestionsForTest(
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            String difficulty,
            List<String> questionTypes,
            int targetCount) {
        return prepareQuestionsForTest(topics, subTopics, experienceLevel, difficulty, questionTypes, targetCount, null);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Question mapFromAiResponse(Map<String, Object> map) {
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

        String topic = String.valueOf(map.getOrDefault("topic", "Core Java")).trim();
        String subTopic = String.valueOf(map.getOrDefault("subTopic", "")).trim();
        String diff = String.valueOf(map.getOrDefault("difficulty", difficulty(map))).trim();
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

    private String difficulty(Map<String, Object> map) {
        return "Medium";
    }

    /**
     * Randomizes option order while keeping correctAnswer pointer intact.
     */
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
        if (question.getExplanation() == null || question.getExplanation().isBlank()) return false;
        if (question.getTopic() == null || question.getTopic().isBlank()) return false;
        return true;
    }

    public List<Question> findByIds(List<String> ids) {
        try {
            List<Question> list = questionRepository.findByIdIn(ids);
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}

        return questionBankService.getCuratedQuestionBank().stream()
                .filter(q -> ids.contains(q.getId()))
                .collect(Collectors.toList());
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
