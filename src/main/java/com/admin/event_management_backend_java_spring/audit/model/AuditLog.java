package com.admin.event_management_backend_java_spring.audit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private String id;

    @Indexed
    private String userId; // ID của user thực hiện hành động

    @Indexed
    private String userEmail; // Email của user thực hiện hành động

    @Indexed
    private String userRole; // Role của user thực hiện hành động

    @Indexed
    private String action; // Hành động được thực hiện

    @Indexed
    private String resourceType; // Loại resource (EVENT, USER, POINTS, etc.)

    private String resourceId; // ID của resource bị ảnh hưởng

    private String description; // Mô tả chi tiết hành động

    private String ipAddress; // IP address của user

    private String userAgent; // User agent của browser

    private Map<String, Object> oldValues; // Giá trị cũ (cho update operations)

    private Map<String, Object> newValues; // Giá trị mới (cho update operations)

    private String status; // SUCCESS, FAILED, PENDING

    private String errorMessage; // Thông báo lỗi nếu có

    @Indexed
    private Date timestamp; // Thời gian thực hiện hành động

    @Indexed
    private String sessionId; // Session ID

    private String departmentId; // Department ID nếu có

    public enum ActionType {
        // User actions
        USER_LOGIN, USER_LOGOUT, USER_REGISTER, USER_UPDATE, USER_DELETE, USER_PASSWORD_CHANGE,
        
        // Event actions
        EVENT_CREATE, EVENT_UPDATE, EVENT_DELETE, EVENT_PUBLISH, EVENT_CANCEL, EVENT_APPROVE, EVENT_REJECT,
        
        // Registration actions
        REGISTRATION_CREATE, REGISTRATION_UPDATE, REGISTRATION_DELETE, 
        REGISTRATION_CHECKIN, REGISTRATION_CHECKOUT,
        
        // Points actions
        POINTS_AWARD, POINTS_DEDUCT, POINTS_MANUAL_UPDATE, POINTS_BULK_UPDATE,
        
        // Department actions
        DEPARTMENT_CREATE, DEPARTMENT_UPDATE, DEPARTMENT_DELETE, DEPARTMENT_PENALTY_UPDATE,
        
        // Course actions
        COURSE_CREATE, COURSE_UPDATE, COURSE_DELETE,
        
        // System actions
        SYSTEM_BACKUP, SYSTEM_RESTORE, SYSTEM_CONFIG_UPDATE,
        
        // Security actions
        LOGIN_FAILED, UNAUTHORIZED_ACCESS, SUSPICIOUS_ACTIVITY
    }

    public enum ResourceType {
        USER, EVENT, REGISTRATION, POINTS, DEPARTMENT, COURSE, SYSTEM, NOTIFICATION, FEEDBACK
    }

    public enum Status {
        SUCCESS, FAILED, PENDING, CANCELLED
    }
}
