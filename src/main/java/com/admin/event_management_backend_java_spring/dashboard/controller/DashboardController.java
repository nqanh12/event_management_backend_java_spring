package com.admin.event_management_backend_java_spring.dashboard.controller;

import com.admin.event_management_backend_java_spring.dashboard.payload.request.DashboardFilterRequest;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.response.DashboardResponse;
import com.admin.event_management_backend_java_spring.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY_ADMIN')")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * Lấy dashboard tổng hợp với bộ lọc theo ngày tháng năm
     * 
     * @param startDate Ngày bắt đầu (format: yyyy-MM-dd hoặc yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate Ngày kết thúc (format: yyyy-MM-dd hoặc yyyy-MM-dd'T'HH:mm:ss)
     * @param preset Preset option: TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_WEEK, LAST_WEEK, 
     *               THIS_MONTH, LAST_MONTH, THIS_QUARTER, LAST_QUARTER, THIS_YEAR, LAST_YEAR, ALL_TIME
     * 
     * @example GET /api/dashboard?preset=LAST_30_DAYS
     * @example GET /api/dashboard?startDate=2024-01-01&endDate=2024-01-31
     * @example GET /api/dashboard?preset=THIS_MONTH
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) DashboardFilterRequest.DatePreset preset) {
        
        DashboardFilterRequest filter = new DashboardFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPreset(preset);
        
        // Tính toán dates từ preset nếu không có startDate/endDate
        if (filter.getStartDate() == null || filter.getEndDate() == null) {
            filter.calculateDatesFromPreset();
        }
        
        ApiResponse<DashboardResponse> response = dashboardService.getDashboard(filter);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thống kê tổng quan
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardResponse.OverviewStats>> getOverviewStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        DashboardResponse.OverviewStats overviewStats = fullResponse.getData().getOverviewStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Overview stats retrieved successfully", overviewStats));
    }
    
    /**
     * Lấy thống kê theo thời gian
     */
    @GetMapping("/time-series")
    public ResponseEntity<ApiResponse<DashboardResponse.TimeSeriesStats>> getTimeSeriesStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        DashboardResponse.TimeSeriesStats timeSeriesStats = fullResponse.getData().getTimeSeriesStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Time series stats retrieved successfully", timeSeriesStats));
    }
    
    /**
     * Lấy thống kê theo department
     */
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<java.util.List<DashboardResponse.DepartmentStats>>> getDepartmentStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        java.util.List<DashboardResponse.DepartmentStats> departmentStats = fullResponse.getData().getDepartmentStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Department stats retrieved successfully", departmentStats));
    }
    
    /**
     * Lấy top students
     */
    @GetMapping("/top-students")
    public ResponseEntity<ApiResponse<java.util.List<DashboardResponse.UserStats>>> getTopStudents() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        java.util.List<DashboardResponse.UserStats> topStudents = fullResponse.getData().getTopStudents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Top students retrieved successfully", topStudents));
    }
    
    /**
     * Lấy top organizers
     */
    @GetMapping("/top-organizers")
    public ResponseEntity<ApiResponse<java.util.List<DashboardResponse.UserStats>>> getTopOrganizers() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        java.util.List<DashboardResponse.UserStats> topOrganizers = fullResponse.getData().getTopOrganizers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Top organizers retrieved successfully", topOrganizers));
    }
    
    /**
     * Lấy thống kê events
     */
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<DashboardResponse.EventStats>> getEventStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        DashboardResponse.EventStats eventStats = fullResponse.getData().getEventStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Event stats retrieved successfully", eventStats));
    }
    
    /**
     * Lấy thống kê points
     */
    @GetMapping("/points")
    public ResponseEntity<ApiResponse<DashboardResponse.PointsStats>> getPointsStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        DashboardResponse.PointsStats pointsStats = fullResponse.getData().getPointsStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Points stats retrieved successfully", pointsStats));
    }
    
    /**
     * Lấy thống kê security
     */
    @GetMapping("/security")
    public ResponseEntity<ApiResponse<DashboardResponse.SecurityStats>> getSecurityStats() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        DashboardResponse.SecurityStats securityStats = fullResponse.getData().getSecurityStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Security stats retrieved successfully", securityStats));
    }
    
    /**
     * Lấy recent activities
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<java.util.List<DashboardResponse.RecentActivity>>> getRecentActivities() {
        ApiResponse<DashboardResponse> fullResponse = dashboardService.getDashboard();
        java.util.List<DashboardResponse.RecentActivity> recentActivities = fullResponse.getData().getRecentActivities();
        return ResponseEntity.ok(new ApiResponse<>(true, "Recent activities retrieved successfully", recentActivities));
    }
}
