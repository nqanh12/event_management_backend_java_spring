package com.admin.event_management_backend_java_spring.department.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDepartmentPenaltyRequest {
    @NotNull(message = "Training points penalty is required")
    @Min(value = 0, message = "Training points penalty must be non-negative")
    private Integer trainingPointsPenalty;

    @NotNull(message = "Social points penalty is required")
    @Min(value = 0, message = "Social points penalty must be non-negative")
    private Integer socialPointsPenalty;
} 