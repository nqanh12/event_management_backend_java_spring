package com.admin.event_management_backend_java_spring.leadership.model;

/**
 * Enum định nghĩa chức vụ cán bộ và điểm tương ứng
 */
public enum LeadershipPosition {
    LEADER("Lớp trưởng / Chủ tịch CLB / Chủ nhiệm đội", 10),
    VICE_LEADER("Lớp phó / Phó chủ nhiệm CLB", 8),
    EXECUTIVE_MEMBER("Uỷ viên BCH khoa / chi hội / ban cán sự", 7),
    REGULAR_MEMBER("Thành viên CLB – đội – nhóm thường xuyên", 3);
    
    private final String displayName;
    private final int points;
    
    LeadershipPosition(String displayName, int points) {
        this.displayName = displayName;
        this.points = points;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPoints() {
        return points;
    }
}

