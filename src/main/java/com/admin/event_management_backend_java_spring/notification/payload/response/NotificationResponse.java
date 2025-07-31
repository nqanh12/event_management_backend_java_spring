package com.admin.event_management_backend_java_spring.notification.payload.response;

import lombok.Data;
import java.util.Date;

@Data
public class NotificationResponse {
    private String id;
    private String userName;
    private String title;
    private String content;
    private String type;
    private boolean isRead;
    private Date createdAt;
} 