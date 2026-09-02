package com.prepforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {

    private String id;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String topic;
    private String difficulty;
    private String userAnswer;
    private Boolean isCorrect;
    private Boolean isSkipped;
}
