package com.admin.event_management_backend_java_spring.leadership.model;

/**
 * Enum định nghĩa loại tổ chức cán bộ
 */
public enum LeadershipType {
    CLASS("Cán bộ lớp"),
    UNION("Cán bộ đoàn"),
    CLUB("Cán bộ CLB"),
    TEAM("Cán bộ đội/nhóm");
    
    private final String displayName;
    
    LeadershipType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

