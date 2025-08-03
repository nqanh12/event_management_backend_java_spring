package com.admin.event_management_backend_java_spring.registration.service;

import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.registration.payload.request.RegistrationRequest;
import com.admin.event_management_backend_java_spring.registration.payload.response.RegistrationResponse;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import com.admin.event_management_backend_java_spring.audit.service.AuditService;
import java.util.Map;

@Slf4j
@Service
public class RegistrationService {
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditService auditService;

    private RegistrationResponse toRegistrationResponse(Registration reg) {
        RegistrationResponse dto = new RegistrationResponse();
        dto.setId(reg.getId());
        dto.setEventName(reg.getEvent() != null ? reg.getEvent().getName() : null);
        dto.setUserName(reg.getUser() != null ? reg.getUser().getFullName() : null);
        dto.setStatus(reg.getStatus() != null ? reg.getStatus().name() : null);
        dto.setCheckInTime(reg.getCheckInTime());
        dto.setCheckOutTime(reg.getCheckOutTime());
        dto.setPointsAwarded(reg.getPointsAwarded());
        return dto;
    }

    /**
     * Kiểm tra xem có sự kiện nào trùng thời gian với sự kiện hiện tại không
     */
    private boolean hasTimeConflict(Event newEvent, List<Registration> userRegistrations) {
        Date newStartTime = newEvent.getStartTime();
        Date newEndTime = newEvent.getEndTime();
        
        for (Registration registration : userRegistrations) {
            Event existingEvent = registration.getEvent();
            
            // Chỉ kiểm tra các đăng ký có status REGISTERED hoặc ATTENDED
            if (registration.getStatus() == Registration.RegistrationStatus.REGISTERED || 
                registration.getStatus() == Registration.RegistrationStatus.ATTENDED) {
                
                Date existingStartTime = existingEvent.getStartTime();
                Date existingEndTime = existingEvent.getEndTime();
                
                // Kiểm tra trùng lịch: sự kiện mới bắt đầu trong khi sự kiện cũ chưa kết thúc
                // hoặc sự kiện mới kết thúc sau khi sự kiện cũ đã bắt đầu
                if ((newStartTime.before(existingEndTime) && newEndTime.after(existingStartTime)) ||
                    (existingStartTime.before(newEndTime) && existingEndTime.after(newStartTime))) {
                    return true; // Có trùng lịch
                }
            }
        }
        return false; // Không có trùng lịch
    }

    /**
     * Lấy danh sách các sự kiện trùng lịch với sự kiện hiện tại
     */
    private String getConflictingEventsInfo(Event newEvent, List<Registration> userRegistrations) {
        Date newStartTime = newEvent.getStartTime();
        Date newEndTime = newEvent.getEndTime();
        StringBuilder conflictingEvents = new StringBuilder();
        
        for (Registration registration : userRegistrations) {
            Event existingEvent = registration.getEvent();
            
            // Chỉ kiểm tra các đăng ký có status REGISTERED hoặc ATTENDED
            if (registration.getStatus() == Registration.RegistrationStatus.REGISTERED || 
                registration.getStatus() == Registration.RegistrationStatus.ATTENDED) {
                
                Date existingStartTime = existingEvent.getStartTime();
                Date existingEndTime = existingEvent.getEndTime();
                
                // Kiểm tra trùng lịch
                if ((newStartTime.before(existingEndTime) && newEndTime.after(existingStartTime)) ||
                    (existingStartTime.before(newEndTime) && existingEndTime.after(newStartTime))) {
                    
                    if (conflictingEvents.length() > 0) {
                        conflictingEvents.append(", ");
                    }
                    conflictingEvents.append(existingEvent.getName())
                                   .append(" (")
                                   .append(existingStartTime)
                                   .append(" - ")
                                   .append(existingEndTime)
                                   .append(")");
                }
            }
        }
        return conflictingEvents.toString();
    }

