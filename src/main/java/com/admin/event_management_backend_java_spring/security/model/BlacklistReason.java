package com.admin.event_management_backend_java_spring.security.model;

public enum BlacklistReason {
    LOGOUT("User logged out"),
    REVOKED("Token revoked by admin"),
    COMPROMISED("Token compromised"),
    EXPIRED("Token expired"),
    SUSPICIOUS_ACTIVITY("Suspicious activity detected"),
    PASSWORD_CHANGED("Password changed"),
    ACCOUNT_DISABLED("Account disabled"),
    FORCE_LOGOUT("Force logout by admin");
    
    private final String description;
    
    BlacklistReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
