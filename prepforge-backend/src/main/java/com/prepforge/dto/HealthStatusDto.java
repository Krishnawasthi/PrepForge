package com.prepforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusDto {
    private String status;
    private String applicationName;
    private String version;
    private String environment;
    private long uptimeSeconds;
    private Map<String, Object> services;
}
