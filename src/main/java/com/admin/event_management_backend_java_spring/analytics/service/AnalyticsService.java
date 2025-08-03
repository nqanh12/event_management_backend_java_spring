package com.admin.event_management_backend_java_spring.analytics.service;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import com.admin.event_management_backend_java_spring.audit.repository.AuditLogRepository;
import com.admin.event_management_backend_java_spring.analytics.payload.request.AnalyticsRequest;
import com.admin.event_management_backend_java_spring.analytics.payload.response.AnalyticsResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Phân tích xu hướng tham gia sự kiện
     */
    @Cacheable("analytics")
    public ApiResponse<Map<String, Object>> getParticipationTrends(Date startDate, Date endDate) {
        log.info("[ANALYTICS] Phân tích xu hướng tham gia sự kiện từ {} đến {}", startDate, endDate);
        List<Event> events = eventRepository.findAll().stream()
            .filter(e -> e.getStartTime() != null && e.getStartTime().after(startDate) && e.getStartTime().before(endDate))
            .collect(Collectors.toList());

        List<Registration> registrations = registrationRepository.findAll().stream()
            .filter(r -> r.getCheckInTime() != null && r.getCheckInTime().after(startDate) && r.getCheckInTime().before(endDate))
            .collect(Collectors.toList());

        Map<String, Object> trends = new HashMap<>();
        
        // Xu hướng theo tháng
        Map<String, Long> monthlyTrends = events.stream()
            .collect(Collectors.groupingBy(
                e -> formatMonth(e.getStartTime()),
                Collectors.counting()
            ));
        trends.put("monthlyEventTrends", monthlyTrends);

        // Xu hướng tham gia theo loại sự kiện
        Map<String, Long> eventTypeParticipation = registrations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getEvent().getType().name(),
                Collectors.counting()
            ));
        trends.put("eventTypeParticipation", eventTypeParticipation);

        // Tỷ lệ tham gia theo khoa
        Map<String, Double> departmentParticipation = departmentRepository.findAll().stream()
            .collect(Collectors.toMap(
                Department::getName,
                dept -> calculateParticipationRate(dept.getId(), registrations)
            ));
        trends.put("departmentParticipation", departmentParticipation);

        return new ApiResponse<>(true, "Participation trends analyzed", trends);
    }

    /**
     * Phân tích hiệu quả điểm thưởng
     */
    @Cacheable("analytics")
    public ApiResponse<Map<String, Object>> getPointsEffectivenessAnalysis() {
        List<User> users = userRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();

        Map<String, Object> analysis = new HashMap<>();

        // Phân tích điểm theo khoa
        Map<String, Double> avgPointsByDepartment = departmentRepository.findAll().stream()
            .collect(Collectors.toMap(
                Department::getName,
                dept -> calculateAveragePointsByDepartment(dept.getId(), users)
            ));
        analysis.put("averagePointsByDepartment", avgPointsByDepartment);

        // Tương quan giữa điểm và tham gia
        Map<String, Object> correlation = new HashMap<>();
        correlation.put("highPointsUsers", countHighPointsUsers(users));
        correlation.put("activeParticipants", countActiveParticipants(registrations));
        correlation.put("correlationScore", calculateCorrelationScore(users, registrations));
        analysis.put("pointsParticipationCorrelation", correlation);

        return new ApiResponse<>(true, "Points effectiveness analyzed", analysis);
    }

    /**
     * Phân tích ROI của sự kiện
     */
    @Cacheable("analytics")
    public ApiResponse<Map<String, Object>> getEventROIAnalysis() {
        log.info("[ANALYTICS] Phân tích ROI các sự kiện");
        List<Event> events = eventRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();

        Map<String, Object> roi = new HashMap<>();

        // ROI theo loại sự kiện
        Map<String, Double> roiByType = events.stream()
            .collect(Collectors.groupingBy(
                e -> e.getType().name(),
                Collectors.averagingDouble(e -> calculateEventROI(e, registrations))
            ));
        roi.put("roiByEventType", roiByType);

        // Top sự kiện có ROI cao nhất
        List<Map<String, Object>> topROIEvents = events.stream()
            .map(e -> {
                Map<String, Object> eventROI = new HashMap<>();
                eventROI.put("eventId", e.getId());
                eventROI.put("eventName", e.getName());
                eventROI.put("roi", calculateEventROI(e, registrations));
                eventROI.put("participantCount", countEventParticipants(e.getId(), registrations));
                return eventROI;
            })
            .sorted((a, b) -> Double.compare((Double) b.get("roi"), (Double) a.get("roi")))
            .limit(10)
            .collect(Collectors.toList());
        roi.put("topROIEvents", topROIEvents);

        return new ApiResponse<>(true, "Event ROI analyzed", roi);
    }

    /**
     * Phân tích hành vi người dùng
     */
    @Cacheable("analytics")
    public ApiResponse<Map<String, Object>> getUserBehaviorAnalysis() {
        log.info("[ANALYTICS] Phân tích hành vi người dùng");
        List<User> users = userRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();
        List<AuditLog> auditLogs = auditLogRepository.findAll();

        Map<String, Object> behavior = new HashMap<>();

        // Phân tích thời gian hoạt động
        Map<String, Integer> activityByHour = auditLogs.stream()
            .collect(Collectors.groupingBy(
                log -> String.valueOf(new Date(log.getTimestamp().getTime()).getHours()),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        behavior.put("activityByHour", activityByHour);

        // Phân tích thiết bị sử dụng
        Map<String, Long> deviceUsage = auditLogs.stream()
            .filter(log -> log.getUserAgent() != null)
            .collect(Collectors.groupingBy(
                this::extractDeviceType,
                Collectors.counting()
            ));
        behavior.put("deviceUsage", deviceUsage);

        // Phân tích session duration
        Map<String, Object> sessionAnalysis = new HashMap<>();
        sessionAnalysis.put("averageSessionDuration", calculateAverageSessionDuration(auditLogs));
        sessionAnalysis.put("mostActiveUsers", findMostActiveUsers(auditLogs));
        behavior.put("sessionAnalysis", sessionAnalysis);

        return new ApiResponse<>(true, "User behavior analyzed", behavior);
    }

    public AnalyticsResponse getSummary(AnalyticsRequest request) {
        // Dummy implementation, có thể thay bằng logic thực tế
        return new AnalyticsResponse(request.getType(), 100);
    }

    // Helper methods
    private String formatMonth(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(date);
    }

    private double calculateParticipationRate(String departmentId, List<Registration> registrations) {
        long deptRegistrations = registrations.stream()
            .filter(r -> r.getUser().getDepartment() != null && 
                        r.getUser().getDepartment().getId().equals(departmentId))
            .count();
        long totalRegistrations = registrations.size();
        return totalRegistrations > 0 ? (double) deptRegistrations / totalRegistrations * 100 : 0;
    }

    private double calculateAveragePointsByDepartment(String departmentId, List<User> users) {
        return users.stream()
            .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(departmentId))
            .mapToInt(u -> {
                // Tính tổng training points từ 8 học kỳ
                int totalTrainingPoints = 0;
                if (u.getTrainingPoints1() != null) totalTrainingPoints += u.getTrainingPoints1().intValue();
                if (u.getTrainingPoints2() != null) totalTrainingPoints += u.getTrainingPoints2().intValue();
                if (u.getTrainingPoints3() != null) totalTrainingPoints += u.getTrainingPoints3().intValue();
                if (u.getTrainingPoints4() != null) totalTrainingPoints += u.getTrainingPoints4().intValue();
                if (u.getTrainingPoints5() != null) totalTrainingPoints += u.getTrainingPoints5().intValue();
                if (u.getTrainingPoints6() != null) totalTrainingPoints += u.getTrainingPoints6().intValue();
                if (u.getTrainingPoints7() != null) totalTrainingPoints += u.getTrainingPoints7().intValue();
                if (u.getTrainingPoints8() != null) totalTrainingPoints += u.getTrainingPoints8().intValue();
                
                int socialPoints = u.getSocialPoints() != null ? u.getSocialPoints().intValue() : 0;
                return totalTrainingPoints + socialPoints;
            })
            .average()
            .orElse(0.0);
    }

    private int countHighPointsUsers(List<User> users) {
        return (int) users.stream()
            .filter(u -> {
                // Tính tổng training points từ 8 học kỳ
                int totalTrainingPoints = 0;
                if (u.getTrainingPoints1() != null) totalTrainingPoints += u.getTrainingPoints1().intValue();
                if (u.getTrainingPoints2() != null) totalTrainingPoints += u.getTrainingPoints2().intValue();
                if (u.getTrainingPoints3() != null) totalTrainingPoints += u.getTrainingPoints3().intValue();
                if (u.getTrainingPoints4() != null) totalTrainingPoints += u.getTrainingPoints4().intValue();
                if (u.getTrainingPoints5() != null) totalTrainingPoints += u.getTrainingPoints5().intValue();
                if (u.getTrainingPoints6() != null) totalTrainingPoints += u.getTrainingPoints6().intValue();
                if (u.getTrainingPoints7() != null) totalTrainingPoints += u.getTrainingPoints7().intValue();
                if (u.getTrainingPoints8() != null) totalTrainingPoints += u.getTrainingPoints8().intValue();
                
                int socialPoints = u.getSocialPoints() != null ? u.getSocialPoints().intValue() : 0;
                return totalTrainingPoints + socialPoints > 100;
            })
            .count();
    }

    private int countActiveParticipants(List<Registration> registrations) {
        return (int) registrations.stream()
            .filter(r -> r.getCheckInTime() != null && r.getCheckOutTime() != null)
            .count();
    }

    private double calculateCorrelationScore(List<User> users, List<Registration> registrations) {
        // Simplified correlation calculation
        double avgPoints = users.stream()
            .mapToInt(u -> {
                // Tính tổng training points từ 8 học kỳ
                int totalTrainingPoints = 0;
                if (u.getTrainingPoints1() != null) totalTrainingPoints += u.getTrainingPoints1().intValue();
                if (u.getTrainingPoints2() != null) totalTrainingPoints += u.getTrainingPoints2().intValue();
                if (u.getTrainingPoints3() != null) totalTrainingPoints += u.getTrainingPoints3().intValue();
                if (u.getTrainingPoints4() != null) totalTrainingPoints += u.getTrainingPoints4().intValue();
                if (u.getTrainingPoints5() != null) totalTrainingPoints += u.getTrainingPoints5().intValue();
                if (u.getTrainingPoints6() != null) totalTrainingPoints += u.getTrainingPoints6().intValue();
                if (u.getTrainingPoints7() != null) totalTrainingPoints += u.getTrainingPoints7().intValue();
                if (u.getTrainingPoints8() != null) totalTrainingPoints += u.getTrainingPoints8().intValue();
                
                int socialPoints = u.getSocialPoints() != null ? u.getSocialPoints().intValue() : 0;
                return totalTrainingPoints + socialPoints;
            })
            .average()
            .orElse(0.0);
        
        double avgRegistrations = (double) registrations.size() / users.size();
        
        return avgPoints > 0 && avgRegistrations > 0 ? Math.min(avgPoints / avgRegistrations, 1.0) : 0.0;
    }

    private double calculateEventROI(Event event, List<Registration> registrations) {
        int participants = countEventParticipants(event.getId(), registrations);
        int totalPoints = participants * (event.getTrainingPointsReward() != null ? event.getTrainingPointsReward() : 10);
        return participants > 0 ? (double) totalPoints / participants : 0;
    }

    private int countEventParticipants(String eventId, List<Registration> registrations) {
        return (int) registrations.stream()
            .filter(r -> r.getEvent().getId().equals(eventId) && r.getCheckInTime() != null)
            .count();
    }

    private String extractDeviceType(AuditLog log) {
        String userAgent = log.getUserAgent();
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Mobile")) return "Mobile";
        if (userAgent.contains("Tablet")) return "Tablet";
        return "Desktop";
    }

    private double calculateAverageSessionDuration(List<AuditLog> auditLogs) {
        // Simplified session duration calculation
        return auditLogs.stream()
            .collect(Collectors.groupingBy(AuditLog::getSessionId))
            .values()
            .stream()
            .mapToLong(session -> session.size())
            .average()
            .orElse(0.0);
    }

    private List<String> findMostActiveUsers(List<AuditLog> auditLogs) {
        return auditLogs.stream()
            .collect(Collectors.groupingBy(AuditLog::getUserId, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
} 