    public ApiResponse<RegistrationResponse> registerEvent(String userId, RegistrationRequest req) {
        log.info("[REGISTRATION] User {} đăng ký sự kiện {}", userId, req.getEventId());
        Event event = eventRepository.findById(req.getEventId())
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        // Kiểm tra xem user có được phép đăng ký event này không dựa trên cohort
        if (!event.isUserAllowedToRegister(user)) {
            throw new AppException(ErrorCode.PERMISSION_DENIED, 
                "Bạn không được phép đăng ký sự kiện này. Sự kiện chỉ dành cho khóa học: " + 
                (event.getAllowedCohorts() != null ? event.getAllowedCohorts().toString() : "Không có hạn chế"));
        }
        
        // Kiểm tra xem user đã đăng ký event này chưa
        if (registrationRepository.existsByUserIdAndEventId(userId, req.getEventId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bạn đã đăng ký sự kiện này rồi");
        }
        
        // Kiểm tra trùng lịch/thời gian với các sự kiện đã đăng ký
        List<Registration> userRegistrations = registrationRepository.findByUserId(userId);
        if (hasTimeConflict(event, userRegistrations)) {
            throw new AppException(ErrorCode.BAD_REQUEST, 
                "Không thể đăng ký sự kiện này vì trùng thời gian với sự kiện khác đã đăng ký: " + getConflictingEventsInfo(event, userRegistrations));
        }
        
        // Kiểm tra số lượng người tham gia tối đa
        if (event.getMaxParticipants() != null) {
            long currentRegistrations = registrationRepository.findAll().stream()
                .filter(r -> r.getEvent().getId().equals(req.getEventId()) && 
                           (r.getStatus() == Registration.RegistrationStatus.REGISTERED || 
                            r.getStatus() == Registration.RegistrationStatus.ATTENDED))
                .count();
            
            if (currentRegistrations >= event.getMaxParticipants()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Sự kiện đã đạt số lượng người tham gia tối đa");
            }
        }
        
        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setStatus(Registration.RegistrationStatus.REGISTERED);
        registration.setUpdatedBy(userId);
        registration.setUpdatedAt(new Date());
        registration.setIsDeleted(false);
        registration.setDeletedAt(null);
        registrationRepository.save(registration);
        // Ghi log Audit
        auditService.logActivity(
            "REGISTRATION_CREATE",
            "REGISTRATION",
            registration.getId(),
            "Đăng ký sự kiện: " + event.getName() + " cho user: " + user.getFullName(),
            null,
            Map.of(
                "event", event.getName(),
                "user", user.getFullName(),
                "status", registration.getStatus() != null ? registration.getStatus().name() : null
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Registered successfully", toRegistrationResponse(registration));
    }
    

    
    public ApiResponse<RegistrationResponse> checkIn(String userId, String eventId) {
        log.info("[REGISTRATION] User {} check-in sự kiện {}", userId, eventId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        
        Registration registration = registrationRepository.findByUserIdAndEventId(userId, eventId);
        if (registration == null) {
            throw new AppException(ErrorCode.REGISTRATION_NOT_FOUND, "Bạn chưa đăng ký sự kiện này");
        }
        
        if (registration.getStatus() != Registration.RegistrationStatus.REGISTERED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Trạng thái đăng ký không hợp lệ");
        }
        
        // Kiểm tra thời gian check-in
        Date now = new Date();
        if (now.before(event.getStartTime())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Sự kiện chưa bắt đầu");
        }
        
        // Cho phép check-in từ 30 phút trước khi sự kiện bắt đầu
        Date allowedCheckInTime = new Date(event.getStartTime().getTime() - (30 * 60 * 1000));
        if (now.before(allowedCheckInTime)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chưa tới thời gian check-in");
        }
        
        // Thực hiện check-in
        registration.setCheckInTime(now);
        registration.setStatus(Registration.RegistrationStatus.ATTENDED);
        registration.setUpdatedBy(userId);
        registration.setUpdatedAt(new Date());
        registration = registrationRepository.save(registration);
        // Ghi log Audit
        auditService.logActivity(
            "REGISTRATION_UPDATE",
            "REGISTRATION",
            registration.getId(),
            "Check-in sự kiện: " + event.getName() + " cho user: " + user.getFullName(),
            null,
            Map.of(
                "event", event.getName(),
                "user", user.getFullName(),
                "status", registration.getStatus() != null ? registration.getStatus().name() : null,
                "checkInTime", registration.getCheckInTime()
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Check-in thành công", toRegistrationResponse(registration));
    }

    public ApiResponse<RegistrationResponse> checkOut(String userId, String eventId) {
        log.info("[REGISTRATION] User {} check-out sự kiện {}", userId, eventId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
    
        Registration registration = registrationRepository.findByUserIdAndEventId(userId, eventId);
        if (registration == null) {
            throw new AppException(ErrorCode.REGISTRATION_NOT_FOUND, "Bạn chưa đăng ký sự kiện này");
        }
    
        if (registration.getCheckInTime() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bạn chưa check-in");
        }
    
        if (registration.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bạn đã check-out rồi");
        }
    
        // Thực hiện check-out
        Date now = new Date();
        registration.setCheckOutTime(now);
        registration = registrationRepository.save(registration);
    
        return new ApiResponse<>(true, "Check-out thành công", toRegistrationResponse(registration));
    }

    public ApiResponse<RegistrationResponse> cancelRegistration(String userId, String eventId) {
        Registration reg = registrationRepository.findAll().stream()
            .filter(r -> r.getUser().getId().equals(userId) && r.getEvent().getId().equals(eventId))
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.REGISTRATION_NOT_FOUND, "Registration not found"));
        Event event = reg.getEvent();
        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Sự kiện đã bị huỷ, không thể huỷ đăng ký");
        }
        if (event.getAllowCancelRegistration() == null || !event.getAllowCancelRegistration()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Sự kiện này không cho phép huỷ đăng ký");
        }
        reg.setStatus(Registration.RegistrationStatus.CANCELLED);
        reg.setIsDeleted(true);
        reg.setDeletedAt(new Date());
        reg.setUpdatedBy(userId);
        reg.setUpdatedAt(new Date());
        registrationRepository.save(reg);
        // Ghi log Audit
        auditService.logActivity(
            "REGISTRATION_CANCEL",
            "REGISTRATION",
            reg.getId(),
            "Huỷ đăng ký sự kiện: " + event.getName() + " cho user: " + reg.getUser().getFullName(),
            null,
            Map.of(
                "event", event.getName(),
                "user", reg.getUser().getFullName(),
                "status", reg.getStatus() != null ? reg.getStatus().name() : null
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "Huỷ đăng ký thành công", toRegistrationResponse(reg));
    }

    /**
     * Lấy danh sách đăng ký của user
     */
    public ApiResponse<List<RegistrationResponse>> getUserRegistrations(String userId) {
        List<Registration> regs = registrationRepository.findByUserIdAndIsDeletedFalse(userId);
        List<RegistrationResponse> responses = regs.stream().map(this::toRegistrationResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    /**
     * Lấy danh sách đăng ký của user theo status
     */
    public ApiResponse<List<RegistrationResponse>> getUserRegistrationsByStatus(String userId, Registration.RegistrationStatus status) {
        List<Registration> regs = registrationRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, status);
        List<RegistrationResponse> responses = regs.stream().map(this::toRegistrationResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    public ApiResponse<List<RegistrationResponse>> getEventRegistrations(String eventId) {
        List<Registration> regs = registrationRepository.findByEventIdAndIsDeletedFalse(eventId);
        List<RegistrationResponse> responses = regs.stream().map(this::toRegistrationResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }
}