package com.admin.event_management_backend_java_spring.event.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class EventRequest {
    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "Event type is required")
    private String type; // TRAINING, SOCIAL

    @NotNull(message = "Start time is required")
    private Date startTime;

    @NotNull(message = "End time is required")
    private Date endTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Department ID is required")
    private String departmentId;

    private String courseId;

    @NotNull(message = "Max participants is required")
    private Integer maxParticipants;

    private String note;
    private Boolean allowCancelRegistration;
    private List<Integer> allowedCohorts;
    private Boolean allowAllCohorts = true;
    private String scope;
} 