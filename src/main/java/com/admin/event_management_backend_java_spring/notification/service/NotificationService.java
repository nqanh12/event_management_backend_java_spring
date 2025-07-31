package com.admin.event_management_backend_java_spring.notification.service;

import com.admin.event_management_backend_java_spring.notification.model.Notification;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.notification.payload.request.NotificationRequest;
import com.admin.event_management_backend_java_spring.notification.payload.response.NotificationResponse;
import com.admin.event_management_backend_java_spring.notification.repository.NotificationRepository;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    private NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(notification.getId());
        dto.setUserName(notification.getUser() != null ? notification.getUser().getFullName() : null);
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public ApiResponse<?> sendNotification(NotificationRequest req) {
        log.info("[NOTIFICATION] Gửi thông báo tới userId: {} với tiêu đề: {}", req.getRecipientId(), req.getTitle());
        User user = userRepository.findById(req.getUserId())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(req.getTitle());
        notification.setContent(req.getContent());
        notification.setType(Notification.NotificationType.valueOf(req.getType()));
        notification.setCreatedAt(new java.util.Date());
        notification.setRead(false);
        notificationRepository.save(notification);
        return new ApiResponse<>(true, "Notification sent", toNotificationResponse(notification));
    }

    public ApiResponse<?> getNotificationsByUser(String userId) {
        log.info("[NOTIFICATION] Lấy thông báo cho userId: {}", userId);
        List<NotificationResponse> notifications = notificationRepository.findAll().stream()
            .filter(n -> n.getUser().getId().equals(userId))
            .map(this::toNotificationResponse)
            .toList();
        return new ApiResponse<>(true, "Success", notifications);
    }
    
    @Async("notificationTaskExecutor")
    public CompletableFuture<Void> sendBulkNotificationsAsync(List<NotificationRequest> requests) {
        for (NotificationRequest req : requests) {
            try {
                sendNotification(req);
                Thread.sleep(50); // Small delay
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("notificationTaskExecutor")
    public CompletableFuture<Void> sendDepartmentNotificationAsync(String departmentId, String title, String content) {
        log.warn("[NOTIFICATION] Gửi thông báo phòng ban với departmentId: {}", departmentId);
        List<User> departmentUsers = userRepository.findAll().stream()
            .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(departmentId))
            .collect(Collectors.toList());
            
        for (User user : departmentUsers) {
            try {
                NotificationRequest req = new NotificationRequest();
                req.setUserId(user.getId());
                req.setTitle(title);
                req.setContent(content);
                req.setType("GENERAL");
                sendNotification(req);
                Thread.sleep(50);
            } catch (Exception e) {
                System.err.println("Failed to send department notification to " + user.getEmail() + ": " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
} 