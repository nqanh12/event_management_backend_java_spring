package com.admin.event_management_backend_java_spring.user.payload.response;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String studentId;
    private String role;
    private String departmentName;
    private String className;
    private Integer cohort;
    private String academicYear;
    private Integer currentYear;
    private int trainingPoints;
    private int socialPoints;
} 