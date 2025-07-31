package com.admin.event_management_backend_java_spring.user.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class BulkCreateUserResult {
    private int successCount;
    private int failCount;
    private List<String> failDetails;
} 