package com.prepforge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tests")
public class TestSession {

    @Id
    private String id;

    @Indexed
    private String testId;

    @Indexed
    private String anonymousSessionId;

    private String title;
    private String promptDescription;
    private List<String> topics;
    private List<String> subTopics;
    private String experienceLevel;
    private String difficulty;
    private List<String> questionTypes;
    private int questionCount;
    private int timeLimitMinutes;
    private List<String> questionIds;

    @Builder.Default
    private Instant createdAt = Instant.now();

    // Auto-expire anonymous sessions after 7 days using MongoDB TTL index
    @Indexed(name = "test_session_ttl_idx", expireAfter = "7d")
    private Instant expiresAt;
}
