package com.prepforge.service;

import com.prepforge.dto.HealthStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class HealthService {

    private final MongoTemplate mongoTemplate;

    @Value("${spring.application.name:prepforge-backend}")
    private String applicationName;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String aiModel;

    public HealthService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public HealthStatusDto getHealthStatus() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Map<String, Object> services = new HashMap<>();

        // Check MongoDB connectivity safely
        boolean mongoHealthy = false;
        try {
            mongoHealthy = mongoTemplate.getDb() != null;
            services.put("mongodb", Map.of(
                "status", mongoHealthy ? "UP" : "DOWN",
                "database", mongoTemplate.getDb().getName()
            ));
        } catch (Exception e) {
            services.put("mongodb", Map.of(
                "status", "DOWN",
                "error", e.getMessage() != null ? e.getMessage() : "Connection failed"
            ));
        }

        // AI Provider status metadata
        services.put("aiProvider", Map.of(
            "provider", "Google Gemini",
            "model", aiModel,
            "status", "CONFIGURED"
        ));

        return HealthStatusDto.builder()
                .status("UP")
                .applicationName(applicationName)
                .version("1.0.0")
                .environment("production-ready")
                .uptimeSeconds(uptimeMs / 1000)
                .services(services)
                .build();
    }
}
