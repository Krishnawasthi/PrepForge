package com.prepforge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepforge.entity.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String configuredApiKey;

    @Value("${gemini.api.model:gemini-3.5-flash-lite}")
    private String configuredModel;

    public GeminiService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = new ObjectMapper();
    }

    private String getEffectiveApiKey() {
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) return envKey.trim();
        if (configuredApiKey != null && !configuredApiKey.isBlank()) return configuredApiKey.trim();
        return null;
    }

    /**
     * Batch-generates Java interview questions with naturally mixed difficulty.
     */
    public CompletableFuture<List<Question>> generateQuestions(List<String> topics, String experienceLevel, int count) {
        String apiKey = getEffectiveApiKey();
        if (apiKey == null) {
            log.warn("Gemini API key not configured. Returning empty list to trigger local bank fallback.");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            String topicList = String.join(", ", topics);
            String prompt = String.format(
                    "You are a Senior Java Technical Interviewer.\n" +
                    "Generate EXACTLY %d high-yield Java interview practice questions for a candidate with '%s' experience.\n" +
                    "STRICT TOPIC SCOPE: Only questions related to: %s.\n" +
                    "Do NOT include questions outside these topics.\n\n" +
                    "DIFFICULTY REQUIREMENT:\n" +
                    "Provide a realistic natural mixture of difficulty (approx. 30%% Easy, 50%% Medium, 20%% Hard).\n\n" +
                    "QUESTION FORMAT RULES:\n" +
                    "- Exactly 4 distinct plausible options (A, B, C, D) per question.\n" +
                    "- Exactly 1 unambiguous correct answer that exists word-for-word in the options.\n" +
                    "- Clear, technically deep explanation of why the correct answer is right.\n" +
                    "- Variety: include conceptual questions, tricky output prediction code snippets, and practical scenarios.\n\n" +
                    "RETURN FORMAT:\n" +
                    "Return ONLY a valid JSON array of objects with these keys:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"Question statement with optional markdown code snippet\",\n" +
                    "    \"options\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"],\n" +
                    "    \"correctAnswer\": \"Option 1\",\n" +
                    "    \"explanation\": \"Technical explanation\",\n" +
                    "    \"topic\": \"%s\",\n" +
                    "    \"difficulty\": \"Medium\"\n" +
                    "  }\n" +
                    "]",
                    count, experienceLevel, topicList, topics.get(0)
            );

            try {
                Map<String, Object> reqBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(Map.of("text", prompt)))
                        ),
                        "generationConfig", Map.of(
                                "temperature", 0.75,
                                "maxOutputTokens", 8192,
                                "responseMimeType", "application/json"
                        )
                );

                String bodyJson = objectMapper.writeValueAsString(reqBody);
                String raw = callGemini(apiKey, bodyJson, 30);
                if (raw == null) return Collections.emptyList();

                String jsonText = extractJsonText(raw);
                if (jsonText == null) return Collections.emptyList();

                List<Map<String, Object>> list = objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {});
                List<Question> questions = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    Question q = parseQuestion(map, topics);
                    if (isValidQuestion(q)) {
                        questions.add(q);
                    }
                }
                log.info("Gemini successfully generated {} valid Java questions for topics: {}", questions.size(), topics);
                return questions;
            } catch (Exception e) {
                log.warn("Gemini question generation error: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    /**
     * Generates a single replacement question on the same topic.
     */
    public CompletableFuture<Question> changeQuestion(String topic, String difficulty, String experienceLevel, List<String> usedQuestions) {
        String apiKey = getEffectiveApiKey();
        if (apiKey == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            StringBuilder usedBuilder = new StringBuilder();
            if (usedQuestions != null && !usedQuestions.isEmpty()) {
                usedBuilder.append("DO NOT DUPLICATE OR PARAPHRASE ANY OF THESE PREVIOUSLY SHOWN QUESTIONS:\n");
                for (int i = 0; i < Math.min(usedQuestions.size(), 8); i++) {
                    usedBuilder.append("- ").append(usedQuestions.get(i).replace("\n", " ")).append("\n");
                }
            }

            String prompt = String.format(
                    "You are an expert Java interviewer.\n" +
                    "Generate a COMPLETELY NEW Java interview question testing: '%s'.\n" +
                    "Difficulty: %s | Experience: %s.\n\n" +
                    "%s\n" +
                    "REQUIREMENTS:\n" +
                    "- Exactly 4 options.\n" +
                    "- Exactly 1 correct answer.\n" +
                    "- Clear technical explanation.\n" +
                    "- Return a SINGLE JSON object with keys: question, options, correctAnswer, explanation, topic, difficulty.",
                    topic, difficulty != null ? difficulty : "Medium", experienceLevel != null ? experienceLevel : "Intermediate",
                    usedBuilder.toString()
            );

            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    Map<String, Object> reqBody = Map.of(
                            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                            "generationConfig", Map.of(
                                    "temperature", 0.75 + (attempt * 0.1),
                                    "maxOutputTokens", 2048,
                                    "responseMimeType", "application/json"
                            )
                    );
                    String raw = callGemini(apiKey, objectMapper.writeValueAsString(reqBody), 15);
                    if (raw == null) continue;

                    String jsonText = extractJsonText(raw);
                    if (jsonText == null) continue;

                    Map<String, Object> map = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
                    Question q = parseQuestion(map, List.of(topic));
                    if (isValidQuestion(q)) {
                        return q;
                    }
                } catch (Exception e) {
                    log.warn("Gemini question replacement attempt {} failed: {}", attempt, e.getMessage());
                }
            }
            return null;
        });
    }

    private String callGemini(String apiKey, String body, int timeoutSeconds) {
        List<String> candidateModels = List.of(
                configuredModel != null && !configuredModel.isBlank() ? configuredModel.trim() : "gemini-3.5-flash-lite",
                "gemini-flash-lite-latest",
                "gemini-3-flash-preview"
        );

        for (String model : candidateModels) {
            try {
                String response = geminiWebClient.post()
                        .uri("/v1beta/models/" + model + ":generateContent")
                        .header("Content-Type", "application/json")
                        .header("X-goog-api-key", apiKey)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .block();

                if (response != null && !response.isBlank()) {
                    return response;
                }
            } catch (Exception e) {
                log.warn("Gemini model [{}] call attempt note: {}", model, e.getMessage());
            }
        }
        return null;
    }

    private String extractJsonText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            if (text.startsWith("```json")) text = text.substring(7);
            else if (text.startsWith("```")) text = text.substring(3);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3);

            text = text.trim();
            int startArray = text.indexOf('[');
            int endArray = text.lastIndexOf(']');
            if (startArray != -1 && endArray > startArray) {
                return text.substring(startArray, endArray + 1).trim();
            }

            int startObj = text.indexOf('{');
            int endObj = text.lastIndexOf('}');
            if (startObj != -1 && endObj > startObj) {
                return text.substring(startObj, endObj + 1).trim();
            }
            return text;
        } catch (Exception e) {
            log.warn("Failed extracting JSON from Gemini response: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Question parseQuestion(Map<String, Object> map, List<String> topics) {
        String qText = String.valueOf(map.getOrDefault("question", "")).trim();
        List<String> options = new ArrayList<>();
        Object opts = map.get("options");
        if (opts instanceof List<?>) {
            for (Object opt : (List<?>) opts) {
                options.add(String.valueOf(opt).trim());
            }
        }
        String correct = String.valueOf(map.getOrDefault("correctAnswer", "")).trim();
        String explanation = String.valueOf(map.getOrDefault("explanation", "")).trim();
        String topic = String.valueOf(map.getOrDefault("topic", topics.get(0))).trim();
        String diff = String.valueOf(map.getOrDefault("difficulty", "Medium")).trim();

        return Question.builder()
                .id("q_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                .question(qText)
                .options(options)
                .correctAnswer(correct)
                .explanation(explanation)
                .topic(topic)
                .difficulty(diff)
                .build();
    }

    public boolean isValidQuestion(Question q) {
        if (q == null) return false;
        if (q.getQuestion() == null || q.getQuestion().isBlank()) return false;
        if (q.getOptions() == null || q.getOptions().size() != 4) return false;
        if (q.getCorrectAnswer() == null || q.getCorrectAnswer().isBlank()) return false;
        return q.getOptions().contains(q.getCorrectAnswer());
    }
}
