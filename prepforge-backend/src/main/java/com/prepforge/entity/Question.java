package com.prepforge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    private String id;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String topic;
    private String difficulty;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
