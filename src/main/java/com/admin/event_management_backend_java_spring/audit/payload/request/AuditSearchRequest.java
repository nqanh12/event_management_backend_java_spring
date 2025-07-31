package com.admin.event_management_backend_java_spring.audit.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditSearchRequest {
    private String userId;

    private Long startDate;

    private Long endDate;

    private String actionType;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String eventId;
    private String departmentId;
    private String keyword;
    private Integer page = 0;
    private Integer size = 20;
}
