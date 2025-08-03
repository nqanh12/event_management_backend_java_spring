package com.admin.event_management_backend_java_spring.notification.repository;

import com.admin.event_management_backend_java_spring.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
} 