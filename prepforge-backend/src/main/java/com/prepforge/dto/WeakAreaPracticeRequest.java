package com.prepforge.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeakAreaPracticeRequest {
    private String anonymousSessionId;

    @NotEmpty(message = "At least one weak area topic must be provided")
    private List<String> weakTopics;

    @Builder.Default
    private int questionCount = 10;
}
