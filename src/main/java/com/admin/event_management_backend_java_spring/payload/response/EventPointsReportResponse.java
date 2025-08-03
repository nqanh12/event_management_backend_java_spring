package com.admin.event_management_backend_java_spring.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class EventPointsReportResponse {
    private String eventId;
    private String eventName;
    private String eventType;
    private int totalRegistrations;
    private int fullyAttendedCount;
    private int partiallyAttendedCount;
    private int absentCount;
    private int totalPointsAwarded;
    private int totalPointsPenalized;
    private int netPoints;
    private List<ParticipantPointsDetail> participants;
    
    @Data
    public static class ParticipantPointsDetail {
        private String userId;
        private String userName;
        private String userEmail;
        private boolean hasCheckIn;
        private boolean hasCheckOut;
        private boolean fullyAttended;
        private Integer pointsAwarded; // Số dương = cộng điểm, số âm = trừ điểm
        private String status;
    }
} 