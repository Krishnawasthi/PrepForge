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
public class TestSession {

    private String id;
    private String testId;
    private List<String> topics;
    private String experienceLevel;
    private int questionCount;
    private List<String> questionIds;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
