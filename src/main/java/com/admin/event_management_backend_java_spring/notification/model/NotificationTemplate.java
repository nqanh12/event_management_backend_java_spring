package com.admin.event_management_backend_java_spring.notification.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "notification_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    @Id
    private String id;

    @Indexed
    private String name; // Template name

    @Indexed
    private String code; // Template code for programmatic access

    private String subject; // Email subject
    private String body; // Email body with placeholders
    private String smsBody; // SMS body with placeholders
    
    // Channel configuration
    private List<NotificationChannel> channels; // EMAIL, SMS, PUSH, IN_APP
    
    // Trigger configuration
    private List<NotificationTrigger> triggers; // EVENT_CREATED, EVENT_APPROVED, etc.
    
    // Target audience
    private List<String> targetRoles; // STUDENT, ORGANIZER, etc.
    private List<String> targetDepartments; // Department IDs
    
    // Template variables
    private List<String> variables; // Available variables like {userName}, {eventName}
    
    // Status
    private boolean active = true;
    private boolean isDefault = false;
    
    // Metadata
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    
    public enum NotificationChannel {
        EMAIL, SMS, PUSH_NOTIFICATION, IN_APP, WEBHOOK
    }
    
    public enum NotificationTrigger {
        EVENT_CREATED,
        EVENT_APPROVED,
        EVENT_CANCELLED,
        EVENT_REMINDER,
        REGISTRATION_CREATED,
        REGISTRATION_CANCELLED,
        CHECK_IN,
        CHECK_OUT,
        POINTS_AWARDED,
        POINTS_DEDUCTED,
        USER_REGISTERED,
        PASSWORD_RESET,
        SYSTEM_MAINTENANCE,
        CUSTOM
    }
} 