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
public class PracticeTestDto {

    private String testId;
    private List<String> topics;
    private String experienceLevel;
    private int questionCount;
    private int timeLimitMinutes;
    private List<QuestionDto> questions;
}
