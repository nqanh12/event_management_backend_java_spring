package com.admin.event_management_backend_java_spring.feedback.service;

import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.feedback.model.Feedback;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.feedback.payload.request.FeedbackRequest;
import com.admin.event_management_backend_java_spring.feedback.payload.response.FeedbackResponse;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.feedback.repository.FeedbackRepository;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        FeedbackResponse dto = new FeedbackResponse();
        dto.setId(feedback.getId());
        dto.setUserName(feedback.getUser() != null ? feedback.getUser().getFullName() : null);
        dto.setEventName(feedback.getEvent() != null ? feedback.getEvent().getName() : null);
        dto.setContent(feedback.getContent());
        dto.setResponse(feedback.getResponse());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }

    public ApiResponse<?> sendFeedback(String userId, FeedbackRequest req) {
        log.info("[FEEDBACK] User {} gửi feedback cho event {}", userId, req.getEventId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        Event event = eventRepository.findById(req.getEventId())
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEvent(event);
        feedback.setContent(req.getContent());
        feedback.setCreatedAt(new java.util.Date());
        feedbackRepository.save(feedback);
        return new ApiResponse<>(true, "Feedback sent", toFeedbackResponse(feedback));
    }

    public ApiResponse<?> getFeedbacksByEvent(String eventId) {
        log.info("[FEEDBACK] Lấy feedback cho event {}", eventId);
        List<FeedbackResponse> feedbacks = feedbackRepository.findAll().stream()
            .filter(f -> f.getEvent().getId().equals(eventId))
            .map(this::toFeedbackResponse)
            .toList();
        return new ApiResponse<>(true, "Success", feedbacks);
    }

    public ApiResponse<?> getFeedbacksByUser(String userId) {
        log.info("[FEEDBACK] Lấy feedback cho user {}", userId);
        List<FeedbackResponse> feedbacks = feedbackRepository.findAll().stream()
            .filter(f -> f.getUser().getId().equals(userId))
            .map(this::toFeedbackResponse)
            .toList();
        return new ApiResponse<>(true, "Success", feedbacks);
    }
} 