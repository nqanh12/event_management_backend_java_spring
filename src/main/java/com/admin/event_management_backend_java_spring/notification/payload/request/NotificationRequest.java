package com.admin.event_management_backend_java_spring.notification.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Recipient ID is required")
    private String recipientId;

    // Bổ sung cho tương thích code cũ
    private String userId;
    private String type;
} 