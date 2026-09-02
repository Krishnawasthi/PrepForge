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
public class PracticeResultDto {

    private String attemptId;
    private String testId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private int correctCount;
    private int incorrectCount;
    private int skippedCount;
    private int timeTakenSeconds;
    private List<String> weakTopics;
    private List<String> revisionTips;
    private Map<String, Integer> topicMistakes;
    private List<QuestionDto> questions;
    private Instant completedAt;
}
