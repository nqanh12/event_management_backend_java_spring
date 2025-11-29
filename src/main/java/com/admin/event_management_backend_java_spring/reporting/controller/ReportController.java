package com.admin.event_management_backend_java_spring.reporting.controller;

import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.reporting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<?>> eventCountByStatus() {
        ApiResponse<?> response = reportService.eventCountByStatus();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/students/top")
    public ResponseEntity<ApiResponse<?>> topStudentsByPoints(@RequestParam(defaultValue = "10") int limit) {
        ApiResponse<?> response = reportService.topStudentsByPoints(limit);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<?>> eventCountByDepartment() {
        ApiResponse<?> response = reportService.eventCountByDepartment();
        return ResponseEntity.ok(response);
    }
}
