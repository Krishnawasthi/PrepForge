package com.prepforge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_attempts")
public class TestAttempt {

    @Id
    private String id;

    @Indexed
    private String attemptId;

    @Indexed
    private String testId;

    @Indexed
    private String anonymousSessionId;

    // questionId -> chosen answer
    private Map<String, String> userAnswers;

    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int skippedAnswers;
    private double score;
    private double percentage;
    private long timeTakenSeconds;

    // topic/category breakdown
    private Map<String, Double> topicAccuracy;
    private Map<String, Double> difficultyAccuracy;

    private boolean completed;

    @Builder.Default
    private Instant startedAt = Instant.now();
    private Instant completedAt;

    @Indexed(name = "test_attempt_ttl_idx", expireAfter = "30d")
    private Instant expiresAt;
}
