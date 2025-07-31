package com.admin.event_management_backend_java_spring.analytics.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyticsRequest {
    @NotBlank(message = "Type is required")
    private String type;
} 