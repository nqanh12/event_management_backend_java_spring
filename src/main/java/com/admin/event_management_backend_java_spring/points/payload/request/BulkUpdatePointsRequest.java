package com.admin.event_management_backend_java_spring.points.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BulkUpdatePointsRequest {
    @NotEmpty(message = "User IDs list is required")
    private List<String> userIds;

    @NotNull(message = "Points is required")
    private Integer points;

    @NotNull(message = "Reason is required")
    private String reason;

    private Integer trainingPointsToAdd;
    private Integer socialPointsToAdd;
} 