package com.prepforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptInterpretationRequest {

    @NotBlank(message = "Prompt description cannot be empty")
    @Size(max = 1000, message = "Prompt cannot exceed 1000 characters (approx. 150-200 words)")
    private String prompt;
}
