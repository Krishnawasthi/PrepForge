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
public class PromptInterpretationResponse {

    private String originalPrompt;
    private String goal;
    private List<String> topics;
    private List<String> subTopics;
    private String experienceLevel;
    private String difficulty;
    private List<String> questionTypes;
    private int questionCount;
    private int timeLimitMinutes;
    private double interpretationConfidence;
}
