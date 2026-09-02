package com.prepforge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeRequest {

    private List<String> topics;

    @Builder.Default
    private String experienceLevel = "Intermediate";

    @Min(value = 3, message = "Minimum 3 questions")
    @Max(value = 50, message = "Maximum 50 questions")
    @Builder.Default
    private int questionCount = 10;
}
