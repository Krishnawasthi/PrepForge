package com.prepforge.util;

import com.prepforge.dto.PromptInterpretationResponse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromptParserUtil {

    private static final Map<String, String> TOPIC_KEYWORDS = new LinkedHashMap<>();

    static {
        // Multi-word matches first
        TOPIC_KEYWORDS.put("spring boot", "Spring Boot");
        TOPIC_KEYWORDS.put("spring security", "Spring Security & JWT");
        TOPIC_KEYWORDS.put("spring cloud", "Spring Cloud & Microservices");
        TOPIC_KEYWORDS.put("microservice", "Spring Cloud & Microservices");
        TOPIC_KEYWORDS.put("core java", "Core Java");
        TOPIC_KEYWORDS.put("object oriented", "Object-Oriented Programming (OOP) & Patterns");
        TOPIC_KEYWORDS.put("design pattern", "Object-Oriented Programming (OOP) & Patterns");
        TOPIC_KEYWORDS.put("garbage collection", "JVM & Performance Tuning");
        TOPIC_KEYWORDS.put("exception handling", "Exception Handling & Best Practices");
        TOPIC_KEYWORDS.put("rest api", "RESTful API Design");
        TOPIC_KEYWORDS.put("restful", "RESTful API Design");

        // Specific Java concepts
        TOPIC_KEYWORDS.put("multithread", "Multithreading & Concurrency");
        TOPIC_KEYWORDS.put("concurrency", "Multithreading & Concurrency");
        TOPIC_KEYWORDS.put("thread", "Multithreading & Concurrency");
        TOPIC_KEYWORDS.put("collection", "Java Collections Framework");
        TOPIC_KEYWORDS.put("hashmap", "Java Collections Framework");
        TOPIC_KEYWORDS.put("stream", "Streams API");
        TOPIC_KEYWORDS.put("java 8", "Java 8+ & Modern Java");
        TOPIC_KEYWORDS.put("java8", "Java 8+ & Modern Java");
        TOPIC_KEYWORDS.put("jvm", "JVM & Performance Tuning");
        TOPIC_KEYWORDS.put("oop", "Object-Oriented Programming (OOP) & Patterns");
        TOPIC_KEYWORDS.put("spring", "Spring Framework Core");
        TOPIC_KEYWORDS.put("jpa", "JPA (Java Persistence API)");
        TOPIC_KEYWORDS.put("hibernate", "Hibernate ORM & Performance");
        TOPIC_KEYWORDS.put("kafka", "Kafka & Messaging in Java");
        TOPIC_KEYWORDS.put("redis", "Redis & Backend Caching");
        TOPIC_KEYWORDS.put("sql", "SQL & Query Optimization");
        TOPIC_KEYWORDS.put("dbms", "DBMS & Database Transactions");
        TOPIC_KEYWORDS.put("transaction", "DBMS & Database Transactions");
        TOPIC_KEYWORDS.put("database", "SQL & Query Optimization");
        TOPIC_KEYWORDS.put("java", "Core Java");
    }

    public static PromptInterpretationResponse parsePromptRuleBased(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return getDefaultResponse(prompt);
        }

        String lower = prompt.toLowerCase();
        Set<String> matchedTopics = new LinkedHashSet<>();

        for (Map.Entry<String, String> entry : TOPIC_KEYWORDS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                matchedTopics.add(entry.getValue());
            }
        }

        if (matchedTopics.isEmpty()) {
            matchedTopics.add("Core Java");
            matchedTopics.add("Java Collections Framework");
            matchedTopics.add("Spring Boot");
        }

        // Parse Experience Level numerically
        String experience = "1-2 years";
        Pattern expPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:\\+)?\\s*(?:years?|yoe|yrs?)", Pattern.CASE_INSENSITIVE);
        Matcher expMatcher = expPattern.matcher(lower);

        if (expMatcher.find()) {
            try {
                double years = Double.parseDouble(expMatcher.group(1));
                if (years <= 1.0) {
                    experience = "0-1 years";
                } else if (years <= 2.0) {
                    experience = "1-2 years";
                } else if (years <= 3.0) {
                    experience = "2-3 years";
                } else if (years <= 5.0) {
                    experience = "3-5 years";
                } else {
                    experience = "5+ years";
                }
            } catch (NumberFormatException ignored) {}
        } else if (lower.contains("senior") || lower.contains("lead") || lower.contains("architect") || lower.contains("5+")) {
            experience = "5+ years";
        } else if (lower.contains("beginner") || lower.contains("fresher") || lower.contains("entry") || lower.contains("junior")) {
            experience = "0-1 years";
        }

        // Parse Difficulty
        String difficulty = "Medium";
        if ((lower.contains("hard") || lower.contains("tough") || lower.contains("advanced")) && lower.contains("medium")) {
            difficulty = "Mixed";
        } else if (lower.contains("hard") || lower.contains("tough") || lower.contains("advanced")) {
            difficulty = "Hard";
        } else if (lower.contains("easy") || lower.contains("basic")) {
            difficulty = "Easy";
        } else if (lower.contains("adaptive")) {
            difficulty = "Adaptive";
        } else if (lower.contains("mixed")) {
            difficulty = "Mixed";
        }

        // Parse Question Types
        Set<String> questionTypes = new LinkedHashSet<>();
        if (lower.contains("output")) {
            questionTypes.add("Output-based");
        }
        if (lower.contains("tricky") || lower.contains("trick")) {
            questionTypes.add("Interview trick questions");
        }
        if (lower.contains("scenario") || lower.contains("practical") || lower.contains("real-world")) {
            questionTypes.add("Scenario-based");
        }
        if (lower.contains("debug") || lower.contains("debugging")) {
            questionTypes.add("Debugging");
        }
        if (lower.contains("code analysis") || lower.contains("code-analysis")) {
            questionTypes.add("Code analysis");
        }
        if (lower.contains("sql") || lower.contains("query")) {
            questionTypes.add("SQL query/result");
        }
        if (questionTypes.isEmpty()) {
            questionTypes.add("Conceptual MCQ");
            questionTypes.add("Output-based");
            questionTypes.add("Scenario-based");
        }

        // Parse Question Count (up to 50)
        int count = 15;
        Pattern countPattern = Pattern.compile("(\\b(?:[5-9]|[1-4][0-9]|50)\\b)\\s*(?:questions|qs|items)");
        Matcher matcher = countPattern.matcher(lower);
        if (matcher.find()) {
            try {
                count = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        } else {
            // Also check standalone numbers like "give me 30 questions" or "30 question"
            Pattern fallbackPattern = Pattern.compile("(?:give me|test me on|create|generate)\\s*(\\d{1,2})\\s*questions?", Pattern.CASE_INSENSITIVE);
            Matcher fbMatcher = fallbackPattern.matcher(lower);
            if (fbMatcher.find()) {
                try {
                    int parsed = Integer.parseInt(fbMatcher.group(1));
                    if (parsed >= 5 && parsed <= 50) {
                        count = parsed;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        String goal = "Java Backend Developer Technical Interview";

        return PromptInterpretationResponse.builder()
                .originalPrompt(prompt)
                .goal(goal)
                .topics(new ArrayList<>(matchedTopics))
                .subTopics(List.of())
                .experienceLevel(experience)
                .difficulty(difficulty)
                .questionTypes(new ArrayList<>(questionTypes))
                .questionCount(count)
                .timeLimitMinutes(Math.max(10, count))
                .interpretationConfidence(0.96)
                .build();
    }

    private static PromptInterpretationResponse getDefaultResponse(String prompt) {
        return PromptInterpretationResponse.builder()
                .originalPrompt(prompt != null ? prompt : "")
                .goal("Java Backend Developer Interview Assessment")
                .topics(List.of("Core Java", "Java Collections Framework", "Spring Boot"))
                .subTopics(List.of())
                .experienceLevel("1-2 years")
                .difficulty("Medium")
                .questionTypes(List.of("Conceptual MCQ", "Output-based"))
                .questionCount(10)
                .timeLimitMinutes(15)
                .interpretationConfidence(0.70)
                .build();
    }
}
