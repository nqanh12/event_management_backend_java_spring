package com.admin.event_management_backend_java_spring.points.payload.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PointsProcessingDashboardResponse {
    private int totalEvents;
    private int eventsWithPendingProcessing;
    private int totalPendingRegistrations;
    private Map<String, Long> processingStatusCounts; // PENDING, AUTO_AWARDED, etc.
    private List<EventSummary> eventsSummary;
    
    @Data
    public static class EventSummary {
        private String eventId;
        private String eventName;
        private String eventType;
        private int totalRegistrations;
        private int pendingCount;
        private int autoAwardedCount;
        private int manualProcessedCount;
        private String status; // COMPLETED, ONGOING, etc.
    }
} 