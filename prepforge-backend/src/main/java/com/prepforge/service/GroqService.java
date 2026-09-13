package com.prepforge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepforge.entity.Question;
import com.prepforge.model.JavaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final WebClient groqWebClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String configuredApiKey;

    @Value("${groq.api.model:openai/gpt-oss-120b}")
    private String configuredModel;

    public GroqService(WebClient groqWebClient) {
        this.groqWebClient = groqWebClient;
        this.objectMapper = new ObjectMapper();
    }

    private String getEffectiveApiKey() {
        String envKey = System.getenv("GROQ_API_KEY");
        if (envKey != null && !envKey.isBlank()) return envKey.trim();
        if (configuredApiKey != null && !configuredApiKey.isBlank()) return configuredApiKey.trim();
        return null;
    }

    /**
     * Ultra-fast batch question generation via Groq API.
     * Guaranteed sub-second/few-second inference on LPUs.
     */
    public CompletableFuture<List<Question>> generateQuestions(List<String> topics, String experienceLevel, int count) {
        String apiKey = getEffectiveApiKey();
        if (apiKey == null) {
            log.warn("Groq API key not configured. Returning empty list to trigger fallback.");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (count <= 10) {
            return generateQuestionChunk(apiKey, topics, experienceLevel, count);
        }

        // For counts > 10 (up to 50), chunk into parallel requests of 10 questions each
        int chunkSize = 10;
        int numChunks = (int) Math.ceil((double) count / chunkSize);

        List<CompletableFuture<List<Question>>> futures = new ArrayList<>();
        for (int i = 0; i < numChunks; i++) {
            int currentChunkSize = Math.min(chunkSize, count - (i * chunkSize));
            if (currentChunkSize <= 0) break;

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
                    log.info("Groq parallel batch produced {} combined questions (target: {})", combined.size(), count);
                    return combined;
                });
    }

    private CompletableFuture<List<Question>> generateQuestionChunk(String apiKey, List<String> topics, String experienceLevel, int count) {
        return CompletableFuture.supplyAsync(() -> {
            String topicList = String.join(", ", topics);
            int codeReviewCount = (int) Math.round(count * 0.45);
            int conceptualCount = count - codeReviewCount;

            String subtopicBlock = JavaTopics.getSubtopicPromptBlock(topics);
            String experienceGuidance = buildExperienceGuidance(experienceLevel);

            String prompt = String.format(
                    "You are a Principal Java Technical Interviewer and JVM Specialist.\n" +
                    "Generate EXACTLY %d high-yield Java interview practice questions for a candidate with '%s' experience.\n\n" +
                    "%s\n\n" +
                    "CRITICAL TOPIC CONSTRAINT (MANDATORY):\n" +
                    "- EVERY single question MUST strictly test only these chosen topics: [%s].\n" +
                    "- ABSOLUTELY DO NOT generate questions on any other topic.\n" +
                    "- The 'topic' field in each JSON object MUST be chosen strictly from: [%s].\n\n" +
                    "SUBTOPIC COVERAGE (MANDATORY):\n" +
                    "Each topic has specific subtopics/concepts. You MUST spread questions across as many different subtopics as possible.\n" +
                    "DO NOT repeat the same subtopic. DO NOT ask generic surface-level questions like 'What is inheritance?'.\n" +
                    "Instead, ask deep, specific questions testing individual subtopics.\n\n" +
                    "Topic → Subtopics:\n%s\n" +
                    "Spread questions evenly across these subtopics. Each question should test a DIFFERENT subtopic/concept.\n\n" +
                    "CODE REVIEW & OUTPUT PREDICTION REQUIREMENT (MANDATORY):\n" +
                    "- Exactly %d questions MUST be Code Review / Output Prediction questions ('What is the output of the following Java code snippet?', 'What will be printed when executed?').\n" +
                    "- Every code review question MUST include a complete, valid Java code block enclosed in ```java\\n...\\n```.\n" +
                    "- The remaining %d questions should be deep practical or conceptual interview scenarios on the selected subtopics.\n\n" +
                    "QUESTION RULES:\n" +
                    "- Exactly 4 distinct options (A, B, C, D) per question.\n" +
                    "- Exactly 1 unambiguous correct answer that matches an option word-for-word.\n" +
                    "- Clear, in-depth technical explanation of why the answer is correct and why common misconceptions fail.\n" +
                    "- NEVER repeat a question. Every question must test a different concept.\n\n" +
                    "RETURN FORMAT (VALID JSON ONLY):\n" +
                    "Return ONLY a JSON object with a single key 'questions' containing an array of objects:\n" +
                    "{\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"question\": \"Question statement (with ```java\\n...\\n``` for code review questions)\",\n" +
                    "      \"options\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"],\n" +
                    "      \"correctAnswer\": \"Option 1\",\n" +
                    "      \"explanation\": \"Technical explanation\",\n" +
                    "      \"topic\": \"%s\",\n" +
                    "      \"difficulty\": \"Hard\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                    count, experienceLevel,
                    experienceGuidance,
                    topicList, topicList,
                    subtopicBlock,
                    codeReviewCount, conceptualCount, topics.get(0)
            );

            try {
                Map<String, Object> reqBody = Map.of(
                        "model", configuredModel != null ? configuredModel : "openai/gpt-oss-120b",
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an expert Java interviewer. Always respond with valid JSON containing the 'questions' array."),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "response_format", Map.of("type", "json_object"),
                        "temperature", 0.7,
                        "max_tokens", 8192
                );

                String bodyJson = objectMapper.writeValueAsString(reqBody);
                String raw = callGroq(apiKey, bodyJson, 25);
                if (raw == null) return Collections.emptyList();

                String jsonText = extractJsonText(raw);
                if (jsonText == null) return Collections.emptyList();

                List<Map<String, Object>> list = parseQuestionsList(jsonText);
                List<Question> questions = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    Question q = parseQuestion(map, topics);
                    if (isValidQuestion(q)) {
                        questions.add(q);
                    }
                }
                log.info("Groq successfully generated {} valid Java questions for topics: {}", questions.size(), topics);
                return questions;
            } catch (Exception e) {
                log.warn("Groq chunk generation error: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    /**
     * Generates a single replacement question strictly on the specified topic via Groq.
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

            List<String> subtopics = JavaTopics.getSubtopics(topic);
            String subtopicHint = subtopics.isEmpty() ? "" :
                    "Available subtopics for '" + topic + "': " + String.join(", ", subtopics) +
                    "\nPick a subtopic that was NOT already tested in the previous questions.\n\n";
            String experienceGuidance = buildExperienceGuidance(experienceLevel);

            String prompt = String.format(
                    "You are a Principal Java Technical Interviewer.\n" +
                    "Generate a COMPLETELY NEW Java interview question testing STRICTLY the topic: '%s'.\n\n" +
                    "%s\n\n" +
                    "%s" +
                    "Can be either a conceptual question or a tricky code output question ('What is the output of the following Java code snippet?' with ```java...```).\n\n" +
                    "%s" +
                    "REQUIREMENTS:\n" +
                    "- Strictly on topic '%s'. Do not switch topics.\n" +
                    "- Test a DIFFERENT subtopic/concept than what was previously asked.\n" +
                    "- Exactly 4 options.\n" +
                    "- Exactly 1 correct answer.\n" +
                    "- Clear technical explanation.\n" +
                    "- Return a SINGLE JSON object with keys: question, options, correctAnswer, explanation, topic, difficulty.",
                    topic,
                    experienceGuidance,
                    subtopicHint,
                    usedBuilder.toString(), topic
            );

            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    Map<String, Object> reqBody = Map.of(
                            "model", configuredModel != null ? configuredModel : "openai/gpt-oss-120b",
                            "messages", List.of(
                                    Map.of("role", "system", "content", "You are an expert Java interviewer. Always respond with valid JSON."),
                                    Map.of("role", "user", "content", prompt)
                            ),
                            "response_format", Map.of("type", "json_object"),
                            "temperature", 0.7 + (attempt * 0.1),
                            "max_tokens", 2048
                    );
                    String raw = callGroq(apiKey, objectMapper.writeValueAsString(reqBody), 10);
                    if (raw == null) continue;

                    String jsonText = extractJsonText(raw);
                    if (jsonText == null) continue;

                    Map<String, Object> map = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
                    Question q = parseQuestion(map, List.of(topic));
                    if (isValidQuestion(q)) {
                        return q;
                    }
                } catch (Exception e) {
                    log.warn("Groq question replacement attempt {} failed: {}", attempt, e.getMessage());
                }
            }
            return null;
        });
    }

    private String callGroq(String apiKey, String body, int timeoutSeconds) {
        List<String> candidateModels = List.of(
                configuredModel != null && !configuredModel.isBlank() ? configuredModel.trim() : "openai/gpt-oss-120b",
                "qwen/qwen3.8-27b",
                "groq/compound"
        );

        for (String model : candidateModels) {
            try {
                String payload = body.replace("\"model\":\"" + configuredModel + "\"", "\"model\":\"" + model + "\"");
                String response = groqWebClient.post()
                        .uri("/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .block();

                if (response != null && !response.isBlank()) {
                    return response;
                }
            } catch (Exception e) {
                log.warn("Groq model [{}] call attempt note: {}", model, e.getMessage());
            }
        }
        return null;
    }

    private String extractJsonText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            String text = contentNode.asText().trim();

            if (text.startsWith("```json")) text = text.substring(7);
            else if (text.startsWith("```")) text = text.substring(3);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3);

            return text.trim();
        } catch (Exception e) {
            log.warn("Failed extracting JSON from Groq response: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> parseQuestionsList(String jsonText) {
        try {
            JsonNode root = objectMapper.readTree(jsonText);
            if (root.isArray()) {
                return objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {});
            } else if (root.isObject()) {
                if (root.has("questions") && root.get("questions").isArray()) {
                    return objectMapper.convertValue(root.get("questions"), new TypeReference<List<Map<String, Object>>>() {});
                } else {
                    // Single question wrapped in object
                    Map<String, Object> single = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
                    return List.of(single);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing questions list from JSON: {}", e.getMessage());
        }
        return Collections.emptyList();
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

        String matchedTopic = allowedTopics.stream()
                .filter(t -> t.equalsIgnoreCase(rawTopic) ||
                             rawTopic.toLowerCase().contains(t.toLowerCase()) ||
                             t.toLowerCase().contains(rawTopic.toLowerCase()))
                .findFirst()
                .orElse(allowedTopics.get(0));

        String diff = String.valueOf(map.getOrDefault("difficulty", "Hard")).trim();

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

    private String buildExperienceGuidance(String exp) {
        String level = (exp != null) ? exp.trim().toLowerCase() : "intermediate";
        if (level.contains("adv")) {
            return "EXPERIENCE LEVEL: ADVANCED / SENIOR STAFF LEVEL (MANDATORY):\n" +
                    "- Questions MUST be genuinely DIFFICULT, TRICKY, nuanced, and intellectually rigorous.\n" +
                    "- Strictly AVOID basic recall or surface-level definition questions (e.g. do NOT ask 'What is an interface?' or 'What is a class?').\n" +
                    "- FOR PREDICT THE OUTPUT / CODE REVIEW QUESTIONS (~45% of total):\n" +
                    "  * Code snippets MUST test subtle Java edge cases, compiler tricks, and JVM runtime semantics.\n" +
                    "  * Emphasize tricky scenarios: static vs instance block initialization order in inheritance hierarchies, method overload resolution (widening vs boxing vs varargs precedence and ambiguous calls), try-catch-finally return overrides & suppressed exception chaining, String constant pool reference equality vs new String() vs intern(), IntegerCache (-128..127) vs object references, variable shadowing vs dynamic method dispatch, anonymous inner class effectively-final variable captures, volatile array non-volatility of elements, stream lazy evaluation mutations / parallel stream race conditions, bitwise shift overflow with byte/short promotions, and generic type erasure bridge methods.\n" +
                    "  * Design tempting distractor options based on common developer misconceptions (e.g., subtle compilation errors, alternate evaluation orders, runtime exceptions).\n" +
                    "- FOR CONCEPTUAL QUESTIONS:\n" +
                    "  * Deep JVM internals, Java Memory Model (JMM happens-before guarantees, cache invalidation, CAS operations), architectural trade-offs, and edge-case concurrency.";
        } else if (level.contains("beg")) {
            return "EXPERIENCE LEVEL: BEGINNER / FOUNDATIONAL:\n" +
                    "- Focus on foundational Java syntax, control structures, standard OOP mechanisms, essential collection usage, and clear exception handling.";
        } else {
            return "EXPERIENCE LEVEL: INTERMEDIATE / PROFESSIONAL:\n" +
                    "- Focus on real-world idiomatic Java, OOP design, collection framework internals (HashMap collision chaining, ArrayList growth), Stream pipelines, concurrency basics (Executors, CompletableFuture), and practical interview scenarios.";
        }
    }
}
