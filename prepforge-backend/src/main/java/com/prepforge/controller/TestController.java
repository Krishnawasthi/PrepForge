package com.prepforge.controller;

import com.prepforge.dto.*;
import com.prepforge.entity.TestSession;
import com.prepforge.service.TestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/tests/interpret")
    public ResponseEntity<ApiResponse<PromptInterpretationResponse>> interpretPrompt(
            @Valid @RequestBody PromptInterpretationRequest request) {
        PromptInterpretationResponse interpretation = testService.interpretUserPrompt(request);
        return ResponseEntity.ok(ApiResponse.success("Prompt successfully interpreted", interpretation));
    }

    @PostMapping("/tests/validate")
    public ResponseEntity<ApiResponse<TestSession>> validateTestConfig(
            @Valid @RequestBody TestConfigRequest request) {
        TestSession session = testService.validateAndCreateTestSession(request);
        return ResponseEntity.ok(ApiResponse.success("Test configuration is valid", session));
    }

    @PostMapping("/tests/generate")
    public ResponseEntity<ApiResponse<TestDetailDto>> generateTest(
            @Valid @RequestBody TestConfigRequest request) {
        TestDetailDto testDetail = testService.generateFullTest(request);
        return ResponseEntity.ok(ApiResponse.success("Test successfully generated", testDetail));
    }

    @GetMapping("/tests/{testId}")
    public ResponseEntity<ApiResponse<TestDetailDto>> getTestDetail(@PathVariable String testId) {
        TestDetailDto testDetail = testService.getTestDetail(testId);
        return ResponseEntity.ok(ApiResponse.success(testDetail));
    }

    @PostMapping("/tests/{testId}/questions/{questionId}/replace")
    public ResponseEntity<ApiResponse<QuestionDto>> replaceQuestion(
            @PathVariable String testId,
            @PathVariable String questionId,
            @RequestBody(required = false) QuestionReplaceRequest request) {
        QuestionDto replacement = testService.replaceQuestionInTest(testId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success("Question replaced successfully", replacement));
    }

    @PostMapping("/tests/{testId}/submit")
    public ResponseEntity<ApiResponse<TestResultDto>> submitTest(
            @PathVariable String testId,
            @Valid @RequestBody TestSubmissionRequest request) {
        TestResultDto result = testService.submitTest(testId, request);
        return ResponseEntity.ok(ApiResponse.success("Test submitted and scored successfully", result));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<TestResultDto>> getAttemptResult(@PathVariable String attemptId) {
        TestResultDto result = testService.getAttemptResult(attemptId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/tests/weak-area-practice")
    public ResponseEntity<ApiResponse<TestDetailDto>> generateWeakAreaPractice(
            @Valid @RequestBody WeakAreaPracticeRequest request) {
        TestDetailDto practiceTest = testService.createWeakAreaPracticeTest(request);
        return ResponseEntity.ok(ApiResponse.success("Weak area practice test generated", practiceTest));
    }
}
