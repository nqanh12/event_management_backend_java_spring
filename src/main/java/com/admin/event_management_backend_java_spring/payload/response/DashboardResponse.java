package com.admin.event_management_backend_java_spring.payload.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    // Thống kê tổng quan
    private OverviewStats overviewStats;
    
    // Thống kê theo thời gian
    private TimeSeriesStats timeSeriesStats;
    
    // Thống kê theo department
    private List<DepartmentStats> departmentStats;
    
    // Top performers
    private List<UserStats> topStudents;
    private List<UserStats> topOrganizers;
    
    // Thống kê events
    private EventStats eventStats;
    
    // Thống kê points
    private PointsStats pointsStats;
    
    // Thống kê security
    private SecurityStats securityStats;
    
    // Recent activities
    private List<RecentActivity> recentActivities;
    
    @Data
    public static class OverviewStats {
        private int totalUsers;
        private int totalEvents;
        private int totalRegistrations;
        private int totalDepartments;
        private int totalCourses;
        private int activeEvents;
        private int completedEvents;
        private int pendingRegistrations;
        private double averageEventRating;
    }
    
    @Data
    public static class TimeSeriesStats {
        private List<DataPoint> eventsCreated;
        private List<DataPoint> registrations;
        private List<DataPoint> pointsAwarded;
        private List<DataPoint> userLogins;
    }
    
    @Data
    public static class DataPoint {
        private String date;
        private long value;
        private String label;
    }
    
    @Data
    public static class DepartmentStats {
        private String departmentId;
        private String departmentName;
        private int totalUsers;
        private int totalEvents;
        private int totalRegistrations;
        private int totalPoints;
        private double averagePointsPerUser;
        private double participationRate;
    }
    
    @Data
    public static class UserStats {
        private String userId;
        private String userName;
        private String userEmail;
        private String studentId;
        private String departmentName;
        private String className;
        private int trainingPoints;
        private int socialPoints;
        private int totalEvents;
        private int totalRegistrations;
        private double averageRating;
    }
    
    @Data
    public static class EventStats {
        private int totalEvents;
        private int upcomingEvents;
        private int ongoingEvents;
        private int completedEvents;
        private int cancelledEvents;
        private Map<String, Integer> eventsByType;
        private Map<String, Integer> eventsByStatus;
        private double averageParticipants;
        private double averageRating;
    }
    
    @Data
    public static class PointsStats {
        private int totalTrainingPoints;
        private int totalSocialPoints;
        private int totalPointsAwarded;
        private int totalPointsDeducted;
        private Map<String, Integer> pointsByDepartment;
        private Map<String, Integer> pointsByMonth;
        private double averagePointsPerUser;
        private double averagePointsPerEvent;
    }
    
    @Data
    public static class SecurityStats {
        private int totalLogins;
        private int failedLogins;
        private int suspiciousActivities;
        private int unauthorizedAccess;
        private Map<String, Integer> activitiesByType;
        private List<String> recentAlerts;
    }
    
    @Data
    public static class RecentActivity {
        private String id;
        private String type;
        private String description;
        private String userId;
        private String userName;
        private String timestamp;
        private String status;
        private String resourceType;
        private String resourceId;
    }
} 