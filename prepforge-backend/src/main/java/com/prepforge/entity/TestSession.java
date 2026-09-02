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

    private List<String> topics;
    private String experienceLevel;
    private int questionCount;
    private List<String> questionIds;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
