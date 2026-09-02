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
public class TestDetailDto {
    private String testId;
    private String title;
    private String promptDescription;
    private List<String> topics;
    private String experienceLevel;
    private String difficulty;
    private int questionCount;
    private int timeLimitMinutes;
    private List<QuestionDto> questions;
}
