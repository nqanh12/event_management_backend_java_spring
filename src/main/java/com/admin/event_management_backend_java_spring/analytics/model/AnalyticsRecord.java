package com.admin.event_management_backend_java_spring.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "analytics_records")
public class AnalyticsRecord {
    @Id
    private String id;
    private String type;
    private int value;
    private LocalDateTime timestamp;

} 