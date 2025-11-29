package com.admin.event_management_backend_java_spring.event.model;

/**
 * Enum định nghĩa cấp độ sự kiện
 */
public enum EventLevel {
    NATIONAL("Cấp Quốc gia"),
    CITY("Cấp Thành phố"),
    SCHOOL("Cấp Trường"),
    DEPARTMENT("Cấp Khoa"),
    CLUB("Cấp CLB"),
    LOCAL("Địa phương"),
    EXTERNAL("Ngoài trường");
    
    private final String displayName;
    
    EventLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

