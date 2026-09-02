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
public class QuestionReplaceRequest {

    private String anonymousSessionId;
    private String topic;
    private String subTopic;
    private String concept;
    private String difficulty;
    private String experienceLevel;
    private String questionType;
    private List<String> previouslyUsedQuestions;
}
