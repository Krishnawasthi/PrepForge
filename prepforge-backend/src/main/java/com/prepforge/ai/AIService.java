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

    /**
     * Generates a single replacement question that tests the exact same underlying concept
     * while altering the scenario, code, values, and wording.
     */
    CompletableFuture<Map<String, Object>> generateReplacementQuestion(
            String topic,
            String subTopic,
            String concept,
            String difficulty,
            String experienceLevel,
            String questionType,
            List<String> usedQuestions
    );
}
