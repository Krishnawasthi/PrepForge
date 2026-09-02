package com.prepforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDto {
    private String questionId;
    private String question;
    private List<String> options;
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private boolean isSkipped;
    private String explanation;
    private Map<String, String> optionExplanations;
    private String topic;
    private String subTopic;
    private String difficulty;
    private String questionType;
    private String interviewTip;
}
