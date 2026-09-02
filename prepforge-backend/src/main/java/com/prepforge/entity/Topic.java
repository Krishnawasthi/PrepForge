package com.prepforge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "topics")
public class Topic {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Indexed(unique = true)
    private String slug;

    @Indexed
    private String category;

    private String description;
    private String icon;
    private String badgeColor;
    private boolean popular;
    private List<SubTopic> subTopics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTopic {
        private String id;
        private String name;
        private String description;
    }
}
