package com.admin.event_management_backend_java_spring.integration;

import org.springframework.stereotype.Service;

@Service
public class FcmService {
    // TODO: Cấu hình Firebase Admin SDK
    // Thực tế: dùng FirebaseMessaging để gửi notification

    public boolean sendNotification(String deviceToken, String title, String body) {
        // Mock: luôn trả về true
        // Thực tế: gọi FCM API
        return true;
    }
} 