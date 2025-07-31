package com.admin.event_management_backend_java_spring.controller;

import com.admin.event_management_backend_java_spring.integration.MailService;
import com.admin.event_management_backend_java_spring.notification.payload.request.NotificationRequest;
import com.admin.event_management_backend_java_spring.notification.service.NotificationService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.points.service.PointsService;
import com.admin.event_management_backend_java_spring.reporting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@RestController
@RequestMapping("/api/async")
@PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
public class AsyncController {
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private ReportService reportService;

    @PostMapping("/bulk-email")
    public ResponseEntity<ApiResponse<String>> sendBulkEmail(
            @RequestBody List<String> recipients,
            @RequestParam String subject,
            @RequestParam String content) {
        // Gửi bulk email với template base-template.html
        for (String to : recipients) {
            Map<String, String> vars = new java.util.HashMap<>();
            vars.put("header_title", subject);
            vars.put("header_subtitle", "Thông báo từ hệ thống EventHub");
            vars.put("content", content);
            mailService.sendHtmlMail(to, subject, "base-template.html", vars);
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Bulk email queued for processing. Recipients: " + recipients.size(), 
            "Email processing started"));
    }

    @PostMapping("/event-notification")
    public ResponseEntity<ApiResponse<String>> sendEventNotification(
            @RequestBody List<String> recipients,
            @RequestParam String eventName,
            @RequestParam String eventTime,
            @RequestParam String eventLocation) {
        for (String to : recipients) {
            Map<String, String> vars = new java.util.HashMap<>();
            vars.put("event_name", eventName);
            vars.put("event_time", eventTime);
            vars.put("event_location", eventLocation);
            vars.put("event_capacity", "");
            vars.put("event_status", "");
            vars.put("countdown_timer", "");
            vars.put("registration_link", "");
            vars.put("event_details_link", "");
            mailService.sendHtmlMail(to, "Thông báo sự kiện: " + eventName, "event-notification.html", vars);
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Event notification queued for processing. Recipients: " + recipients.size(), 
            "Notification processing started"));
    }

    @PostMapping("/bulk-notifications")
    public ResponseEntity<ApiResponse<String>> sendBulkNotifications(
            @RequestBody List<NotificationRequest> requests) {
        
        CompletableFuture<Void> future = notificationService.sendBulkNotificationsAsync(requests);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Bulk notifications queued for processing. Count: " + requests.size(), 
            "Notification processing started"));
    }

    @PostMapping("/department-notification")
    public ResponseEntity<ApiResponse<String>> sendDepartmentNotification(
            @RequestParam String departmentId,
            @RequestParam String title,
            @RequestParam String content) {
        
        CompletableFuture<Void> future = notificationService.sendDepartmentNotificationAsync(
            departmentId, title, content);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Department notification queued for processing", 
            "Notification processing started"));
    }

    @PostMapping("/auto-process-points")
    public ResponseEntity<ApiResponse<String>> autoProcessAllPendingPoints() {
        CompletableFuture<?> future = pointsService.bulkProcessPointsAsync();
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Auto points processing queued", 
            "Points processing started"));
    }

    @PostMapping("/export-event-report")
    public ResponseEntity<ApiResponse<String>> exportEventReport(
            @RequestParam Date startDate,
            @RequestParam Date endDate,
            @RequestParam(defaultValue = "pdf") String format) {
        
        CompletableFuture<String> future = reportService.exportEventReportAsync(startDate, endDate, format);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Event report export queued", 
            "Report generation started"));
    }

    @PostMapping("/export-user-report")
    public ResponseEntity<ApiResponse<String>> exportUserReport(
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "pdf") String format) {
        
        CompletableFuture<String> future = reportService.exportUserReportAsync(departmentId, format);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "User report export queued", 
            "Report generation started"));
    }

    @PostMapping("/export-points-report")
    public ResponseEntity<ApiResponse<String>> exportPointsReport(
            @RequestParam Date startDate,
            @RequestParam Date endDate,
            @RequestParam(defaultValue = "pdf") String format) {
        
        CompletableFuture<String> future = reportService.exportPointsReportAsync(startDate, endDate, format);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
            "Points report export queued", 
            "Report generation started"));
    }
} 