package com.prepforge.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AIService {

    /**
     * Interprets a natural language prompt describing interview preparation requirements.
     */
    CompletableFuture<Map<String, Object>> interpretPrompt(String prompt);

    /**
     * Generates a batch of structured multiple-choice questions for the specified configuration.
     * Accepts an optional natural language promptDescription for richer context.
     */
    CompletableFuture<List<Map<String, Object>>> generateQuestionsBatch(
            List<String> topics,
            List<String> subTopics,
            String experienceLevel,
            String difficulty,
            List<String> questionTypes,
            int count,
            String promptDescription
    );
}
