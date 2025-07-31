package com.admin.event_management_backend_java_spring.points.service;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.notification.payload.request.NotificationRequest;
import com.admin.event_management_backend_java_spring.payload.response.EventPointsReportResponse;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import com.admin.event_management_backend_java_spring.points.payload.request.ManualPointsProcessingRequest;
import com.admin.event_management_backend_java_spring.points.payload.request.UpdatePointsRequest;
import com.admin.event_management_backend_java_spring.points.payload.response.BulkUpdatePointsResult;
import com.admin.event_management_backend_java_spring.points.payload.response.ManualPointsProcessingResult;
import com.admin.event_management_backend_java_spring.points.payload.response.PointsProcessingDashboardResponse;
import com.admin.event_management_backend_java_spring.points.repository.PointsHistoryRepository;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.payload.response.UserPointsResponse;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.notification.service.NotificationService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.academic.service.AcademicCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import com.admin.event_management_backend_java_spring.points.payload.request.BulkUpdatePointsRequest;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PointsService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PointsHistoryRepository pointsHistoryRepository;
    @Autowired
    private AcademicCalendarService academicCalendarService;

    /**
     * Tự động cập nhật điểm cho tất cả sự kiện đã kết thúc
     * Chạy mỗi giờ một lần
     */
    @Scheduled(fixedRate = 3600000) // 1 giờ = 3600000ms
    public void autoUpdatePointsForCompletedEvents() {
        Date now = new Date();
        List<Event> completedEvents = eventRepository.findAll().stream()
            .filter(e -> e.getStatus() == Event.EventStatus.ONGOING &&
                        e.getEndTime().before(now))
            .collect(java.util.stream.Collectors.toList());

        for (Event event : completedEvents) {
            try {
                // Cập nhật trạng thái sự kiện thành COMPLETED
                event.setStatus(Event.EventStatus.COMPLETED);
                eventRepository.save(event);

                // Tự động cộng điểm cho tất cả người tham gia
                autoAwardPointsForEvent(event);
            } catch (Exception e) {
                // Log lỗi nhưng không dừng quá trình
                System.err.println("Error processing event " + event.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Tự động xử lý điểm cho tất cả người tham gia một sự kiện
     */
    private void autoAwardPointsForEvent(Event event) {
        List<Registration> allRegistrations = registrationRepository.findAll().stream()
            .filter(r -> r.getEvent().getId().equals(event.getId()) &&
                        r.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING) // Chỉ xử lý những người chưa được xử lý
            .collect(java.util.stream.Collectors.toList());

        int totalPointsAwarded = 0;
        int successCount = 0;
        int pendingCount = 0;

        for (Registration reg : allRegistrations) {
            try {
                User user = reg.getUser();

                // Kiểm tra xem có tham gia đầy đủ không (check-in và check-out)
                boolean fullyAttended = isFullyAttended(reg);

                if (fullyAttended) {
                    // Cộng điểm cho người tham gia đầy đủ
                    int pointsToAward = calculatePointsForEvent(event);

                    if (event.getType() == Event.EventType.TRAINING) {
                        // Sử dụng học kỳ hiện tại dựa trên niên khóa học
                        Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                        addTrainingPoints(user, currentSemester, (double) pointsToAward,
                            "Tự động cộng điểm từ sự kiện", event.getId(), event.getName());
                    } else if (event.getType() == Event.EventType.SOCIAL) {
                        addSocialPoints(user, (double) pointsToAward,
                            "Tự động cộng điểm từ sự kiện", event.getId(), event.getName());
                    }

                    reg.setPointsAwarded(pointsToAward);
                    reg.setPointsProcessingStatus(Registration.PointsProcessingStatus.AUTO_AWARDED);
                    reg.setPointsProcessingReason("Tự động cộng điểm - tham gia đầy đủ");
                    totalPointsAwarded += pointsToAward;
                    successCount++;

                    // Gửi thông báo cộng điểm
                    sendPointsNotification(user, event, pointsToAward, true);

                } else {
                    // Đối với người không tham gia đầy đủ, để PENDING để admin xử lý thủ công
                    reg.setPointsProcessingStatus(Registration.PointsProcessingStatus.PENDING);
                    reg.setPointsProcessingReason("Chờ xử lý thủ công - tham gia không đầy đủ");
                    pendingCount++;
                }

                // Lưu thay đổi
                userRepository.save(user);
                registrationRepository.save(reg);

            } catch (Exception e) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi xử lý điểm cho user: " + reg.getUser().getFullName() + ". " + e.getMessage());
            }
        }

        System.out.println("Event: " + event.getName() +
                          " - Auto awarded: " + totalPointsAwarded + " points to " + successCount + " users" +
                          " - Pending manual review: " + pendingCount + " users");
    }

    /**
     * Kiểm tra xem người tham gia có tham gia đầy đủ không
     */
    private boolean isFullyAttended(Registration registration) {
        return registration.getCheckInTime() != null &&
               registration.getCheckOutTime() != null &&
               registration.getStatus() == Registration.RegistrationStatus.ATTENDED;
    }

    /**
     * Tính điểm trừ dựa trên cấu hình của department
     */
    private int calculatePenaltyPoints(Event event) {
        Department department = event.getDepartment();
        if (department == null) {
            // Mặc định nếu không có department
            return event.getType() == Event.EventType.TRAINING ? 2 : 1;
        }

        if (event.getType() == Event.EventType.TRAINING) {
            return department.getTrainingPointsPenalty() != null ? department.getTrainingPointsPenalty() : 2;
        } else {
            return department.getSocialPointsPenalty() != null ? department.getSocialPointsPenalty() : 1;
        }
    }

    /**
     * Tính điểm cho sự kiện dựa trên loại sự kiện và custom points (nếu có)
     */
    private int calculatePointsForEvent(Event event) {
        if (Boolean.TRUE.equals(event.getUseCustomPoints())) {
            if (event.getType() == Event.EventType.TRAINING && event.getTrainingPointsReward() != null) {
                return event.getTrainingPointsReward();
            }
            if (event.getType() == Event.EventType.SOCIAL && event.getSocialPointsReward() != null) {
                return event.getSocialPointsReward();
            }
            // Nếu không có điểm thưởng cụ thể, trả về 0
            return 0;
        }
        // Mặc định: không tính theo giờ nữa
        if (event.getType() == Event.EventType.TRAINING) {
            return 4;
        } else if (event.getType() == Event.EventType.SOCIAL) {
            return 10;
        }
        return 0;
    }

    /**
     * Gửi thông báo cho user về điểm đã nhận hoặc bị trừ
     */
    private void sendPointsNotification(User user, Event event, int points, boolean isAwarded) {
        String title = isAwarded ? "Điểm rèn luyện mới" : "Điểm bị trừ";
        String content;

        if (isAwarded) {
            if (event.getType() == Event.EventType.TRAINING) {
                Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                Double currentPoints = getTrainingPointsBySemester(user, currentSemester);
                content = String.format(
                    "Bạn đã nhận được %d điểm rèn luyện từ sự kiện '%s'. Điểm hiện tại %s: %.1f điểm.",
                    points, event.getName(), currentSemester.getDisplayName(), currentPoints
                );
            } else {
                content = String.format(
                    "Bạn đã nhận được %d điểm hoạt động xã hội từ sự kiện '%s'. Tổng điểm hiện tại: %.1f điểm.",
                    points, event.getName(), user.getSocialPoints()
                );
            }
        } else {
            if (event.getType() == Event.EventType.TRAINING) {
                Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                Double currentPoints = getTrainingPointsBySemester(user, currentSemester);
                content = String.format(
                    "Bạn đã bị trừ %d điểm rèn luyện do không tham gia đầy đủ sự kiện '%s'. Điểm hiện tại %s: %.1f điểm.",
                    points, event.getName(), currentSemester.getDisplayName(), currentPoints
                );
            } else {
                content = String.format(
                    "Bạn đã bị trừ %d điểm hoạt động xã hội do không tham gia đầy đủ sự kiện '%s'. Tổng điểm hiện tại: %.1f điểm.",
                    points, event.getName(), user.getSocialPoints()
                );
            }
        }

        // Tạo notification
        NotificationRequest notificationReq = new NotificationRequest();
        notificationReq.setRecipientId(user.getId());
        notificationReq.setTitle(title);
        notificationReq.setContent(content);

        notificationService.sendNotification(notificationReq);
    }

    /**
     * Cập nhật điểm thủ công cho một sự kiện cụ thể
     */
    public ApiResponse<BulkUpdatePointsResult> manualUpdatePointsForEvent(String eventId, Integer trainingPointsToAdd, Integer socialPointsToAdd) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        List<Registration> registrations = registrationRepository.findAll().stream()
            .filter(r -> r.getEvent().getId().equals(eventId) &&
                        r.getStatus() == Registration.RegistrationStatus.ATTENDED)
            .collect(java.util.stream.Collectors.toList());

        int successCount = 0;
        int failCount = 0;
        int totalPointsAwarded = 0;
        List<String> failDetails = new ArrayList<>();

        for (Registration reg : registrations) {
            try {
                User user = reg.getUser();
                int pointsToAdd = 0;

                if (trainingPointsToAdd != null && event.getType() == Event.EventType.TRAINING) {
                    // Sử dụng học kỳ hiện tại dựa trên niên khóa học
                    Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                    addTrainingPoints(user, currentSemester, (double) trainingPointsToAdd,
                        "Cập nhật thủ công từ admin", event.getId(), event.getName());
                    pointsToAdd = trainingPointsToAdd;
                } else if (socialPointsToAdd != null && event.getType() == Event.EventType.SOCIAL) {
                    addSocialPoints(user, (double) socialPointsToAdd,
                        "Cập nhật thủ công từ admin", event.getId(), event.getName());
                    pointsToAdd = socialPointsToAdd;
                }

                if (pointsToAdd > 0) {
                    // Cập nhật pointsAwarded nếu chưa có
                    if (reg.getPointsAwarded() == null) {
                        reg.setPointsAwarded(pointsToAdd);
                    } else {
                        reg.setPointsAwarded(reg.getPointsAwarded() + pointsToAdd);
                    }

                    totalPointsAwarded += pointsToAdd;
                    userRepository.save(user);
                    registrationRepository.save(reg);
                    successCount++;

                    // Gửi thông báo
                    sendPointsNotification(user, event, pointsToAdd, true);
                }
            } catch (Exception e) {
                failCount++;
                failDetails.add("User " + reg.getUser().getFullName() + ": " + e.getMessage());
            }
        }

        BulkUpdatePointsResult result = new BulkUpdatePointsResult();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setFailDetails(failDetails);
        result.setTotalPointsAwarded(totalPointsAwarded);
        result.setEventName(event.getName());

        return new ApiResponse<>(true, "Manual update completed", result);
    }

    /**
     * Tạo báo cáo chi tiết về điểm của một sự kiện
     */
    public ApiResponse<EventPointsReportResponse> getEventPointsReport(String eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        List<Registration> registrations = registrationRepository.findAll().stream()
            .filter(r -> r.getEvent().getId().equals(eventId))
            .collect(java.util.stream.Collectors.toList());

        EventPointsReportResponse report = new EventPointsReportResponse();
        report.setEventId(eventId);
        report.setEventName(event.getName());
        report.setEventType(event.getType().name());
        report.setTotalRegistrations(registrations.size());

        int fullyAttendedCount = 0;
        int partiallyAttendedCount = 0;
        int absentCount = 0;
        int totalPointsAwarded = 0;
        int totalPointsPenalized = 0;

        List<EventPointsReportResponse.ParticipantPointsDetail> participants = new ArrayList<>();

        for (Registration reg : registrations) {
            EventPointsReportResponse.ParticipantPointsDetail detail = new EventPointsReportResponse.ParticipantPointsDetail();
            detail.setUserId(reg.getUser().getId());
            detail.setUserName(reg.getUser().getFullName());
            detail.setUserEmail(reg.getUser().getEmail());
            detail.setHasCheckIn(reg.getCheckInTime() != null);
            detail.setHasCheckOut(reg.getCheckOutTime() != null);
            detail.setFullyAttended(isFullyAttended(reg));
            detail.setPointsAwarded(reg.getPointsAwarded());
            detail.setStatus(reg.getStatus().name());

            if (detail.isFullyAttended()) {
                fullyAttendedCount++;
                if (reg.getPointsAwarded() != null && reg.getPointsAwarded() > 0) {
                    totalPointsAwarded += reg.getPointsAwarded();
                }
            } else if (reg.getCheckInTime() != null || reg.getCheckOutTime() != null) {
                partiallyAttendedCount++;
            } else {
                absentCount++;
            }

            if (reg.getPointsAwarded() != null && reg.getPointsAwarded() < 0) {
                totalPointsPenalized += Math.abs(reg.getPointsAwarded());
            }

            participants.add(detail);
        }

        report.setFullyAttendedCount(fullyAttendedCount);
        report.setPartiallyAttendedCount(partiallyAttendedCount);
        report.setAbsentCount(absentCount);
        report.setTotalPointsAwarded(totalPointsAwarded);
        report.setTotalPointsPenalized(totalPointsPenalized);
        report.setNetPoints(totalPointsAwarded - totalPointsPenalized);
        report.setParticipants(participants);

        return new ApiResponse<>(true, "Event points report generated", report);
    }

    /**
     * Xử lý điểm thủ công cho các trường hợp đặc biệt
     */
    @Transactional
    public ApiResponse<ManualPointsProcessingResult> manualProcessPoints(ManualPointsProcessingRequest req, String adminId) {
        Event event = eventRepository.findById(req.getEventId())
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        // Lọc registrations cần xử lý
        List<Registration> registrationsToProcess;
        if (req.getUserIds() != null && !req.getUserIds().isEmpty()) {
            // Xử lý cho danh sách user cụ thể
            registrationsToProcess = registrationRepository.findAll().stream()
                .filter(r -> r.getEvent().getId().equals(req.getEventId()) &&
                            req.getUserIds().contains(r.getUser().getId()) &&
                            r.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());
        } else {
            // Xử lý tất cả trường hợp PENDING
            registrationsToProcess = registrationRepository.findAll().stream()
                .filter(r -> r.getEvent().getId().equals(req.getEventId()) &&
                            r.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());
        }

        int successCount = 0;
        int failCount = 0;
        int totalPointsAwarded = 0;
        int totalPointsPenalized = 0;
        List<String> failDetails = new ArrayList<>();
        List<ManualPointsProcessingResult.ProcessedUserDetail> processedUsers = new ArrayList<>();

        for (Registration reg : registrationsToProcess) {
            try {
                User user = reg.getUser();
                int pointsToProcess = 0;
                Registration.PointsProcessingStatus newStatus = null;

                switch (req.getAction()) {
                    case AWARD:
                        // Cộng điểm
                        pointsToProcess = req.getCustomPoints() != null ? req.getCustomPoints() : calculatePointsForEvent(event);

                        if (event.getType() == Event.EventType.TRAINING) {
                            // Sử dụng học kỳ hiện tại dựa trên niên khóa học
                            Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                            addTrainingPoints(user, currentSemester, (double) pointsToProcess,
                                "Xử lý thủ công từ admin", event.getId(), event.getName());
                        } else if (event.getType() == Event.EventType.SOCIAL) {
                            addSocialPoints(user, (double) pointsToProcess,
                                "Xử lý thủ công từ admin", event.getId(), event.getName());
                        }

                        reg.setPointsAwarded(pointsToProcess);
                        newStatus = Registration.PointsProcessingStatus.MANUAL_AWARDED;
                        totalPointsAwarded += pointsToProcess;

                        // Gửi thông báo cộng điểm
                        sendPointsNotification(user, event, pointsToProcess, true);
                        break;

                    case PENALIZE:
                        // Trừ điểm
                        pointsToProcess = req.getCustomPoints() != null ? req.getCustomPoints() : calculatePenaltyPoints(event);

                        if (event.getType() == Event.EventType.TRAINING) {
                            // Trừ điểm rèn luyện từ kỳ học hiện tại
                            Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                            Double currentPoints = getTrainingPointsBySemester(user, currentSemester);
                            Double newPoints = Math.max(0, currentPoints - pointsToProcess);
                            updateTrainingPointsBySemester(user, currentSemester, newPoints);
                        } else if (event.getType() == Event.EventType.SOCIAL) {
                            // Trừ điểm hoạt động xã hội
                            Double newPoints = Math.max(0, user.getSocialPoints() - pointsToProcess);
                            user.setSocialPoints(newPoints);
                        }

                        reg.setPointsAwarded(-pointsToProcess);
                        newStatus = Registration.PointsProcessingStatus.MANUAL_PENALIZED;
                        totalPointsPenalized += pointsToProcess;

                        // Gửi thông báo trừ điểm
                        sendPointsNotification(user, event, pointsToProcess, false);
                        break;

                    case IGNORE:
                        // Bỏ qua - không cộng không trừ
                        reg.setPointsAwarded(0);
                        newStatus = Registration.PointsProcessingStatus.MANUAL_IGNORED;
                        break;
                }

                reg.setPointsProcessingStatus(newStatus);
                reg.setPointsProcessingReason(req.getReason());
                reg.setProcessedBy(adminId);
                reg.setProcessedAt(new Date());

                // Lưu thay đổi
                userRepository.save(user);
                registrationRepository.save(reg);

                // Thêm vào danh sách đã xử lý
                ManualPointsProcessingResult.ProcessedUserDetail detail = new ManualPointsProcessingResult.ProcessedUserDetail();
                detail.setUserId(user.getId());
                detail.setUserName(user.getFullName());
                detail.setUserEmail(user.getEmail());
                detail.setHasCheckIn(reg.getCheckInTime() != null);
                detail.setHasCheckOut(reg.getCheckOutTime() != null);
                detail.setPointsAwarded(reg.getPointsAwarded());
                detail.setProcessingStatus(newStatus.name());
                detail.setReason(req.getReason());
                processedUsers.add(detail);

                successCount++;

            } catch (Exception e) {
                failCount++;
                failDetails.add("User " + reg.getUser().getFullName() + ": " + e.getMessage());
            }
        }

        ManualPointsProcessingResult result = new ManualPointsProcessingResult();
        result.setEventId(req.getEventId());
        result.setEventName(event.getName());
        result.setAction(req.getAction().name());
        result.setReason(req.getReason());
        result.setDescription(req.getDescription());
        result.setTotalProcessed(registrationsToProcess.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setTotalPointsAwarded(totalPointsAwarded);
        result.setTotalPointsPenalized(totalPointsPenalized);
        result.setFailDetails(failDetails);
        result.setProcessedUsers(processedUsers);

        return new ApiResponse<>(true, "Manual points processing completed", result);
    }

    /**
     * Lấy danh sách registrations cần xử lý thủ công
     */
    public ApiResponse<List<Registration>> getPendingManualProcessing(String eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        List<Registration> pendingRegistrations = registrationRepository.findAll().stream()
            .filter(r -> r.getEvent().getId().equals(eventId) &&
                        r.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());

        return new ApiResponse<>(true, "Pending manual processing registrations", pendingRegistrations);
    }

    /**
     * Lấy dashboard tổng quan về xử lý điểm
     */
    public ApiResponse<PointsProcessingDashboardResponse> getPointsProcessingDashboard() {
        List<Event> allEvents = eventRepository.findAll();
        List<Registration> allRegistrations = registrationRepository.findAll();

        PointsProcessingDashboardResponse dashboard = new PointsProcessingDashboardResponse();
        dashboard.setTotalEvents(allEvents.size());

        // Đếm sự kiện có pending processing
        long eventsWithPending = allEvents.stream()
            .filter(event -> allRegistrations.stream()
                .anyMatch(reg -> reg.getEvent().getId().equals(event.getId()) &&
                               reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING))
            .count();
        dashboard.setEventsWithPendingProcessing((int) eventsWithPending);

        // Đếm tổng số registrations pending
        long totalPending = allRegistrations.stream()
            .filter(reg -> reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
            .count();
        dashboard.setTotalPendingRegistrations((int) totalPending);

        // Thống kê theo trạng thái xử lý
        Map<String, Long> statusCounts = allRegistrations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                reg -> reg.getPointsProcessingStatus() != null ? reg.getPointsProcessingStatus().name() : "NULL",
                java.util.stream.Collectors.counting()
            ));
        dashboard.setProcessingStatusCounts(statusCounts);

        // Tóm tắt từng sự kiện
        List<PointsProcessingDashboardResponse.EventSummary> eventsSummary = allEvents.stream()
            .map(event -> {
                PointsProcessingDashboardResponse.EventSummary summary = new PointsProcessingDashboardResponse.EventSummary();
                summary.setEventId(event.getId());
                summary.setEventName(event.getName());
                summary.setEventType(event.getType().name());
                summary.setStatus(event.getStatus().name());

                List<Registration> eventRegistrations = allRegistrations.stream()
                    .filter(reg -> reg.getEvent().getId().equals(event.getId()))
                    .collect(java.util.stream.Collectors.toList());

                summary.setTotalRegistrations(eventRegistrations.size());
                summary.setPendingCount((int) eventRegistrations.stream()
                    .filter(reg -> reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
                    .count());
                summary.setAutoAwardedCount((int) eventRegistrations.stream()
                    .filter(reg -> reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.AUTO_AWARDED)
                    .count());
                summary.setManualProcessedCount((int) eventRegistrations.stream()
                    .filter(reg -> reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.MANUAL_AWARDED ||
                                 reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.MANUAL_PENALIZED ||
                                 reg.getPointsProcessingStatus() == Registration.PointsProcessingStatus.MANUAL_IGNORED)
                    .count());

                return summary;
            })
            .collect(java.util.stream.Collectors.toList());

        dashboard.setEventsSummary(eventsSummary);

        return new ApiResponse<>(true, "Points processing dashboard", dashboard);
    }

    @Async("pointsTaskExecutor")
    public CompletableFuture<BulkUpdatePointsResult> bulkUpdatePointsAsync(String eventId, Integer trainingPointsToAdd, Integer socialPointsToAdd) {
        try {
            ApiResponse<BulkUpdatePointsResult> result = manualUpdatePointsForEvent(eventId, trainingPointsToAdd, socialPointsToAdd);
            return CompletableFuture.completedFuture(result.getData());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("pointsTaskExecutor")
    public CompletableFuture<ManualPointsProcessingResult> bulkProcessPointsAsync() {
        try {
            List<Registration> pendingRegistrations = registrationRepository.findAll().stream()
                .filter(r -> r.getPointsProcessingStatus() == Registration.PointsProcessingStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());

            for (Registration reg : pendingRegistrations) {
                try {
                    Event event = reg.getEvent();
                    User user = reg.getUser();

                    // Xử lý điểm cho từng registration
                    if (isFullyAttended(reg)) {
                        int pointsToAward = calculatePointsForEvent(event);

                        if (event.getType() == Event.EventType.TRAINING) {
                            // Sử dụng kỳ học hiện tại (đã fix: không mặc định SEMESTER_1)
                            Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                            addTrainingPoints(user, currentSemester, (double) pointsToAward,
                                "Tự động cộng điểm từ sự kiện", event.getId(), event.getName());
                        } else if (event.getType() == Event.EventType.SOCIAL) {
                            addSocialPoints(user, (double) pointsToAward,
                                "Tự động cộng điểm từ sự kiện", event.getId(), event.getName());
                        }

                        reg.setPointsAwarded(pointsToAward);
                        reg.setPointsProcessingStatus(Registration.PointsProcessingStatus.AUTO_AWARDED);
                        reg.setPointsProcessingReason("Tự động xử lý hàng loạt");

                        userRepository.save(user);
                        registrationRepository.save(reg);

                        // Gửi thông báo
                        sendPointsNotification(user, event, pointsToAward, true);
                    }
                } catch (Exception e) {
                    throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi xử lý điểm cho registration: " + reg.getId() + ". " + e.getMessage());
                }
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // ========== CÁC METHOD MỚI CHO CẤU TRÚC ĐIỂM MỚI ==========

    /**
     * Cập nhật điểm cho user (training hoặc social points)
     */
    public ApiResponse<UserPointsResponse> updatePoints(UpdatePointsRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        boolean updated = false;
        String reason = req.getReason() != null ? req.getReason() : "Cập nhật điểm thủ công";
        String description = req.getDescription();
        Semester semester = req.getSemester();
        LocalDateTime now = java.time.LocalDateTime.now();
        // Cập nhật điểm rèn luyện theo kỳ học nếu có
        if (req.getTrainingPoints() != null && semester != null) {
            Double oldPoints = getTrainingPointsBySemester(user, semester);
            Double newPoints = req.getTrainingPoints();
            updateTrainingPointsBySemester(user, semester, newPoints);
            PointsHistory history = new PointsHistory(
                    null, user.getId(), user.getEmail(), user.getFullName(),
                    PointsHistory.PointsType.TRAINING_POINTS, semester,
                    oldPoints, newPoints, newPoints - oldPoints, reason, description, "ADMIN", now, null, null
            );
            pointsHistoryRepository.save(history);
            updated = true;
        }
        // Cập nhật điểm hoạt động xã hội nếu có
        if (req.getSocialPoints() != null) {
            Double oldPoints = user.getSocialPoints();
            Double newPoints = req.getSocialPoints();
            user.setSocialPoints(newPoints);
            PointsHistory history = new PointsHistory(
                    null, user.getId(), user.getEmail(), user.getFullName(),
                    PointsHistory.PointsType.SOCIAL_POINTS, null,
                    oldPoints, newPoints, newPoints - oldPoints, reason, description, "ADMIN", now, null, null
            );
            pointsHistoryRepository.save(history);
            updated = true;
        }
        if (updated) {
            userRepository.save(user);
            return new ApiResponse<>(true, "Cập nhật điểm thành công", getUserPointsResponse(user));
        } else {
            return new ApiResponse<>(false, "Không có dữ liệu điểm để cập nhật", null);
        }
    }

    /**
     * Xử lý điểm thủ công cho danh sách user của một sự kiện
     */
    public ApiResponse<ManualPointsProcessingResult> manualPointsProcessing(ManualPointsProcessingRequest req) {
        // Sử dụng logic tương tự manualProcessPoints nhưng lấy adminId là "ADMIN"
        return manualProcessPoints(req, "ADMIN");
    }

    /**
     * Cộng điểm hàng loạt cho danh sách user
     */
    @Transactional
    public ApiResponse<BulkUpdatePointsResult> bulkUpdatePoints(BulkUpdatePointsRequest req) {
        int successCount = 0;
        int failCount = 0;
        int totalPointsAwarded = 0;
        List<String> failDetails = new ArrayList<>();
        for (String userId : req.getUserIds()) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));
                LocalDateTime now = java.time.LocalDateTime.now();
                // Cộng điểm rèn luyện nếu có
                if (req.getTrainingPointsToAdd() != null) {
                    Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                    Double oldPoints = getTrainingPointsBySemester(user, currentSemester);
                    Double newPoints = oldPoints + req.getTrainingPointsToAdd();
                    updateTrainingPointsBySemester(user, currentSemester, newPoints);
                    PointsHistory history = new PointsHistory(
                            null, user.getId(), user.getEmail(), user.getFullName(),
                            PointsHistory.PointsType.TRAINING_POINTS, currentSemester,
                            oldPoints, newPoints, req.getTrainingPointsToAdd().doubleValue(), req.getReason(), null, "ADMIN", now, null, null
                    );
                    pointsHistoryRepository.save(history);
                    totalPointsAwarded += req.getTrainingPointsToAdd();
                }
                // Cộng điểm xã hội nếu có
                if (req.getSocialPointsToAdd() != null) {
                    Double oldPoints = user.getSocialPoints();
                    Double newPoints = oldPoints + req.getSocialPointsToAdd();
                    user.setSocialPoints(newPoints);
                    PointsHistory history = new PointsHistory(
                            null, user.getId(), user.getEmail(), user.getFullName(),
                            PointsHistory.PointsType.SOCIAL_POINTS, null,
                            oldPoints, newPoints, req.getSocialPointsToAdd().doubleValue(), req.getReason(), null, "ADMIN", now, null, null
                    );
                    pointsHistoryRepository.save(history);
                    totalPointsAwarded += req.getSocialPointsToAdd();
                }
                userRepository.save(user);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failDetails.add("User " + userId + ": " + e.getMessage());
            }
        }
        BulkUpdatePointsResult result = new BulkUpdatePointsResult();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setFailDetails(failDetails);
        result.setTotalPointsAwarded(totalPointsAwarded);
        result.setEventName(null);
        return new ApiResponse<>(failCount == 0, failCount == 0 ? "Cập nhật điểm hàng loạt thành công" : "Có lỗi khi cập nhật điểm cho một số user", result);
    }

    /**
     * Lấy thông tin điểm của user
     */
    public ApiResponse<UserPointsResponse> getUserPoints(String userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            UserPointsResponse response = getUserPointsResponse(user);
            return new ApiResponse<>(true, "Lấy thông tin điểm thành công", response);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Lỗi lấy thông tin điểm: " + e.getMessage(), null);
        }
    }

    /**
     * Lấy lịch sử điểm của user
     */
    public ApiResponse<List<PointsHistory>> getUserPointsHistory(String userId) {
        try {
            List<PointsHistory> history = pointsHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
            return new ApiResponse<>(true, "Lấy lịch sử điểm thành công", history);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Lỗi lấy lịch sử điểm: " + e.getMessage(), null);
        }
    }

    /**
     * Lấy điểm rèn luyện theo kỳ học
     */
    private Double getTrainingPointsBySemester(User user, Semester semester) {
        try {
            Field field = User.class.getDeclaredField(semester.getTrainingPointsFieldName());
            field.setAccessible(true);
            return (Double) field.get(user);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Cập nhật điểm rèn luyện theo kỳ học
     */
    private void updateTrainingPointsBySemester(User user, Semester semester, Double points) {
        try {
            Field field = User.class.getDeclaredField(semester.getTrainingPointsFieldName());
            field.setAccessible(true);
            field.set(user, points);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật điểm cho kỳ " + semester.getDisplayName(), e);
        }
    }

    /**
     * Tạo response cho thông tin điểm của user
     */
    private UserPointsResponse getUserPointsResponse(User user) {
        UserPointsResponse response = new UserPointsResponse();
        response.setUserId(user.getId());
        response.setUserEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setSocialPoints(user.getSocialPoints());

        // Tạo map điểm rèn luyện theo kỳ
        Map<String, Double> trainingPointsMap = new HashMap<>();
        double totalTrainingPoints = 0.0;
        int semesterCount = 0;

        for (Semester semester : Semester.values()) {
            Double points = getTrainingPointsBySemester(user, semester);
            trainingPointsMap.put(semester.getDisplayName(), points);
            totalTrainingPoints += points;
            if (points > 0) semesterCount++;
        }

        response.setTrainingPointsBySemester(trainingPointsMap);
        response.setTotalTrainingPoints(totalTrainingPoints);
        response.setAverageTrainingPoints(semesterCount > 0 ? totalTrainingPoints / semesterCount : 0.0);
        response.setTotalPoints(totalTrainingPoints + user.getSocialPoints());

        // Xác định kỳ học hiện tại dựa trên niên khóa học
        Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
        response.setCurrentSemester(currentSemester.getDisplayName());
        response.setCurrentSemesterPoints(getTrainingPointsBySemester(user, currentSemester));

        return response;
    }

    /**
     * Cộng điểm rèn luyện cho kỳ học cụ thể
     */
    public void addTrainingPoints(User user, Semester semester, Double points, String reason, String eventId, String eventName) {
        Double currentPoints = getTrainingPointsBySemester(user, semester);
        Double newPoints = currentPoints + points;

        updateTrainingPointsBySemester(user, semester, newPoints);

        // Lưu lịch sử
        PointsHistory history = new PointsHistory(
            null, user.getId(), user.getEmail(), user.getFullName(),
            PointsHistory.PointsType.TRAINING_POINTS, semester,
            currentPoints, newPoints, newPoints - currentPoints, reason, "Tự động cộng điểm từ sự kiện", "SYSTEM", java.time.LocalDateTime.now(), eventId, eventName
        );
        pointsHistoryRepository.save(history);
    }

    /**
     * Cộng điểm hoạt động xã hội
     */
    public void addSocialPoints(User user, Double points, String reason, String eventId, String eventName) {
        Double currentPoints = user.getSocialPoints();
        Double newPoints = currentPoints + points;

        user.setSocialPoints(newPoints);

        // Lưu lịch sử
        PointsHistory history = new PointsHistory(
            null, user.getId(), user.getEmail(), user.getFullName(),
            PointsHistory.PointsType.SOCIAL_POINTS, null,
            currentPoints, newPoints, newPoints - currentPoints, reason, "Tự động cộng điểm từ sự kiện", "SYSTEM", java.time.LocalDateTime.now(), eventId, eventName
        );
        pointsHistoryRepository.save(history);
    }

    /**
     * Cập nhật điểm rèn luyện cho kỳ học cụ thể (dùng cho controller)
     */
    public ApiResponse<UserPointsResponse> updateTrainingPoints(UpdatePointsRequest request, String adminId) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (request.getSemester() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Kỳ học không được để trống");
        }
        Double oldPoints = getTrainingPointsBySemester(user, request.getSemester());
        Double newPoints = request.getTrainingPoints();
        updateTrainingPointsBySemester(user, request.getSemester(), newPoints);
        PointsHistory history = new PointsHistory(
                null, user.getId(), user.getEmail(), user.getFullName(),
                PointsHistory.PointsType.TRAINING_POINTS, request.getSemester(),
                oldPoints, newPoints, newPoints - oldPoints, request.getReason(), request.getDescription(), adminId, java.time.LocalDateTime.now(), null, null
        );
        pointsHistoryRepository.save(history);
        userRepository.save(user);
        return new ApiResponse<>(true, "Cập nhật điểm rèn luyện thành công", getUserPointsResponse(user));
    }

    /**
     * Cập nhật điểm hoạt động xã hội (dùng cho controller)
     */
    public ApiResponse<UserPointsResponse> updateSocialPoints(UpdatePointsRequest request, String adminId) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        Double oldPoints = user.getSocialPoints();
        Double newPoints = request.getSocialPoints();
        user.setSocialPoints(newPoints);
        PointsHistory history = new PointsHistory(
                null, user.getId(), user.getEmail(), user.getFullName(),
                PointsHistory.PointsType.SOCIAL_POINTS, null,
                oldPoints, newPoints, newPoints - oldPoints, request.getReason(), request.getDescription(), adminId, java.time.LocalDateTime.now(), null, null
        );
        pointsHistoryRepository.save(history);
        userRepository.save(user);
        return new ApiResponse<>(true, "Cập nhật điểm hoạt động xã hội thành công", getUserPointsResponse(user));
    }
}

