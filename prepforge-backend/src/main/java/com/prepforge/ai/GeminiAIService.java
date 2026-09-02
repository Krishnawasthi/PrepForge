package com.prepforge.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);
    private static final int CHUNK_SIZE = 15; // Generate in chunks of 15 to avoid token limit issues

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String configuredApiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String configuredModel;

    public GeminiAIService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = new ObjectMapper();
    }

    // ---- Key Resolution (Header > Env > Properties) -------------------------

    private String getEffectiveApiKey() {
        // 1. HTTP request header (user can pass their own key)
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String headerKey = attributes.getRequest().getHeader("X-Gemini-Key");
                if (headerKey != null && !headerKey.isBlank()) return headerKey.trim();
            }
        } catch (Exception ignored) {}

        // 2. Environment variable
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) return envKey.trim();

        // 3. application.yml / application.properties
        if (configuredApiKey != null && !configuredApiKey.isBlank()) return configuredApiKey.trim();

        return null;
    }

    // ---- Prompt Interpretation -----------------------------------------------

    @Override
    public CompletableFuture<Map<String, Object>> interpretPrompt(String prompt) {
        String apiKey = getEffectiveApiKey();
        if (apiKey == null) return CompletableFuture.completedFuture(Collections.emptyMap());

        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt =
                    "You are an expert Java backend interview coach. " +
                    "Analyse the user's text and return a JSON object with EXACTLY these keys: " +
                    "topics (array of strings from: Core Java, Object-Oriented Programming (OOP) & Patterns, " +
                    "Java Collections Framework, Java 8+ & Modern Java, Streams API, " +
                    "Multithreading & Concurrency, Exception Handling & Best Practices, " +
                    "JVM & Performance Tuning, Spring Boot, Spring Framework Core, " +
                    "Spring Security & JWT, Spring Cloud & Microservices, RESTful API Design), " +
                    "experienceLevel (one of: 0-1 years, 1-2 years, 2-3 years, 3-5 years, 5+ years), " +
                    "difficulty (one of: Easy, Medium, Hard, Mixed), " +
                    "questionTypes (array: Conceptual MCQ, Output-based, Scenario-based, Debugging), " +
                    "questionCount (integer 5-40), timeLimitMinutes (integer). " +
                    "User text: " + prompt;

                String body = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", systemPrompt)))),
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.3)
                ));

                String response = callGemini(apiKey, body, 15);
                if (response != null) {
                    String json = extractJsonText(response);
                    return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                log.warn("Gemini prompt interpretation failed: {}", e.getMessage());
            }
            return Collections.emptyMap();
        });
    }

    // ---- Question Generation --------------------------------------------------

    @Override
    public CompletableFuture<List<Map<String, Object>>> generateQuestionsBatch(
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            String difficulty,
            List<String> questionTypes,
            int count,
            String promptDescription) {

        String apiKey = getEffectiveApiKey();
        if (apiKey == null) {
            log.warn("No Gemini API key configured. Question generation will fail. Please set GEMINI_API_KEY.");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> allQuestions = new ArrayList<>();
            Set<String> seenQuestions = new HashSet<>();

            // For large counts, generate in chunks to avoid token limits
            int remaining = count;
            int chunkNum = 0;

            while (remaining > 0) {
                int chunkCount = Math.min(CHUNK_SIZE, remaining);
                chunkNum++;

                log.info("Gemini chunk {}: generating {} questions for topics={}, difficulty={}, exp={}",
                        chunkNum, chunkCount, topics, difficulty, experienceLevel);

                List<Map<String, Object>> chunk = generateChunk(
                        apiKey, topics, subTopics, experienceLevel,
                        questionTypes, chunkCount, promptDescription, seenQuestions, chunkNum, difficulty);

                for (Map<String, Object> q : chunk) {
                    String qText = String.valueOf(q.getOrDefault("question", ""));
                    if (!qText.isBlank() && seenQuestions.add(qText.toLowerCase().trim())) {
                        allQuestions.add(q);
                    }
                }

                remaining -= chunkCount;

                // Safety: If Gemini returned nothing in this chunk, break to avoid infinite loop
                if (chunk.isEmpty()) {
                    log.warn("Gemini returned empty chunk for count={}. Breaking loop.", chunkCount);
                    break;
                }
            }

            log.info("Gemini total questions generated: {} (requested: {})", allQuestions.size(), count);
            return allQuestions;
        });
    }

    // ---- Internal Helpers ----------------------------------------------------

    private List<Map<String, Object>> generateChunk(
            String apiKey,
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            List<String> questionTypes,
            int chunkCount,
            String promptDescription,
            Set<String> alreadySeen,
            int chunkNum,
            String difficulty) {

        try {
            String userContext = (promptDescription != null && !promptDescription.isBlank())
                    ? "\n\nUser's specific requirements: " + promptDescription
                    : "";

            String alreadySeenHint = alreadySeen.isEmpty() ? ""
                    : "\n\nIMPORTANT — do NOT repeat any question on these concepts already used: "
                    + String.join(", ", alreadySeen.stream().limit(8).toList());

            String prompt = "You are a senior Java backend engineer and technical interviewer.\n"
                    + "Generate EXACTLY " + chunkCount + " unique, high-quality Java backend interview questions.\n\n"
                    + "TOPICS: " + String.join(", ", topics) + "\n"
                    + "EXPERIENCE LEVEL: " + experienceLevel + "\n"
                    + "DIFFICULTY: " + difficulty + "\n"
                    + "QUESTION FORMATS: " + String.join(", ", questionTypes) + "\n"
                    + userContext
                    + alreadySeenHint + "\n\n"
                    + "RULES:\n"
                    + "1. Each question must cover a DISTINCT concept — no repetition.\n"
                    + "2. Include Java code snippets (inside markdown ``` blocks) where helpful.\n"
                    + "3. Questions must be things senior interviewers actually ask.\n"
                    + "4. All 4 options must be plausible, not obviously wrong.\n"
                    + "5. correctAnswer must EXACTLY match one of the 4 options strings.\n\n"
                    + "Return a JSON array of EXACTLY " + chunkCount + " objects. Each object:\n"
                    + "{ question, options: [4 strings], correctAnswer, explanation, "
                    + "optionExplanations: {A,B,C,D}, topic, subTopic, difficulty, "
                    + "experienceLevel, questionType, interviewTip }";

            String body = objectMapper.writeValueAsString(Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                    "responseMimeType", "application/json",
                    "temperature", 0.85
                )
            ));

            log.info("Gemini chunk {}: requesting {} questions | topics={}", chunkNum, chunkCount, topics);
            String response = callGemini(apiKey, body, 55);
            if (response == null) return Collections.emptyList();

            String jsonText = extractJsonText(response);
            if (jsonText == null || jsonText.isBlank()) {
                log.warn("Gemini chunk {} returned blank JSON text", chunkNum);
                return Collections.emptyList();
            }

            return objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {});

        } catch (Exception e) {
            log.error("Gemini chunk {} generation error: {}", chunkNum, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String callGemini(String apiKey, String body, int timeoutSeconds) {
        // Ordered candidates: primary fast model, latest flash-lite, then 3-flash preview
        List<String> candidateModels = new ArrayList<>();
        if (configuredModel != null && !configuredModel.isBlank()) {
            candidateModels.add(configuredModel.trim());
        }
        if (!candidateModels.contains("gemini-3.5-flash-lite")) candidateModels.add("gemini-3.5-flash-lite");
        if (!candidateModels.contains("gemini-flash-lite-latest")) candidateModels.add("gemini-flash-lite-latest");
        if (!candidateModels.contains("gemini-3-flash-preview")) candidateModels.add("gemini-3-flash-preview");

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
                log.warn("Gemini model [{}] call note: {}. Trying fallback model if available.", model, e.getMessage());
            }
        }
        log.error("All Gemini candidate models failed to generate content.");
        return null;
    }

    /**
     * Extracts the JSON text from Gemini's response envelope.
     * Handles cases where Gemini wraps output in markdown code fences.
     */
    private String extractJsonText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Strip markdown code fences if present: ```json ... ``` or ``` ... ```
            if (text.contains("```")) {
                Pattern fence = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
                Matcher m = fence.matcher(text);
                if (m.find()) {
                    return m.group(1).trim();
                }
            }

            // Find the JSON array or object boundaries
            int start = text.indexOf('[');
            int startObj = text.indexOf('{');
            if (start == -1 && startObj == -1) return text.trim();

            if (start != -1 && (startObj == -1 || start <= startObj)) {
                int end = text.lastIndexOf(']');
                if (end > start) return text.substring(start, end + 1).trim();
            } else {
                int end = text.lastIndexOf('}');
                if (end > startObj) return text.substring(startObj, end + 1).trim();
            }

            return text.trim();
        } catch (Exception e) {
            log.warn("JSON text extraction from Gemini response failed: {}", e.getMessage());
            return null;
        }
    }
}
