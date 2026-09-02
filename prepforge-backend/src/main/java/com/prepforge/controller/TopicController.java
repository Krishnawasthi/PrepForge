package com.prepforge.controller;

import com.prepforge.dto.ApiResponse;
import com.prepforge.dto.TopicDto;
import com.prepforge.exception.ResourceNotFoundException;
import com.prepforge.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicDto>>> getAllTopics(
            @RequestParam(required = false) String category) {
        List<TopicDto> topics = category != null && !category.isBlank()
                ? topicService.getTopicsByCategory(category)
                : topicService.getAllTopics();
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved topics catalog", topics));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<TopicDto>> getTopicBySlug(@PathVariable String slug) {
        TopicDto topic = topicService.getTopicBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with slug: " + slug));
        return ResponseEntity.ok(ApiResponse.success(topic));
    }
}
