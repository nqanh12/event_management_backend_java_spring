package com.admin.event_management_backend_java_spring.integration;

import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarService {
    // TODO: Cấu hình OAuth2, clientId, clientSecret, redirectUri
    // Có thể dùng thư viện google-api-client nếu tích hợp thực tế

    public String createCalendarEvent(String summary, String description, String startTime, String endTime) {
        // Mock: trả về link Google Calendar event
        // Thực tế: gọi Google Calendar API
        return "https://calendar.google.com/calendar/r/eventedit?text=" + summary;
    }
} 