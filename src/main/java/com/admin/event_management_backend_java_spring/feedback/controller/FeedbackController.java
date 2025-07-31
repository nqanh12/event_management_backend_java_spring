package com.admin.event_management_backend_java_spring.feedback.controller;

import com.admin.event_management_backend_java_spring.feedback.payload.request.FeedbackRequest;
import com.admin.event_management_backend_java_spring.feedback.service.FeedbackService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @Operation(summary = "Gửi phản hồi sự kiện", description = "Người dùng gửi phản hồi cho sự kiện.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendFeedback(@Valid @RequestBody FeedbackRequest req, Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<?> response = feedbackService.sendFeedback(userId, req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Lấy phản hồi theo sự kiện", description = "Lấy danh sách phản hồi cho một sự kiện theo ID.")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<?>> getFeedbacksByEvent(
            @Parameter(description = "ID của sự kiện") @PathVariable String eventId) {
        ApiResponse<?> response = feedbackService.getFeedbacksByEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy phản hồi theo người dùng", description = "Lấy danh sách phản hồi của một người dùng theo ID.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getFeedbacksByUser(
            @Parameter(description = "ID của người dùng") @PathVariable String userId) {
        ApiResponse<?> response = feedbackService.getFeedbacksByUser(userId);
        return ResponseEntity.ok(response);
    }
} 