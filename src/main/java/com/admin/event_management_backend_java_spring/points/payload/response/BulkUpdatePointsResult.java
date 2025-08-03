package com.admin.event_management_backend_java_spring.points.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class BulkUpdatePointsResult {
    private int successCount;
    private int failCount;
    private List<String> failDetails;
    private int totalPointsAwarded;
    private String eventName;
} 