package com.admin.event_management_backend_java_spring.integration;

import org.springframework.stereotype.Service;

@Service
public class ZoomService {
    // TODO: Cấu hình OAuth2 hoặc JWT cho Zoom API
    // Có thể dùng thư viện zoom-api nếu tích hợp thực tế

    public String createZoomMeeting(String topic, String startTime) {
        // Mock: trả về link Zoom meeting
        // Thực tế: gọi Zoom API
        return "https://zoom.us/j/1234567890?pwd=mock";
    }
} 