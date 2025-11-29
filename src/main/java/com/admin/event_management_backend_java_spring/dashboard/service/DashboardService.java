package com.admin.event_management_backend_java_spring.dashboard.service;

import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import com.admin.event_management_backend_java_spring.audit.repository.AuditLogRepository;
import com.admin.event_management_backend_java_spring.dashboard.payload.request.DashboardFilterRequest;
import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.response.DashboardResponse;
import com.admin.event_management_backend_java_spring.feedback.repository.FeedbackRepository;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import com.admin.event_management_backend_java_spring.points.repository.PointsHistoryRepository;
import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Pageable.unpaged;

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
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Lấy dashboard tổng hợp
     */
    @Cacheable(value = "dashboard", key = "'dashboard'", unless = "#result == null", cacheManager = "cacheManager")
    public ApiResponse<DashboardResponse> getDashboard() {
        DashboardFilterRequest defaultFilter = new DashboardFilterRequest();
        defaultFilter.setPreset(DashboardFilterRequest.DatePreset.LAST_30_DAYS);
        defaultFilter.calculateDatesFromPreset();
        return getDashboard(defaultFilter);
    }

    /**
     * Lấy dashboard tổng hợp với bộ lọc theo ngày tháng năm
     */
    public ApiResponse<DashboardResponse> getDashboard(DashboardFilterRequest filter) {
        try {
            DashboardResponse dashboard = new DashboardResponse();

            // Overview stats
            dashboard.setOverviewStats(buildOverviewStats(filter));

            // Time series stats
            dashboard.setTimeSeriesStats(buildTimeSeriesStats(filter));

            // Department stats
            dashboard.setDepartmentStats(buildDepartmentStats(filter));

            // Top performers
            dashboard.setTopStudents(buildTopStudents());
            dashboard.setTopOrganizers(buildTopOrganizers());

            // Event stats
            dashboard.setEventStats(buildEventStats(filter));

            // Points stats
            dashboard.setPointsStats(buildPointsStats(filter));

            // Security stats
            dashboard.setSecurityStats(buildSecurityStats(filter));

            // Recent activities
            dashboard.setRecentActivities(buildRecentActivities());

            return new ApiResponse<>(true, "Dashboard data retrieved successfully", dashboard);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Error retrieving dashboard data: " + e.getMessage(), null);
        }
    }

    private DashboardResponse.OverviewStats buildOverviewStats(DashboardFilterRequest filter) {
        DashboardResponse.OverviewStats stats = new DashboardResponse.OverviewStats();

        // Nếu có date filter, chỉ tính các thống kê trong khoảng thời gian đó
        if (filter != null && filter.hasDateFilter()) {
            // Count events trong date range
            stats.setTotalEvents((int) eventRepository.countByStartTimeBetween(filter.getStartDate(), filter.getEndDate()));
            stats.setActiveEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
            stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));

            // Count registrations trong date range
            stats.setTotalRegistrations((int) registrationRepository.countByCheckInTimeBetween(filter.getStartDate(), filter.getEndDate()));
        } else {
            // Sử dụng count thay vì findAll để tối ưu performance (tất cả dữ liệu)
            stats.setTotalEvents((int) eventRepository.count());
            stats.setActiveEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
            stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
            stats.setTotalRegistrations((int) registrationRepository.count());
        }

        // Các stats không phụ thuộc vào date filter
        stats.setTotalUsers((int) userRepository.count());
        stats.setTotalDepartments((int) departmentRepository.count());
        stats.setPendingRegistrations((int) registrationRepository.countByStatus(Registration.RegistrationStatus.REGISTERED));

        // Tính average rating từ database thay vì load tất cả
        stats.setAverageEventRating(calculateAverageEventRating());

        return stats;
    }

    private double calculateAverageEventRating() {
        // Sử dụng aggregation pipeline để tính rating trung bình từ feedbacks
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("rating").ne(null)),
            Aggregation.group().avg("rating").as("avgRating")
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "feedbacks", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        
        if (doc != null) {
            Double avgRating = doc.getDouble("avgRating");
            return avgRating != null ? avgRating : 0.0;
        }
        return 0.0;
    }

        private DashboardResponse.TimeSeriesStats buildTimeSeriesStats(DashboardFilterRequest filter) {
            DashboardResponse.TimeSeriesStats stats = new DashboardResponse.TimeSeriesStats();

            Date startDate;
            Date endDate;

            if (filter != null && filter.hasDateFilter()) {
                startDate = filter.getStartDate();
                endDate = filter.getEndDate();
            } else {
                // Default: last 30 days
                Calendar cal = Calendar.getInstance();
                endDate = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, -30);
                startDate = cal.getTime();
            }

            // Sử dụng date range queries thay vì findAll() + filter trong memory
            List<Event> events = eventRepository.findByStartTimeBetween(startDate, endDate, unpaged()).getContent();

            List<Registration> registrations = registrationRepository.findByCheckInTimeBetween(startDate, endDate);

            List<AuditLog> auditLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

            // Build time series data
            stats.setEventsCreated(buildTimeSeriesData(events, Event::getStartTime, "Events Created"));
            stats.setRegistrations(buildTimeSeriesData(registrations, Registration::getCheckInTime, "Registrations"));
            stats.setUserLogins(buildTimeSeriesData(auditLogs.stream()
                .filter(log -> "USER_LOGIN".equals(log.getAction())).collect(Collectors.toList()),
                AuditLog::getTimestamp, "User Logins"));

            // Points awarded time series
            if (filter != null && filter.hasDateFilter()) {
                java.time.LocalDateTime startDateTime = startDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                java.time.LocalDateTime endDateTime = endDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

                List<PointsHistory> pointsHistory = pointsHistoryRepository.findByChangedAtBetween(startDateTime, endDateTime);
                stats.setPointsAwarded  (buildTimeSeriesData(
                    pointsHistory.stream()
                        .filter(h -> h.getPointsChange() != null && h.getPointsChange() > 0)
                        .collect(Collectors.toList()),
                    h -> h.getChangedAt() != null ?
                        Date.from(h.getChangedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null,
                    "Points Awarded"
                ));
            } else {
                stats.setPointsAwarded(new ArrayList<>());
            }

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

    private List<DashboardResponse.DepartmentStats> buildDepartmentStats(DashboardFilterRequest filter) {
        List<Department> departments = departmentRepository.findAll();
        List<DashboardResponse.DepartmentStats> departmentStats = new ArrayList<>();

        for (Department dept : departments) {
            DashboardResponse.DepartmentStats stats = new DashboardResponse.DepartmentStats();
            stats.setDepartmentId(dept.getId());
            stats.setDepartmentName(dept.getName());

            // Sử dụng count thay vì stream filter
            stats.setTotalUsers((int) userRepository.countByDepartmentId(dept.getId()));

            // Count events trong date range nếu có filter
            if (filter != null && filter.hasDateFilter()) {
                stats.setTotalEvents((int) eventRepository.countByDepartmentIdAndStartTimeBetween(
                    dept.getId(), filter.getStartDate(), filter.getEndDate()));
            } else {
                stats.setTotalEvents((int) eventRepository.countByDepartmentId(dept.getId()));
            }

            // Tính points từ database thay vì load tất cả users
            stats.setTotalPoints(calculateDepartmentTotalPoints(dept.getId()));
            stats.setAveragePointsPerUser(calculateDepartmentAveragePoints(dept.getId()));
            stats.setParticipationRate(calculateDepartmentParticipationRate(dept.getId()));

            departmentStats.add(stats);
        }

        return departmentStats;
    }

    private int calculateDepartmentTotalPoints(String departmentId) {
        // Tính tổng điểm (training + social) của tất cả users trong department
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("department.$id").is(new org.bson.types.ObjectId(departmentId))),
            Aggregation.project()
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8 + socialPoints").as("totalPoints"),
            Aggregation.group().sum("totalPoints").as("sum")
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        
        if (doc != null) {
            Number sum = doc.get("sum", Number.class);
            return sum != null ? sum.intValue() : 0;
        }
        return 0;
    }

    private double calculateDepartmentAveragePoints(String departmentId) {
        // Tính điểm trung bình (training + social) của users trong department
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("department.$id").is(new org.bson.types.ObjectId(departmentId))),
            Aggregation.project()
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8 + socialPoints").as("totalPoints"),
            Aggregation.group().avg("totalPoints").as("avgPoints")
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        
        if (doc != null) {
            Double avgPoints = doc.getDouble("avgPoints");
            return avgPoints != null ? avgPoints : 0.0;
        }
        return 0.0;
    }

    private double calculateDepartmentParticipationRate(String departmentId) {
        // Tính tỷ lệ tham gia: số users đã tham gia ít nhất 1 event / tổng số users trong department
        long totalUsers = userRepository.countByDepartmentId(departmentId);
        if (totalUsers == 0) {
            return 0.0;
        }
        
        // Đếm số users trong department đã có ít nhất 1 registration với status ATTENDED
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("status").is("ATTENDED")),
            Aggregation.lookup("users", "user.$id", "_id", "userDetails"),
            Aggregation.unwind("userDetails"),
            Aggregation.match(Criteria.where("userDetails.department.$id").is(new org.bson.types.ObjectId(departmentId))),
            Aggregation.group("userDetails._id"),
            Aggregation.group().sum("_id").as("participatedUsers")
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "registrations", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        
        long participatedUsers = 0;
        if (doc != null) {
            Number count = doc.get("participatedUsers", Number.class);
            participatedUsers = count != null ? count.longValue() : 0;
        }
        
        return totalUsers > 0 ? (double) participatedUsers / totalUsers : 0.0;
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

        // Sử dụng count queries thay vì findAll() + filter để tối ưu performance
        stats.setTotalEvents((int) eventRepository.countByOrganizerId(user.getId()));
        stats.setTotalRegistrations((int) registrationRepository.countByUserId(user.getId()));

        // Average rating from feedbacks (placeholder - Feedback model doesn't have rating field)
        stats.setAverageRating(0.0);

        return stats;
    }

    private DashboardResponse.EventStats buildEventStats(DashboardFilterRequest filter) {
        DashboardResponse.EventStats stats = new DashboardResponse.EventStats();

        // Nếu có date filter, chỉ tính events trong khoảng thời gian đó
        if (filter != null && filter.hasDateFilter()) {
            stats.setTotalEvents((int) eventRepository.countByStartTimeBetween(
                filter.getStartDate(), filter.getEndDate()));
            // Status counts vẫn tính tất cả (vì có thể event đang diễn ra không nằm trong range)
            stats.setUpcomingEvents((int) eventRepository.countByStatus(Event.EventStatus.APPROVED));
            stats.setOngoingEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
            stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
            stats.setCancelledEvents((int) eventRepository.countByStatus(Event.EventStatus.CANCELLED));
        } else {
            // Sử dụng count thay vì findAll
            stats.setTotalEvents((int) eventRepository.count());
            stats.setUpcomingEvents((int) eventRepository.countByStatus(Event.EventStatus.APPROVED));
            stats.setOngoingEvents((int) eventRepository.countByStatus(Event.EventStatus.ONGOING));
            stats.setCompletedEvents((int) eventRepository.countByStatus(Event.EventStatus.COMPLETED));
            stats.setCancelledEvents((int) eventRepository.countByStatus(Event.EventStatus.CANCELLED));
        }

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
        // Tính số người tham gia trung bình mỗi event (chỉ tính events đã có người tham gia)
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("status").is("ATTENDED")),
            Aggregation.group("event.$id").sum("_id").as("participantCount"),
            Aggregation.group().avg("participantCount").as("avgParticipants")
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "registrations", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        
        if (doc != null) {
            Double avgParticipants = doc.getDouble("avgParticipants");
            return avgParticipants != null ? avgParticipants : 0.0;
        }
        return 0.0;
    }

    private DashboardResponse.PointsStats buildPointsStats(DashboardFilterRequest filter) {
        DashboardResponse.PointsStats stats = new DashboardResponse.PointsStats();

        // TODO: Implement aggregation queries for better performance
        stats.setTotalTrainingPoints(calculateTotalTrainingPoints());
        stats.setTotalSocialPoints(calculateTotalSocialPoints());

        // Nếu có date filter, chỉ tính points trong khoảng thời gian đó
        if (filter != null && filter.hasDateFilter()) {
            stats.setTotalPointsAwarded(calculateTotalPointsAwarded(filter));
            stats.setTotalPointsDeducted(calculateTotalPointsDeducted(filter));
            stats.setPointsByMonth(buildPointsByMonthMap(filter));
        } else {
            stats.setTotalPointsAwarded(calculateTotalPointsAwarded(null));
            stats.setTotalPointsDeducted(calculateTotalPointsDeducted(null));
            stats.setPointsByMonth(buildPointsByMonthMap(null));
        }

        stats.setPointsByDepartment(buildPointsByDepartmentMap());
        stats.setAveragePointsPerUser(calculateAveragePointsPerUser());
        stats.setAveragePointsPerEvent(calculateAveragePointsPerEvent());

        return stats;
    }

    private int calculateTotalTrainingPoints() {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.project()
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8").as("totalTrainingPoints"),
            Aggregation.group().sum("totalTrainingPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        if (doc != null) {
            Number sum = doc.get("sum", Number.class);
            return sum != null ? sum.intValue() : 0;
        }
        return 0;
    }

    private int calculateTotalSocialPoints() {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.group().sum("socialPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        if (doc != null) {
            Number sum = doc.get("sum", Number.class);
            return sum != null ? sum.intValue() : 0;
        }
        return 0;
    }

    private int calculateTotalPointsAwarded(DashboardFilterRequest filter) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("pointsChange").gt(0)));

        if (filter != null && filter.hasDateFilter()) {
            operations.add(0, Aggregation.match(Criteria.where("changedAt")
                .gte(java.sql.Timestamp.valueOf(filter.getStartDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .lte(java.sql.Timestamp.valueOf(filter.getEndDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))));
        }

        operations.add(Aggregation.group().sum("pointsChange").as("sum"));

        Aggregation agg = Aggregation.newAggregation(operations);
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        if (doc != null) {
            Number sum = doc.get("sum", Number.class);
            return sum != null ? sum.intValue() : 0;
        }
        return 0;
    }

    private int calculateTotalPointsDeducted(DashboardFilterRequest filter) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("pointsChange").lt(0)));

        if (filter != null && filter.hasDateFilter()) {
            operations.add(0, Aggregation.match(Criteria.where("changedAt")
                .gte(java.sql.Timestamp.valueOf(filter.getStartDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .lte(java.sql.Timestamp.valueOf(filter.getEndDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))));
        }

        operations.add(Aggregation.group().sum("pointsChange").as("sum"));

        Aggregation agg = Aggregation.newAggregation(operations);
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        org.bson.Document doc = results.getUniqueMappedResult();
        if (doc != null) {
            Number sum = doc.get("sum", Number.class);
            return sum != null ? Math.abs(sum.intValue()) : 0;
        }
        return 0;
    }

    private Map<String, Integer> buildPointsByDepartmentMap() {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.project()
                .and("department.name").as("departmentName")
                .andExpression("trainingPoints1 + trainingPoints2 + trainingPoints3 + trainingPoints4 + trainingPoints5 + trainingPoints6 + trainingPoints7 + trainingPoints8 + socialPoints").as("totalPoints"),
            Aggregation.group("departmentName").sum("totalPoints").as("sum")
        );
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "users", org.bson.Document.class);
        Map<String, Integer> map = new HashMap<>();
        for (org.bson.Document doc : results) {
            String key = doc.getString("_id");
            if (key == null) {
                key = "UNKNOWN"; // hoặc continue; để bỏ qua nếu muốn
            }
            Number sum = doc.get("sum", Number.class);
            map.put(key, sum != null ? sum.intValue() : 0);
        }
        return map;
    }

    private Map<String, Integer> buildPointsByMonthMap(DashboardFilterRequest filter) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("pointsChange").gt(0)));

        if (filter != null && filter.hasDateFilter()) {
            operations.add(0, Aggregation.match(Criteria.where("changedAt")
                .gte(java.sql.Timestamp.valueOf(filter.getStartDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .lte(java.sql.Timestamp.valueOf(filter.getEndDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))));
        }

        operations.add(Aggregation.project()
            .andExpression("year(changedAt)").as("year")
            .andExpression("month(changedAt)").as("month")
            .and("pointsChange").as("pointsChange"));
        operations.add(Aggregation.group("year", "month").sum("pointsChange").as("sum"));

        Aggregation agg = Aggregation.newAggregation(operations);
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "pointsHistory", org.bson.Document.class);
        Map<String, Integer> map = new HashMap<>();
        for (org.bson.Document doc : results) {
            Integer year = doc.getInteger("year");
            Integer month = doc.getInteger("month");
            String key = (year != null ? year : 0) + "-" + String.format("%02d", month != null ? month : 0);
            Number sum = doc.get("sum", Number.class);
            map.put(key, sum != null ? sum.intValue() : 0);
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

    private DashboardResponse.SecurityStats buildSecurityStats(DashboardFilterRequest filter) {
        DashboardResponse.SecurityStats stats = new DashboardResponse.SecurityStats();

        // Nếu có date filter, chỉ tính trong khoảng thời gian đó
        if (filter != null && filter.hasDateFilter()) {
            stats.setTotalLogins((int) auditLogRepository.countByActionAndTimestampBetween(
                "USER_LOGIN", filter.getStartDate(), filter.getEndDate()));
            stats.setFailedLogins((int) auditLogRepository.countByActionAndTimestampBetween(
                "LOGIN_FAILED", filter.getStartDate(), filter.getEndDate()));
            stats.setSuspiciousActivities((int) auditLogRepository.countByActionAndTimestampBetween(
                "SUSPICIOUS_ACTIVITY", filter.getStartDate(), filter.getEndDate()));
            stats.setUnauthorizedAccess((int) auditLogRepository.countByActionAndTimestampBetween(
                "UNAUTHORIZED_ACCESS", filter.getStartDate(), filter.getEndDate()));
        } else {
            // Sử dụng count thay vì load tất cả audit logs
            stats.setTotalLogins((int) auditLogRepository.countByAction("USER_LOGIN"));
            stats.setFailedLogins((int) auditLogRepository.countByAction("LOGIN_FAILED"));
            stats.setSuspiciousActivities((int) auditLogRepository.countByAction("SUSPICIOUS_ACTIVITY"));
            stats.setUnauthorizedAccess((int) auditLogRepository.countByAction("UNAUTHORIZED_ACCESS"));
        }

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
        // Lấy các alerts gần đây từ audit logs (suspicious activities, failed logins, unauthorized access)
        Date recentThreshold = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 24 giờ gần đây
        
        List<AuditLog> recentAlerts = auditLogRepository.findByActionAndTimestampBetweenOrderByTimestampDesc(
            "SUSPICIOUS_ACTIVITY", recentThreshold, new Date());
        recentAlerts.addAll(auditLogRepository.findByActionAndTimestampBetweenOrderByTimestampDesc(
            "UNAUTHORIZED_ACCESS", recentThreshold, new Date()));
        
        // Lấy các failed logins gần đây (nhiều hơn 3 lần trong 1 giờ)
        List<AuditLog> failedLogins = auditLogRepository.findByActionAndTimestampBetweenOrderByTimestampDesc(
            "LOGIN_FAILED", recentThreshold, new Date());
        
        // Group by user và IP để tìm các pattern đáng ngờ
        Map<String, Long> failedLoginCounts = failedLogins.stream()
            .collect(Collectors.groupingBy(
                log -> (log.getUserId() != null ? log.getUserId() : "") + "_" + (log.getIpAddress() != null ? log.getIpAddress() : ""),
                Collectors.counting()
            ));
        
        // Thêm alerts cho các user có nhiều failed logins
        failedLoginCounts.entrySet().stream()
            .filter(entry -> entry.getValue() >= 3)
            .forEach(entry -> {
                String[] parts = entry.getKey().split("_");
                String userId = parts.length > 0 ? parts[0] : "Unknown";
                String ip = parts.length > 1 ? parts[1] : "Unknown";
                recentAlerts.add(createAlertLog("MULTIPLE_FAILED_LOGINS", 
                    "User " + userId + " has " + entry.getValue() + " failed login attempts from IP " + ip));
            });
        
        // Sắp xếp theo thời gian và lấy 10 alerts gần nhất
        return recentAlerts.stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(10)
            .map(log -> String.format("[%s] %s: %s", 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(log.getTimestamp()),
                log.getAction(),
                log.getDescription()))
            .collect(Collectors.toList());
    }
    
    private AuditLog createAlertLog(String action, String description) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(new Date());
        log.setStatus("FAILED");
        return log;
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
