package com.admin.event_management_backend_java_spring.analytics.repository;

import com.admin.event_management_backend_java_spring.analytics.model.AnalyticsRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalyticsRepository extends MongoRepository<AnalyticsRecord, String> {
    // Custom query methods nếu cần
} 