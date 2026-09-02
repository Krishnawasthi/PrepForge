package com.prepforge.controller;

import com.prepforge.dto.*;
import com.prepforge.service.PracticeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getTopics() {
        return ResponseEntity.ok(ApiResponse.success(practiceService.getTopics()));
    }

    @PostMapping("/tests")
    public ResponseEntity<ApiResponse<PracticeTestDto>> createPracticeTest(
            @Valid @RequestBody(required = false) CreatePracticeRequest request) {
        if (request == null) request = new CreatePracticeRequest();
        PracticeTestDto test = practiceService.createPracticeTest(request);
        return ResponseEntity.ok(ApiResponse.success("Practice test generated successfully", test));
    }

    @GetMapping("/tests/{testId}")
    public ResponseEntity<ApiResponse<PracticeTestDto>> getPracticeTest(@PathVariable String testId) {
        PracticeTestDto test = practiceService.getPracticeTest(testId);
        return ResponseEntity.ok(ApiResponse.success(test));
    }

    @PostMapping("/tests/{testId}/questions/{questionId}/change")
    public ResponseEntity<ApiResponse<QuestionDto>> changeQuestion(
            @PathVariable String testId,
            @PathVariable String questionId,
            @RequestBody(required = false) QuestionChangeRequest request) {
        QuestionDto replacement = practiceService.changeQuestion(testId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success("Question replaced successfully", replacement));
    }

    @PostMapping("/tests/{testId}/submit")
    public ResponseEntity<ApiResponse<PracticeResultDto>> submitPracticeTest(
            @PathVariable String testId,
            @RequestBody(required = false) SubmitPracticeRequest request) {
        PracticeResultDto result = practiceService.submitPracticeTest(testId, request);
        return ResponseEntity.ok(ApiResponse.success("Test submitted and scored", result));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<PracticeResultDto>> getAttemptResult(@PathVariable String attemptId) {
        PracticeResultDto result = practiceService.getAttemptResult(attemptId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
