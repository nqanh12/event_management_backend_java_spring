package com.admin.event_management_backend_java_spring.notification.controller;

import com.admin.event_management_backend_java_spring.notification.payload.request.NotificationRequest;
import com.admin.event_management_backend_java_spring.notification.service.NotificationService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Gửi thông báo", description = "Gửi thông báo đến người dùng.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendNotification(@Valid @RequestBody NotificationRequest req) {
        ApiResponse<?> response = notificationService.sendNotification(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Lấy thông báo theo người dùng", description = "Lấy danh sách thông báo của một người dùng theo ID.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getNotificationsByUser(
            @Parameter(description = "ID của người dùng") @PathVariable String userId) {
        ApiResponse<?> response = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(response);
    }
}
