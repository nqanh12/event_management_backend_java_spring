package com.admin.event_management_backend_java_spring.analytics.controller;

import com.admin.event_management_backend_java_spring.analytics.payload.request.AnalyticsRequest;
import com.admin.event_management_backend_java_spring.analytics.payload.response.AnalyticsResponse;
import com.admin.event_management_backend_java_spring.analytics.service.AnalyticsService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "Lấy tổng quan phân tích", description = "Lấy tổng quan phân tích theo loại (type).")
    @PostMapping("/summary")
    public AnalyticsResponse getSummary(@Valid @RequestBody AnalyticsRequest request) {
        return analyticsService.getSummary(request);
    }

    @Operation(summary = "Xu hướng tham gia sự kiện", description = "Phân tích xu hướng tham gia sự kiện trong khoảng thời gian.")
    @PostMapping("/participation-trends")
    public ApiResponse<Map<String, Object>> getParticipationTrends(
            @Parameter(description = "Ngày bắt đầu") @RequestParam Date startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam Date endDate) {
        return analyticsService.getParticipationTrends(startDate, endDate);
    }

    @Operation(summary = "Phân tích ROI sự kiện", description = "Phân tích ROI cho các sự kiện.")
    @GetMapping("/event-roi")
    public ApiResponse<Map<String, Object>> getEventROIAnalysis() {
        return analyticsService.getEventROIAnalysis();
    }

    @Operation(summary = "Phân tích hành vi người dùng", description = "Phân tích hành vi người dùng trong hệ thống.")
    @GetMapping("/user-behavior")
    public ApiResponse<Map<String, Object>> getUserBehaviorAnalysis() {
        return analyticsService.getUserBehaviorAnalysis();
    }
}
