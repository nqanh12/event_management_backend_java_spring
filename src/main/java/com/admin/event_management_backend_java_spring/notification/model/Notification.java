package com.admin.event_management_backend_java_spring.notification.model;

import com.admin.event_management_backend_java_spring.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;
    @DBRef
    private User user;
    private String title;
    private String content;
    private NotificationType type;
    private boolean isRead = false;
    private Date createdAt;

    public enum NotificationType {
        EVENT, SYSTEM, POINTS
    }
} 