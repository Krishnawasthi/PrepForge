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
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String question;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private Map<String, String> optionExplanations;

    @Indexed
    private String topic;

    @Indexed
    private String subTopic;

    @Indexed
    private String difficulty;

    @Indexed
    private String experienceLevel;

    @Indexed
    private String questionType;

    private String interviewTip;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
