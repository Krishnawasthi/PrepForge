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
import java.util.stream.Collectors;

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
     * Batch-generates Java interview questions strictly from the chosen topics.
     * Guaranteed ~45% code review / output prediction questions with IDE code snippets.
     * Chunks large counts (up to 50) into parallel requests for maximum speed and zero token truncation.
     */
    public CompletableFuture<List<Question>> generateQuestions(List<String> topics, String experienceLevel, int count) {
        String apiKey = getEffectiveApiKey();
        if (apiKey == null) {
            log.warn("Gemini API key not configured. Returning empty list to trigger local bank fallback.");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // If count is small (<= 12), single request is optimal
        if (count <= 12) {
            return generateQuestionChunk(apiKey, topics, experienceLevel, count);
        }

        // For large counts (up to 50), chunk into parallel requests of 10-12 questions each
        int chunkSize = 10;
        int numChunks = (int) Math.ceil((double) count / chunkSize);

        List<CompletableFuture<List<Question>>> futures = new ArrayList<>();
        for (int i = 0; i < numChunks; i++) {
            int currentChunkSize = Math.min(chunkSize, count - (i * chunkSize));
            if (currentChunkSize <= 0) break;

            // Sub-slice topics for this chunk to ensure balanced representation
            int topicStart = (i * 2) % topics.size();
            List<String> chunkTopics = new ArrayList<>();
            for (int t = 0; t < Math.min(3, topics.size()); t++) {
                chunkTopics.add(topics.get((topicStart + t) % topics.size()));
            }

            futures.add(generateQuestionChunk(apiKey, chunkTopics, experienceLevel, currentChunkSize));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Question> combined = new ArrayList<>();
                    Set<String> seen = new HashSet<>();
                    for (CompletableFuture<List<Question>> f : futures) {
                        try {
                            List<Question> list = f.join();
                            for (Question q : list) {
                                if (combined.size() >= count) break;
                                String norm = q.getQuestion().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                                if (seen.add(norm)) {
                                    combined.add(q);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    log.info("Gemini parallel batch produced {} combined questions (target: {})", combined.size(), count);
                    return combined;
                });
    }

    private CompletableFuture<List<Question>> generateQuestionChunk(String apiKey, List<String> topics, String experienceLevel, int count) {
        return CompletableFuture.supplyAsync(() -> {
            String topicList = String.join(", ", topics);
            int codeReviewCount = (int) Math.round(count * 0.45);
            int conceptualCount = count - codeReviewCount;

            String prompt = String.format(
                    "You are a Senior Java Technical Interviewer.\n" +
                    "Generate EXACTLY %d high-yield Java interview practice questions for a candidate with '%s' experience.\n\n" +
                    "CRITICAL TOPIC CONSTRAINT (MANDATORY):\n" +
                    "- EVERY single question MUST strictly test only these chosen topics: [%s].\n" +
                    "- ABSOLUTELY DO NOT generate questions on any other topic.\n" +
                    "- The 'topic' field in each JSON object MUST be chosen strictly from: [%s].\n\n" +
                    "CODE REVIEW & OUTPUT PREDICTION REQUIREMENT (MANDATORY):\n" +
                    "- Exactly %d questions MUST be Code Review / Output Prediction questions.\n" +
                    "  * E.g. 'What is the output of the following Java code snippet?', 'What will be printed when this code is executed?', 'Does this code compile or throw an exception at runtime?'.\n" +
                    "  * Every code review question MUST include a complete, valid Java code block enclosed in ```java\\n...\\n```.\n" +
                    "- The remaining %d questions should be deep practical or conceptual interview scenarios on the selected topics.\n\n" +
                    "QUESTION RULES:\n" +
                    "- Exactly 4 distinct options (A, B, C, D) per question.\n" +
                    "- Exactly 1 unambiguous correct answer that matches an option word-for-word.\n" +
                    "- Clear technical explanation.\n\n" +
                    "RETURN FORMAT:\n" +
                    "Return ONLY a valid JSON array of objects with these keys:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"Question statement (with ```java\\n...\\n``` for code review questions)\",\n" +
                    "    \"options\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"],\n" +
                    "    \"correctAnswer\": \"Option 1\",\n" +
                    "    \"explanation\": \"Technical explanation\",\n" +
                    "    \"topic\": \"%s\",\n" +
                    "    \"difficulty\": \"Medium\"\n" +
                    "  }\n" +
                    "]",
                    count, experienceLevel, topicList, topicList, codeReviewCount, conceptualCount, topics.get(0)
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
                return questions;
            } catch (Exception e) {
                log.warn("Gemini chunk generation note: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    /**
     * Generates a single replacement question strictly on the specified topic.
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
                    "Generate a COMPLETELY NEW Java interview question testing STRICTLY the topic: '%s'.\n" +
                    "Experience level: %s.\n" +
                    "Can be either a conceptual question or a code output question ('What is the output of the following Java code snippet?' with ```java...```).\n\n" +
                    "%s\n" +
                    "REQUIREMENTS:\n" +
                    "- Strictly on topic '%s'. Do not switch topics.\n" +
                    "- Exactly 4 options.\n" +
                    "- Exactly 1 correct answer.\n" +
                    "- Clear technical explanation.\n" +
                    "- Return a SINGLE JSON object with keys: question, options, correctAnswer, explanation, topic, difficulty.",
                    topic, experienceLevel != null ? experienceLevel : "Intermediate",
                    usedBuilder.toString(), topic
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
    private Question parseQuestion(Map<String, Object> map, List<String> allowedTopics) {
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
        String rawTopic = String.valueOf(map.getOrDefault("topic", "")).trim();

        // Strict topic enforcement: find exact or best match in allowedTopics
        String matchedTopic = allowedTopics.stream()
                .filter(t -> t.equalsIgnoreCase(rawTopic) ||
                             rawTopic.toLowerCase().contains(t.toLowerCase()) ||
                             t.toLowerCase().contains(rawTopic.toLowerCase()))
                .findFirst()
                .orElse(allowedTopics.get(0));

        String diff = String.valueOf(map.getOrDefault("difficulty", "Medium")).trim();

        return Question.builder()
                .id("q_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                .question(qText)
                .options(options)
                .correctAnswer(correct)
                .explanation(explanation)
                .topic(matchedTopic)
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
