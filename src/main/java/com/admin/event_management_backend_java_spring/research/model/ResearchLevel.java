package com.admin.event_management_backend_java_spring.research.model;

/**
 * Enum định nghĩa cấp độ nghiên cứu khoa học và điểm tương ứng
 */
public enum ResearchLevel {
    NATIONAL("Cấp Quốc gia", 30),
    CITY("Cấp Thành phố", 20),
    SCHOOL("Cấp Trường", 10),
    DEPARTMENT("Cấp Khoa", 5);
    
    private final String displayName;
    private final int points;
    
    ResearchLevel(String displayName, int points) {
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

