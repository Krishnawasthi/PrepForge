package com.prepforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDto {
    private String attemptId;
    private String testId;
    private String testTitle;
    private int totalQuestions;
    private int correctCount;
    private int incorrectCount;
    private int skippedCount;
    private double score;
    private double percentage;
    private long timeTakenSeconds;
    private String feedbackMessage;

    // Performance breakdowns
    private Map<String, Double> topicAccuracy;
    private Map<String, Double> difficultyAccuracy;
    private Map<String, Double> questionTypeAccuracy;

    // Identified areas
    private List<String> weakAreas;
    private List<String> strongAreas;

    // Detailed question reviews
    private List<QuestionResultDto> questions;

    private Instant completedAt;
}
