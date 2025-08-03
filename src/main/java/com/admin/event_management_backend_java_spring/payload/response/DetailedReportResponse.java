package com.admin.event_management_backend_java_spring.payload.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DetailedReportResponse {
    private EventReport eventReport;
    private UserReport userReport;
    private PointsReport pointsReport;
    private DepartmentReport departmentReport;
    private ParticipationReport participationReport;
    private SecurityReport securityReport;
    
    @Data
    public static class EventReport {
        private int totalEvents;
        private int approvedEvents;
        private int ongoingEvents;
        private int completedEvents;
        private int cancelledEvents;
        private Map<String, Integer> eventsByType;
        private Map<String, Integer> eventsByMonth;
        private Map<String, Integer> eventsByDepartment;
        private List<EventDetail> topEvents;
        private double averageParticipantsPerEvent;
        private double averageEventDuration;
    }
    
    @Data
    public static class EventDetail {
        private String eventId;
        private String eventName;
        private String eventType;
        private String departmentName;
        private int participantCount;
        private double averageRating;
        private String status;
    }
    
    @Data
    public static class UserReport {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private Map<String, Integer> usersByRole;
        private Map<String, Integer> usersByDepartment;
        private List<UserDetail> topActiveUsers;
        private List<UserDetail> topPointEarners;
        private double averagePointsPerUser;
    }
    
    @Data
    public static class UserDetail {
        private String userId;
        private String userName;
        private String userEmail;
        private String userRole;
        private String departmentName;
        private int trainingPoints;
        private int socialPoints;
        private int totalEvents;
        private int totalRegistrations;
    }
    
    @Data
    public static class PointsReport {
        private int totalTrainingPoints;
        private int totalSocialPoints;
        private int totalPointsAwarded;
        private int totalPointsDeducted;
        private Map<String, Integer> pointsByDepartment;
        private Map<String, Integer> pointsByMonth;
        private Map<String, Integer> pointsByEventType;
        private List<PointsDetail> topPointEarners;
        private double averagePointsPerEvent;
        private double averagePointsPerUser;
    }
    
    @Data
    public static class PointsDetail {
        private String userId;
        private String userName;
        private String departmentName;
        private int trainingPoints;
        private int socialPoints;
        private int totalPoints;
        private int eventsAttended;
    }
    
    @Data
    public static class DepartmentReport {
        private int totalDepartments;
        private List<DepartmentDetail> departmentDetails;
        private Map<String, Integer> eventsByDepartment;
        private Map<String, Integer> usersByDepartment;
        private Map<String, Integer> pointsByDepartment;
    }
    
    @Data
    public static class DepartmentDetail {
        private String departmentId;
        private String departmentName;
        private String departmentType;
        private int totalUsers;
        private int totalEvents;
        private int totalRegistrations;
        private int totalPoints;
        private double averagePointsPerUser;
        private double participationRate;
    }
    
    @Data
    public static class ParticipationReport {
        private int totalRegistrations;
        private int attendedRegistrations;
        private int absentRegistrations;
        private int cancelledRegistrations;
        private Map<String, Integer> registrationsByStatus;
        private Map<String, Integer> registrationsByMonth;
        private Map<String, Integer> registrationsByDepartment;
        private double overallAttendanceRate;
        private List<ParticipationDetail> topParticipatingUsers;
    }
    
    @Data
    public static class ParticipationDetail {
        private String userId;
        private String userName;
        private String departmentName;
        private int totalRegistrations;
        private int attendedEvents;
        private int absentEvents;
        private double attendanceRate;
    }
    
    @Data
    public static class SecurityReport {
        private int totalLogins;
        private int failedLogins;
        private int suspiciousActivities;
        private int unauthorizedAccess;
        private Map<String, Integer> activitiesByType;
        private Map<String, Integer> activitiesByUser;
        private Map<String, Integer> activitiesByHour;
        private List<SecurityDetail> recentSecurityEvents;
        private double successRate;
    }
    
    @Data
    public static class SecurityDetail {
        private String activityId;
        private String activityType;
        private String userId;
        private String userEmail;
        private String ipAddress;
        private String description;
        private String status;
        private String timestamp;
    }
} 