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
public class QuestionChangeRequest {

    private String topic;
    private String difficulty;
    private String experienceLevel;
    private List<String> previouslyUsedQuestions;
}
