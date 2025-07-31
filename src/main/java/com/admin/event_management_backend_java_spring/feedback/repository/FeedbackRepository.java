package com.admin.event_management_backend_java_spring.feedback.repository;

import com.admin.event_management_backend_java_spring.feedback.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FeedbackRepository extends MongoRepository<Feedback, String> {
} 