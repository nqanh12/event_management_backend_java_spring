package com.admin.event_management_backend_java_spring.audit.payload.response;

import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String userRole;
    private String departmentId;
    private String departmentName;
    
    private String action;
    private String description;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    
    private String status;
    private String errorMessage;
    private Long duration;
    
    private Date timestamp;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    
    private Map<String, Object> additionalData;
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    
    // Computed fields
    private String timeAgo;
    private String severity;
    private boolean isSuspicious;
    private boolean isFailed;
    
    public static AuditLogResponse fromEntity(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        
        AuditLogResponse response = new AuditLogResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setUserEmail(entity.getUserEmail());
        response.setUserName(entity.getUserEmail()); // Use email as name since userName field doesn't exist
        response.setUserRole(entity.getUserRole());
        response.setDepartmentId(entity.getDepartmentId());
        response.setDepartmentName(""); // Will be populated separately if needed
        
        response.setAction(entity.getAction());
        response.setDescription(entity.getDescription());
        response.setResourceType(entity.getResourceType());
        response.setResourceId(entity.getResourceId());
        response.setResourceName(""); // Will be populated separately if needed
        
        response.setStatus(entity.getStatus());
        response.setErrorMessage(entity.getErrorMessage());
        response.setDuration(0L); // Duration not tracked in current model
        
        response.setTimestamp(entity.getTimestamp());
        response.setIpAddress(entity.getIpAddress());
        response.setUserAgent(entity.getUserAgent());
        response.setSessionId(entity.getSessionId());
        
        response.setAdditionalData(null); // Use oldValues and newValues instead
        response.setRequestData(entity.getOldValues());
        response.setResponseData(entity.getNewValues());
        
        // Set computed fields
        response.setTimeAgo(calculateTimeAgo(entity.getTimestamp()));
        response.setSeverity(determineSeverity(entity));
        response.setSuspicious(isSuspiciousActivity(entity));
        response.setFailed("FAILED".equals(entity.getStatus()) || "ERROR".equals(entity.getStatus()));
        
        return response;
    }
    
    private static String calculateTimeAgo(Date timestamp) {
        if (timestamp == null) return "";
        
        long diffInMillies = new Date().getTime() - timestamp.getTime();
        long diffInMinutes = diffInMillies / (1000 * 60);
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        
        if (diffInMinutes < 1) {
            return "Vừa xong";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " phút trước";
        } else if (diffInHours < 24) {
            return diffInHours + " giờ trước";
        } else if (diffInDays < 7) {
            return diffInDays + " ngày trước";
        } else {
            return diffInDays / 7 + " tuần trước";
        }
    }
    
    private static String determineSeverity(AuditLog entity) {
        if ("FAILED".equals(entity.getStatus()) || "ERROR".equals(entity.getStatus())) {
            return "HIGH";
        }
        
        if ("LOGIN_FAILED".equals(entity.getAction()) || 
            "UNAUTHORIZED_ACCESS".equals(entity.getAction()) ||
            "SUSPICIOUS_ACTIVITY".equals(entity.getAction())) {
            return "HIGH";
        }
        
        if ("USER_LOGIN".equals(entity.getAction()) || 
            "USER_LOGOUT".equals(entity.getAction()) ||
            "PASSWORD_CHANGE".equals(entity.getAction())) {
            return "MEDIUM";
        }
        
        return "LOW";
    }
    
    private static boolean isSuspiciousActivity(AuditLog entity) {
        if ("SUSPICIOUS_ACTIVITY".equals(entity.getAction()) ||
            "UNAUTHORIZED_ACCESS".equals(entity.getAction()) ||
            "MULTIPLE_LOGIN_ATTEMPTS".equals(entity.getAction())) {
            return true;
        }
        
        // Check for multiple failed logins
        if ("LOGIN_FAILED".equals(entity.getAction())) {
            return true;
        }
        
        // Check for unusual activity patterns - skip duration check since it's not available
        // if (entity.getDuration() != null && entity.getDuration() > 30000) { // > 30 seconds
        //     return true;
        // }
        
        return false;
    }
}
