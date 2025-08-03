package com.admin.event_management_backend_java_spring.user.payload.response;

import lombok.Data;
import java.util.Map;

@Data
public class UserPointsResponse {
    private String userId;
    private String userEmail;
    private String fullName;
    
    // Training Points cho từng kỳ
    private Map<String, Double> trainingPointsBySemester;
    
    // Social Points tích lũy
    private Double socialPoints;
    
    // Thống kê tổng quan
    private Double averageTrainingPoints;
    private Double totalTrainingPoints;
    private Double totalPoints;
    
    // Thông tin kỳ học hiện tại
    private String currentSemester;
    private Double currentSemesterPoints;
} 