package com.admin.event_management_backend_java_spring.points.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class ManualPointsProcessingResult {
    private String eventId;
    private String eventName;
    private String action;
    private String reason;
    private int totalProcessed;
    private int successCount;
    private int failCount;
    private int totalPointsAwarded;
    private int totalPointsPenalized;
    private String description;
    private List<String> failDetails;
    private List<ProcessedUserDetail> processedUsers;
    
    @Data
    public static class ProcessedUserDetail {
        private String userId;
        private String userName;
        private String userEmail;
        private boolean hasCheckIn;
        private boolean hasCheckOut;
        private Integer pointsAwarded;
        private String processingStatus;
        private String reason;
    }
} 