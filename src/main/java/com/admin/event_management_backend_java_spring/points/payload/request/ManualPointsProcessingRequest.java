package com.admin.event_management_backend_java_spring.points.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ManualPointsProcessingRequest {
    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotNull(message = "User IDs are required")
    private List<String> userIds;

    @NotNull(message = "Action is required")
    private Action action;

    private Integer customPoints;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;

    // Enum cho action
    public enum Action {
        AWARD, PENALIZE, IGNORE
    }
} 