package com.prepforge.controller;

import com.prepforge.dto.ApiResponse;
import com.prepforge.dto.HealthStatusDto;
import com.prepforge.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthStatusDto>> checkHealth() {
        HealthStatusDto status = healthService.getHealthStatus();
        return ResponseEntity.ok(ApiResponse.success("PrepForge Backend Service is healthy", status));
    }
}
