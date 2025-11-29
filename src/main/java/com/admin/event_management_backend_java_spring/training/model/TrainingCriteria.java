package com.admin.event_management_backend_java_spring.training.model;

/**
 * Enum định nghĩa 5 tiêu chí đánh giá điểm rèn luyện
 */
public enum TrainingCriteria {
    TC1_LEARNING("TC1 - Ý thức và thái độ học tập", 20, 20.0),
    TC2_REGULATION("TC2 - Chấp hành nội quy, quy định", 25, 25.0),
    TC3_ACTIVITIES("TC3 - Hoạt động chính trị - xã hội - văn hoá - thể thao", 20, 0.0),
    TC4_COMMUNITY("TC4 - Ý thức công dân và quan hệ cộng đồng", 25, 25.0),
    TC5_LEADERSHIP("TC5 - Vai trò cán bộ lớp, đoàn, hội, CLB", 10, 0.0);
    
    private final String displayName;
    private final int maxPoints;
    private final double defaultPoints;
    
    TrainingCriteria(String displayName, int maxPoints, double defaultPoints) {
        this.displayName = displayName;
        this.maxPoints = maxPoints;
        this.defaultPoints = defaultPoints;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMaxPoints() {
        return maxPoints;
    }
    
    public double getDefaultPoints() {
        return defaultPoints;
    }
    
    public String getCode() {
        return name();
    }
}

