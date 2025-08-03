package com.admin.event_management_backend_java_spring.department.payload.response;

import lombok.Data;
import java.util.Date;

@Data
public class DepartmentResponse {
    private String id;
    private String name;
    private String type;
    
    // Penalty points configuration
    private Integer trainingPointsPenalty;
    private Integer socialPointsPenalty;
    
    
    // Audit fields
    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;
} 