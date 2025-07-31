package com.admin.event_management_backend_java_spring.analytics.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {
    private String type;
    private int value;
} 