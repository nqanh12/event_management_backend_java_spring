package com.admin.event_management_backend_java_spring.department.payload.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    private String name;
    
    @NotNull(message = "Department type is required")
    private String type; // FACULTY, SCHOOL
    
    // Penalty points configuration
    @Min(value = 0, message = "Training points penalty must be non-negative")
    private Integer trainingPointsPenalty;
    
    @Min(value = 0, message = "Social points penalty must be non-negative")
    private Integer socialPointsPenalty;
} 