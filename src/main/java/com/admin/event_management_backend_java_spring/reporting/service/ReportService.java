package com.admin.event_management_backend_java_spring.reporting.service;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    public ApiResponse<Map<String, Long>> eventCountByStatus() {
        List<Event> events = eventRepository.findAll();
        Map<String, Long> result = events.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus().name(), Collectors.counting()));
        return new ApiResponse<>(true, "Event count by status", result);
    }

    public ApiResponse<List<User>> topStudentsByPoints(int limit) {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.STUDENT)
                .sorted(Comparator.comparingInt((User u) -> {
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
                }).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        return new ApiResponse<>(true, "Top students by points", students);
    }

    public ApiResponse<Map<String, Long>> eventCountByDepartment() {
        List<Event> events = eventRepository.findAll();
        Map<String, Long> result = events.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getDepartment().getName(), 
                    Collectors.counting()
                ));
        return new ApiResponse<>(true, "Event count by department", result);
    }
    
    @Async("reportTaskExecutor")
    public CompletableFuture<String> exportEventReportAsync(Date startDate, Date endDate, String format) {
        try {
            // Simulate report generation
            Thread.sleep(2000);
            return CompletableFuture.completedFuture("event_report_" + System.currentTimeMillis() + "." + format);
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Error generating report: " + e.getMessage());
        }
    }
    
    @Async("reportTaskExecutor")
    public CompletableFuture<String> exportUserReportAsync(String departmentId, String format) {
        try {
            // Simulate report generation
            Thread.sleep(1500);
            return CompletableFuture.completedFuture("user_report_" + System.currentTimeMillis() + "." + format);
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Error generating report: " + e.getMessage());
        }
    }
    
    @Async("reportTaskExecutor")
    public CompletableFuture<String> exportPointsReportAsync(Date startDate, Date endDate, String format) {
        try {
            // Simulate report generation
            Thread.sleep(3000);
            return CompletableFuture.completedFuture("points_report_" + System.currentTimeMillis() + "." + format);
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Error generating report: " + e.getMessage());
        }
    }
}
