package com.prepforge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConfigRequest {

    private String anonymousSessionId;
    private String title;
    private String promptDescription;

    private List<String> topics;

    private List<String> subTopics;

    @NotNull(message = "Experience level is required")
    private String experienceLevel;

    @NotNull(message = "Difficulty level is required")
    private String difficulty;

    private List<String> questionTypes;

    @Min(value = 5, message = "Minimum question count is 5")
    @Max(value = 50, message = "Maximum question count is 50 for high-yield assessment")
    @Builder.Default
    private int questionCount = 10;

    @Min(value = 0, message = "Time limit cannot be negative")
    @Max(value = 180, message = "Maximum time limit is 180 minutes")
    @Builder.Default
    private int timeLimitMinutes = 15;

    public List<String> getEffectiveTopics() {
        if (topics == null || topics.isEmpty()) {
            return List.of(
                "Core Java",
                "Object-Oriented Programming (OOP) & Patterns",
                "Java Collections Framework",
                "Java 8+ & Modern Java",
                "Streams API",
                "Multithreading & Concurrency",
                "JVM & Performance Tuning",
                "Spring Boot",
                "Spring Framework Core",
                "Spring Security & JWT",
                "RESTful API Design"
            );
        }
        return topics;
    }
}
