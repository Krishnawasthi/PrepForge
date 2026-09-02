package com.prepforge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionRequest {

    private String anonymousSessionId;
    private String attemptId;

    // questionId -> chosen option string
    @NotNull(message = "Answers map cannot be null")
    private Map<String, String> answers;

    private long timeTakenSeconds;
}
