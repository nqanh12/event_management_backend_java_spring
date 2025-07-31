package com.admin.event_management_backend_java_spring.dashboard.service;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.response.DashboardResponse;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import com.admin.event_management_backend_java_spring.audit.repository.AuditLogRepository;
import com.admin.event_management_backend_java_spring.points.repository.PointsHistoryRepository;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory.PointsType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationSpELExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PointsHistoryRepository pointsHistoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Lấy dashboard tổng hợp
     */
    @Cacheable(value = "dashboard", key = "'dashboard'", unless = "#result == null", cacheManager = "cacheManager")
    public ApiResponse<DashboardResponse> getDashboard() {
        try {
            DashboardResponse dashboard = new DashboardResponse();

            // Overview stats
            dashboard.setOverviewStats(buildOverviewStats());

            // Time series stats
            dashboard.setTimeSeriesStats(buildTimeSeriesStats());

            // Department stats
            dashboard.setDepartmentStats(buildDepartmentStats());

            // Top performers
            dashboard.setTopStudents(buildTopStudents());
            dashboard.setTopOrganizers(buildTopOrganizers());

            // Event stats
            dashboard.setEventStats(buildEventStats());

            // Points stats
            dashboard.setPointsStats(buildPointsStats());

            // Security stats
            dashboard.setSecurityStats(buildSecurityStats());

            // Recent activities
            dashboard.setRecentActivities(buildRecentActivities());

            return new ApiResponse<>(true, "Dashboard data retrieved successfully", dashboard);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Error retrieving dashboard data: " + e.getMessage(), null);
        }
    }

    private DashboardResponse.OverviewStats buildOverviewStats() {
        DashboardResponse.OverviewStats stats = new DashboardResponse.OverviewStats();

        // Sử dụng count thay vì findAll để tối ưu performance
        stats.setTotalUsers((int) userRepository.count());
        stats.setTotalEvents((int) eventRepository.count());
        stats.setTotalRegistrations((int) registrationRepository.count());
        stats.setTotalDepartments((int) departmentRepository.count());

        // Count theo status thay vì load tất cả
        stats.setActiveEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
        stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
        stats.setPendingRegistrations((int) registrationRepository.countByStatus(Registration.RegistrationStatus.REGISTERED));

        // Tính average rating từ database thay vì load tất cả
        stats.setAverageEventRating(calculateAverageEventRating());

        return stats;
    }

    private double calculateAverageEventRating() {
        // TODO: Implement calculation from database
        // Có thể sử dụng aggregation pipeline của MongoDB
        return 4.5; // Placeholder
    }

    private DashboardResponse.TimeSeriesStats buildTimeSeriesStats() {
        DashboardResponse.TimeSeriesStats stats = new DashboardResponse.TimeSeriesStats();

        // Get last 30 days data
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = cal.getTime();

        List<Event> events = eventRepository.findAll().stream()
            .filter(e -> e.getStartTime() != null && e.getStartTime().after(startDate))
            .collect(Collectors.toList());

        List<Registration> registrations = registrationRepository.findAll().stream()
            .filter(r -> r.getCheckInTime() != null && r.getCheckInTime().after(startDate))
            .collect(Collectors.toList());

        List<AuditLog> auditLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        // Build time series data
        stats.setEventsCreated(buildTimeSeriesData(events, Event::getStartTime, "Events Created"));
        stats.setRegistrations(buildTimeSeriesData(registrations, Registration::getCheckInTime, "Registrations"));
        stats.setUserLogins(buildTimeSeriesData(auditLogs.stream()
            .filter(log -> "USER_LOGIN".equals(log.getAction())).collect(Collectors.toList()),
            AuditLog::getTimestamp, "User Logins"));

        // Add points awarded time series (placeholder)
        stats.setPointsAwarded(new ArrayList<>());

        return stats;
    }

    private <T> List<DashboardResponse.DataPoint> buildTimeSeriesData(List<T> items,
                                                                     java.util.function.Function<T, Date> dateExtractor,
                                                                     String label) {
        Map<String, Long> dailyCounts = items.stream()
            .filter(item -> dateExtractor.apply(item) != null)
            .collect(Collectors.groupingBy(
                item -> formatDate(dateExtractor.apply(item)),
                Collectors.counting()
            ));

        return dailyCounts.entrySet().stream()
            .map(entry -> {
                DashboardResponse.DataPoint point = new DashboardResponse.DataPoint();
                point.setDate(entry.getKey());
                point.setValue(entry.getValue());
                point.setLabel(label);
                return point;
            })
            .sorted(Comparator.comparing(DashboardResponse.DataPoint::getDate))
            .collect(Collectors.toList());
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private List<DashboardResponse.DepartmentStats> buildDepartmentStats() {
        List<Department> departments = departmentRepository.findAll();
        List<DashboardResponse.DepartmentStats> departmentStats = new ArrayList<>();

        for (Department dept : departments) {
            DashboardResponse.DepartmentStats stats = new DashboardResponse.DepartmentStats();
            stats.setDepartmentId(dept.getId());
            stats.setDepartmentName(dept.getName());

            // Sử dụng count thay vì stream filter
            stats.setTotalUsers((int) userRepository.countByDepartmentId(dept.getId()));
            stats.setTotalEvents((int) eventRepository.countByDepartmentId(dept.getId()));

            // Tính points từ database thay vì load tất cả users
            stats.setTotalPoints(calculateDepartmentTotalPoints(dept.getId()));
            stats.setAveragePointsPerUser(calculateDepartmentAveragePoints(dept.getId()));
            stats.setParticipationRate(calculateDepartmentParticipationRate(dept.getId()));

            departmentStats.add(stats);
        }

        return departmentStats;
    }

    private int calculateDepartmentTotalPoints(String departmentId) {
        // TODO: Implement aggregation query
        return 0; // Placeholder
    }

    private double calculateDepartmentAveragePoints(String departmentId) {
        // TODO: Implement aggregation query
        return 0.0; // Placeholder
    }

    private double calculateDepartmentParticipationRate(String departmentId) {
        // TODO: Implement aggregation query
        return 0.0; // Placeholder
    }

    private List<DashboardResponse.UserStats> buildTopStudents() {
        // Sử dụng optimized query thay vì load tất cả users
        Pageable pageable = PageRequest.of(0, 10, Sort.by("socialPoints").descending());
        List<User> topStudents = userRepository.findTopStudentsBySocialPoints(pageable);

        return topStudents.stream()
            .map(this::buildUserStats)
            .collect(Collectors.toList());
    }

    private List<DashboardResponse.UserStats> buildTopOrganizers() {
        // Sử dụng pagination thay vì load tất cả
        Pageable pageable = PageRequest.of(0, 10, Sort.by("socialPoints").descending());
        Page<User> organizerPage = userRepository.findByRole(User.UserRole.ORGANIZER, pageable);

        return organizerPage.getContent().stream()
            .map(this::buildUserStats)
            .collect(Collectors.toList());
    }

    private DashboardResponse.UserStats buildUserStats(User user) {
        DashboardResponse.UserStats stats = new DashboardResponse.UserStats();
        stats.setUserId(user.getId());
        stats.setUserName(user.getFullName());
        stats.setUserEmail(user.getEmail());
        stats.setStudentId(user.getStudentId());
        stats.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : "N/A");
        stats.setClassName(user.getClassName());

        // Tính tổng training points từ 8 học kỳ
        int totalTrainingPoints = 0;
        if (user.getTrainingPoints1() != null) totalTrainingPoints += user.getTrainingPoints1().intValue();
        if (user.getTrainingPoints2() != null) totalTrainingPoints += user.getTrainingPoints2().intValue();
        if (user.getTrainingPoints3() != null) totalTrainingPoints += user.getTrainingPoints3().intValue();
        if (user.getTrainingPoints4() != null) totalTrainingPoints += user.getTrainingPoints4().intValue();
        if (user.getTrainingPoints5() != null) totalTrainingPoints += user.getTrainingPoints5().intValue();
        if (user.getTrainingPoints6() != null) totalTrainingPoints += user.getTrainingPoints6().intValue();
        if (user.getTrainingPoints7() != null) totalTrainingPoints += user.getTrainingPoints7().intValue();
        if (user.getTrainingPoints8() != null) totalTrainingPoints += user.getTrainingPoints8().intValue();

        stats.setTrainingPoints(totalTrainingPoints);
        stats.setSocialPoints(user.getSocialPoints() != null ? user.getSocialPoints().intValue() : 0);

        // Count user's events and registrations
        List<Event> userEvents = eventRepository.findAll().stream()
            .filter(e -> e.getOrganizer() != null && e.getOrganizer().getId().equals(user.getId()))
            .collect(Collectors.toList());
        stats.setTotalEvents(userEvents.size());

        List<Registration> userRegistrations = registrationRepository.findAll().stream()
            .filter(r -> r.getUser() != null && r.getUser().getId().equals(user.getId()))
            .collect(Collectors.toList());
        stats.setTotalRegistrations(userRegistrations.size());

        // Average rating from feedbacks (placeholder - Feedback model doesn't have rating field)
        stats.setAverageRating(0.0);

        return stats;
    }

    private DashboardResponse.EventStats buildEventStats() {
        DashboardResponse.EventStats stats = new DashboardResponse.EventStats();

        // Sử dụng count thay vì findAll
        stats.setTotalEvents((int) eventRepository.count());
        stats.setUpcomingEvents((int) eventRepository.countByStatus(Event.EventStatus.APPROVED));
        stats.setOngoingEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
        stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
        stats.setCancelledEvents((int) eventRepository.countByStatus(Event.EventStatus.CANCELLED));

        // Build maps từ database queries
        stats.setEventsByType(buildEventsByTypeMap());
        stats.setEventsByStatus(buildEventsByStatusMap());

        stats.setAverageParticipants(calculateAverageParticipants());
        stats.setAverageRating(calculateAverageEventRating());

        return stats;
    }

    private Map<String, Integer> buildEventsByTypeMap() {
        Map<String, Integer> eventsByType = new HashMap<>();
        eventsByType.put("TRAINING", (int) eventRepository.countByType(Event.EventType.TRAINING));
        eventsByType.put("SOCIAL", (int) eventRepository.countByType(Event.EventType.SOCIAL));
        return eventsByType;
    }

    private Map<String, Integer> buildEventsByStatusMap() {
        Map<String, Integer> eventsByStatus = new HashMap<>();
        eventsByStatus.put("PENDING", (int) eventRepository.countByStatus(Event.EventStatus.PENDING));
        eventsByStatus.put("APPROVED", (int) eventRepository.countByStatus(Event.EventStatus.APPROVED));
        eventsByStatus.put("ONGOING", (int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
        eventsByStatus.put("COMPLETED", (int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
        eventsByStatus.put("CANCELLED", (int) eventRepository.countByStatus(Event.EventStatus.CANCELLED));
        return eventsByStatus;
    }

    private double calculateAverageParticipants() {
        // TODO: Implement aggregation query
        return 25.0; // Placeholder
    }

    private DashboardResponse.PointsStats buildPointsStats() {
        DashboardResponse.PointsStats stats = new DashboardResponse.PointsStats();

        // TODO: Implement aggregation queries for better performance
        stats.setTotalTrainingPoints(calculateTotalTrainingPoints());
        stats.setTotalSocialPoints(calculateTotalSocialPoints());
        stats.setTotalPointsAwarded(calculateTotalPointsAwarded());
        stats.setTotalPointsDeducted(calculateTotalPointsDeducted());

        stats.setPointsByDepartment(buildPointsByDepartmentMap());
        stats.setPointsByMonth(buildPointsByMonthMap());

        stats.setAveragePointsPerUser(calculateAveragePointsPerUser());
        stats.setAveragePointsPerEvent(calculateAveragePointsPerEvent());

        return stats;
    }

    private int calculateTotalTrainingPoints() {
        // Tổng điểm rèn luyện của tất cả user (cộng 8 trường trainingPoints1-8)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.project()
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8").as("totalTrainingPoints"),
            Aggregation.group().sum("totalTrainingPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getInteger("sum", 0) : 0;
    }

    private int calculateTotalSocialPoints() {
        // Tổng điểm CTXH của tất cả user
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.group().sum("socialPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getInteger("sum", 0) : 0;
    }

    private int calculateTotalPointsAwarded() {
        // Tổng điểm đã cộng (từ PointsHistory, chỉ tính điểm dương)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("pointsChange").gt(0)),
            Aggregation.group().sum("pointsChange").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getInteger("sum", 0) : 0;
    }

    private int calculateTotalPointsDeducted() {
        // Tổng điểm đã trừ (từ PointsHistory, chỉ tính điểm âm)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("pointsChange").lt(0)),
            Aggregation.group().sum("pointsChange").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? Math.abs(doc.getInteger("sum", 0)) : 0;
    }

    private Map<String, Integer> buildPointsByDepartmentMap() {
        // Tổng điểm rèn luyện + CTXH theo phòng ban
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.project()
                .and("department.name").as("departmentName")
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8 + socialPoints").as("totalPoints"),
            Aggregation.group("departmentName").sum("totalPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        Map<String, Integer> map = new HashMap<>();
        for (org.bson.Document doc : results) {
            map.put(doc.getString("_id"), doc.getInteger("sum", 0));
        }
        return map;
    }

    private Map<String, Integer> buildPointsByMonthMap() {
        // Tổng điểm cộng theo tháng (từ PointsHistory, chỉ tính điểm dương)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("pointsChange").gt(0)),
            Aggregation.project()
                .andExpression("year(changedAt)").as("year")
                .andExpression("month(changedAt)").as("month")
                .and("pointsChange").as("pointsChange"),
            Aggregation.group("year", "month").sum("pointsChange").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        Map<String, Integer> map = new HashMap<>();
        for (org.bson.Document doc : results) {
            String key = doc.getInteger("year") + "-" + String.format("%02d", doc.getInteger("month"));
            map.put(key, doc.getInteger("sum", 0));
        }
        return map;
    }

    private double calculateAveragePointsPerUser() {
        // Trung bình tổng điểm (rèn luyện + CTXH) trên mỗi user
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.project()
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8 + socialPoints").as("totalPoints"),
            Aggregation.group().avg("totalPoints").as("avg")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getDouble("avg") : 0.0;
    }

    private double calculateAveragePointsPerEvent() {
        // Trung bình tổng điểm cộng trên mỗi sự kiện (từ PointsHistory, chỉ tính điểm dương)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("pointsChange").gt(0)),
            Aggregation.group("eventId").sum("pointsChange").as("sum"),
            Aggregation.group().avg("sum").as("avg")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getDouble("avg") : 0.0;
    }

    private DashboardResponse.SecurityStats buildSecurityStats() {
        DashboardResponse.SecurityStats stats = new DashboardResponse.SecurityStats();

        // Sử dụng count thay vì load tất cả audit logs
        stats.setTotalLogins((int) auditLogRepository.countByAction("USER_LOGIN"));
        stats.setFailedLogins((int) auditLogRepository.countByAction("LOGIN_FAILED"));
        stats.setSuspiciousActivities((int) auditLogRepository.countByAction("SUSPICIOUS_ACTIVITY"));
        stats.setUnauthorizedAccess((int) auditLogRepository.countByAction("UNAUTHORIZED_ACCESS"));

        stats.setActivitiesByType(buildActivitiesByTypeMap());
        stats.setRecentAlerts(buildRecentAlerts());

        return stats;
    }

    private Map<String, Integer> buildActivitiesByTypeMap() {
        Map<String, Integer> activitiesByType = new HashMap<>();
        activitiesByType.put("LOGIN", (int) auditLogRepository.countByAction("USER_LOGIN"));
        activitiesByType.put("LOGOUT", (int) auditLogRepository.countByAction("USER_LOGOUT"));
        activitiesByType.put("EVENT_CREATE", (int) auditLogRepository.countByAction("EVENT_CREATE"));
        activitiesByType.put("EVENT_UPDATE", (int) auditLogRepository.countByAction("EVENT_UPDATE"));
        activitiesByType.put("POINTS_UPDATE", (int) auditLogRepository.countByAction("POINTS_AWARD"));
        return activitiesByType;
    }

    private List<String> buildRecentAlerts() {
        // TODO: Implement recent alerts logic
        return new ArrayList<>();
    }

    private List<DashboardResponse.RecentActivity> buildRecentActivities() {
        // Sử dụng pagination thay vì load tất cả audit logs
        Pageable pageable = PageRequest.of(0, 20, Sort.by("timestamp").descending());
        Page<AuditLog> recentLogs = auditLogRepository.findRecentActivities(pageable);

        return recentLogs.getContent().stream()
            .map(this::buildRecentActivity)
            .collect(Collectors.toList());
    }

    private DashboardResponse.RecentActivity buildRecentActivity(AuditLog log) {
        DashboardResponse.RecentActivity activity = new DashboardResponse.RecentActivity();
        activity.setId(log.getId());
        activity.setType(log.getAction());
        activity.setDescription(log.getDescription());
        activity.setUserId(log.getUserId());
        activity.setUserName(log.getUserEmail()); // Using email as username for now
        activity.setTimestamp(log.getTimestamp().toString());
        activity.setStatus(log.getStatus());
        activity.setResourceType(log.getResourceType());
        activity.setResourceId(log.getResourceId());
        return activity;
    }

    // Khi có thay đổi dữ liệu liên quan, clear cache
    @CacheEvict(value = "dashboard", key = "'dashboard'", cacheManager = "cacheManager")
    public void clearDashboardCache() {}
}
