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
public class TopicDto {
    private String id;
    private String name;
    private String slug;
    private String category;
    private String description;
    private String icon;
    private String badgeColor;
    private boolean popular;
    private List<SubTopicDto> subTopics;
}
