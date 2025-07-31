package com.admin.event_management_backend_java_spring.event.service;

import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.event.payload.request.EventRequest;
import com.admin.event_management_backend_java_spring.event.payload.response.EventResponse;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import com.admin.event_management_backend_java_spring.integration.MailService;
import com.admin.event_management_backend_java_spring.notification.payload.request.NotificationRequest;
import com.admin.event_management_backend_java_spring.notification.service.NotificationService;
import com.admin.event_management_backend_java_spring.audit.service.AuditService;
import java.util.*;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuditService auditService;


    public ApiResponse<EventResponse> createEvent(EventRequest req, String organizerId) {
        log.info("[EVENT] Tạo sự kiện mới: {} - {}", req.getName(), req.getType());
        Department department = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Event event = new Event();
        event.setName(req.getName());
        event.setType(Event.EventType.valueOf(req.getType()));
        event.setStartTime(req.getStartTime());
        event.setEndTime(req.getEndTime());
        event.setLocation(req.getLocation());
        event.setDepartment(department);
        event.setOrganizer(organizer);
        event.setStatus(Event.EventStatus.PENDING);
        event.setMaxParticipants(req.getMaxParticipants());
        event.setNote(req.getNote());
        event.setAllowCancelRegistration(req.getAllowCancelRegistration() != null ? req.getAllowCancelRegistration() : true);
        event.setAllowedCohorts(req.getAllowedCohorts());
        event.setAllowAllCohorts(req.getAllowAllCohorts() != null ? req.getAllowAllCohorts() : true);
        if (req.getScope() != null) {
            event.setScope(Event.EventScope.valueOf(req.getScope()));
        }
        event.setCreatedBy(organizerId);
        event.setCreatedAt(new Date());
        event.setUpdatedBy(organizerId);
        event.setUpdatedAt(new Date());
        event.setIsDeleted(false);
        event.setDeletedAt(null);
        eventRepository.save(event);
        // Ghi log Audit
        auditService.logActivity(
            "EVENT_CREATE",
            "EVENT",
            event.getId(),
            "Tạo sự kiện mới: " + event.getName(),
            null,
            Map.of(
                "name", event.getName(),
                "type", event.getType() != null ? event.getType().name() : null,
                "status", event.getStatus() != null ? event.getStatus().name() : null,
                "department", department.getName(),
                "organizer", organizer.getFullName()
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Event created", toEventResponse(event));
    }



    public ApiResponse<List<EventResponse>> getAllEvents() {
        List<Event> events = eventRepository.findByIsDeletedFalse();
        List<EventResponse> responses = events.stream().map(this::toEventResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    public ApiResponse<List<EventResponse>> getEventsByFilter(Event.EventStatus status, Event.EventType type, String departmentId) {
        List<Event> events;
        if (status != null && type != null && departmentId != null) {
            events = eventRepository.findByDepartmentIdAndStatusAndIsDeletedFalse(departmentId, status, null).getContent();
        } else if (status != null && type != null) {
            events = eventRepository.findByStatusAndTypeAndIsDeletedFalse(status, type, null).getContent();
        } else if (status != null) {
            events = eventRepository.findByStatusAndIsDeletedFalse(status, null).getContent();
        } else if (type != null) {
            events = eventRepository.findByTypeAndIsDeletedFalse(type, null).getContent();
        } else if (departmentId != null) {
            events = eventRepository.findByDepartmentIdAndIsDeletedFalse(departmentId, null).getContent();
        } else {
            events = eventRepository.findByIsDeletedFalse();
        }
        List<EventResponse> responses = events.stream().map(this::toEventResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    public ApiResponse<EventResponse> approveEvent(String eventId, String approverId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        event.setStatus(Event.EventStatus.APPROVED);
        eventRepository.save(event);
        sendEventApprovedEmail(event);
        return new ApiResponse<>(true, "Event approved", toEventResponse(event));
    }

    private void sendEventApprovedEmail(Event event) {
        String subject = "[Event Management] Sự kiện mới được đăng: " + event.getName();
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.getRole() == User.UserRole.STUDENT &&
                (event.getScope() == Event.EventScope.SCHOOL ||
                 (event.getScope() == Event.EventScope.DEPARTMENT && u.getDepartment() != null && event.getDepartment() != null && u.getDepartment().getId().equals(event.getDepartment().getId())))) {
                Map<String, String> vars = new HashMap<>();
                vars.put("event_name", event.getName());
                vars.put("event_time", event.getStartTime() + " - " + event.getEndTime());
                vars.put("event_location", event.getLocation());
                vars.put("event_capacity", event.getMaxParticipants() != null ? event.getMaxParticipants().toString() : "");
                vars.put("event_status", event.getStatus().name());
                vars.put("countdown_timer", ""); // Có thể tính toán thêm
                vars.put("registration_link", "https://your-frontend-url/event/" + event.getId());
                vars.put("event_details_link", "https://your-frontend-url/event/" + event.getId());
                mailService.sendHtmlMail(u.getEmail(), subject, "event-notification.html", vars);
                NotificationRequest req = new NotificationRequest();
                req.setUserId(u.getId());
                req.setTitle("Sự kiện mới: " + event.getName());
                req.setContent("Bạn có sự kiện mới: " + event.getName());
                req.setType("EVENT");
                notificationService.sendNotification(req);
            }
        }
    }

    private EventResponse toEventResponse(Event event) {
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setType(event.getType() != null ? event.getType().name() : null);
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        dto.setDepartmentName(event.getDepartment() != null ? event.getDepartment().getName() : null);
        dto.setOrganizerName(event.getOrganizer() != null ? event.getOrganizer().getFullName() : null);
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setNote(event.getNote());
        dto.setAllowCancelRegistration(event.getAllowCancelRegistration());
        dto.setAllowedCohorts(event.getAllowedCohorts());
        dto.setAllowAllCohorts(event.getAllowAllCohorts());
        return dto;
    }

    public ApiResponse<EventResponse> updateEvent(String eventId, EventRequest req, String userId) {
        log.info("[EVENT] Cập nhật sự kiện id: {} - {}", eventId, req.getName());
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        // Lưu giá trị cũ để log
        Map<String, Object> oldValues = Map.of(
            "name", event.getName(),
            "type", event.getType() != null ? event.getType().name() : null,
            "status", event.getStatus() != null ? event.getStatus().name() : null,
            "department", event.getDepartment() != null ? event.getDepartment().getName() : null,
            "organizer", event.getOrganizer() != null ? event.getOrganizer().getFullName() : null
        );
        // Chỉ organizer hoặc admin mới được sửa
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!event.getOrganizer().getId().equals(userId) && user.getRole() != User.UserRole.GLOBAL_ADMIN && user.getRole() != User.UserRole.FACULTY_ADMIN && user.getRole() != User.UserRole.SCHOOL_MANAGER) {
            throw new AppException(ErrorCode.PERMISSION_DENIED, "Bạn không có quyền cập nhật sự kiện này");
        }
        Department department = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        
        event.setName(req.getName());
        event.setType(Event.EventType.valueOf(req.getType()));
        event.setStartTime(req.getStartTime());
        event.setEndTime(req.getEndTime());
        event.setLocation(req.getLocation());
        event.setDepartment(department);
        event.setMaxParticipants(req.getMaxParticipants());
        event.setNote(req.getNote());
        event.setAllowCancelRegistration(req.getAllowCancelRegistration() != null ? req.getAllowCancelRegistration() : true);
        event.setAllowedCohorts(req.getAllowedCohorts());
        event.setAllowAllCohorts(req.getAllowAllCohorts() != null ? req.getAllowAllCohorts() : true);
        if (req.getScope() != null) {
            event.setScope(Event.EventScope.valueOf(req.getScope()));
        }
        event.setUpdatedBy(userId);
        event.setUpdatedAt(new Date());
        eventRepository.save(event);
        // Ghi log Audit
        auditService.logActivity(
            "EVENT_UPDATE",
            "EVENT",
            event.getId(),
            "Cập nhật sự kiện: " + event.getName(),
            oldValues,
            Map.of(
                "name", event.getName(),
                "type", event.getType() != null ? event.getType().name() : null,
                "status", event.getStatus() != null ? event.getStatus().name() : null,
                "department", department.getName(),
                "organizer", user.getFullName()
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Cập nhật sự kiện thành công", toEventResponse(event));
    }

    public ApiResponse<EventResponse> cancelEvent(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!event.getOrganizer().getId().equals(userId) && user.getRole() != User.UserRole.GLOBAL_ADMIN && user.getRole() != User.UserRole.FACULTY_ADMIN && user.getRole() != User.UserRole.SCHOOL_MANAGER) {
            throw new AppException(ErrorCode.PERMISSION_DENIED, "Bạn không có quyền huỷ sự kiện này");
        }
        event.setStatus(Event.EventStatus.CANCELLED);
        event.setIsDeleted(true);
        event.setDeletedAt(new Date());
        event.setUpdatedBy(userId);
        event.setUpdatedAt(new Date());
        eventRepository.save(event);
        // Ghi log Audit
        auditService.logActivity(
            "EVENT_CANCEL",
            "EVENT",
            event.getId(),
            "Huỷ sự kiện: " + event.getName(),
            null,
            Map.of(
                "name", event.getName(),
                "status", event.getStatus() != null ? event.getStatus().name() : null,
                "department", event.getDepartment() != null ? event.getDepartment().getName() : null,
                "organizer", user.getFullName()
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Đã huỷ sự kiện", toEventResponse(event));
    }
}
