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
public class QuestionDto {
    private String id;
    private String question;
    private List<String> options;
    
    // For test taking: correctAnswer, explanation, and optionExplanations are withheld until result
    private String correctAnswer;
    private String explanation;
    private Map<String, String> optionExplanations;
    
    private String topic;
    private String subTopic;
    private String difficulty;
    private String experienceLevel;
    private String questionType;
    private String interviewTip;
}
