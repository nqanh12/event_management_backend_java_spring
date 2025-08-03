package com.admin.event_management_backend_java_spring.audit.service;

import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.PaginatedResponse;
import com.admin.event_management_backend_java_spring.audit.repository.AuditLogRepository;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import com.admin.event_management_backend_java_spring.audit.payload.request.AuditSearchRequest;

@Slf4j
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Ghi log tự động với thông tin từ security context
     */
    public void logActivity(String action, String resourceType, String resourceId,
                          String description, Map<String, Object> oldValues,
                          Map<String, Object> newValues, String status, String errorMessage) {

        AuditLog auditLog = new AuditLog();

        // Lấy thông tin user từ security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                auditLog.setUserId(user.getId());
                auditLog.setUserEmail(user.getEmail());
                auditLog.setUserRole(user.getRole().name());
                auditLog.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
            }
        }

        // Lấy thông tin request
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }
        } catch (Exception e) {
            // Ignore if not in web context
        }

        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDescription(description);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setStatus(status);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setTimestamp(new Date());

        auditLogRepository.save(auditLog);
    }

    /**
     * Ghi log đơn giản
     */
    public void logActivity(String action, String resourceType, String resourceId, String description) {
        logActivity(action, resourceType, resourceId, description, null, null, "SUCCESS", null);
    }

    /**
     * Ghi log với status
     */
    public void logActivity(String action, String resourceType, String resourceId,
                          String description, String status) {
        logActivity(action, resourceType, resourceId, description, null, null, status, null);
    }

    /**
     * Ghi log lỗi
     */
    public void logError(String action, String resourceType, String resourceId,
                        String description, String errorMessage) {
        logActivity(action, resourceType, resourceId, description, null, null, "FAILED", errorMessage);
    }

    // Log cho các thao tác 2FA
    public void log2faAction(String userId, String action, String description) {
        logActivity(action, "USER", userId, description);
    }
    public void log2faError(String userId, String action, String description, String errorMessage) {
        logActivity(action, "USER", userId, description, "FAILED");
    }

    /**
     * Lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Tìm kiếm audit logs
     */
    public ApiResponse<List<AuditLog>> searchAuditLogs(String userId, String action, String resourceType, String resourceId, String status, Date startDate, Date endDate, String userRole, String departmentId) {
        List<AuditLog> logs = auditLogRepository.findAll();
        return new ApiResponse<>(true, "Audit logs retrieved successfully",
            logs.stream()
                .filter(log -> userId == null || userId.equals(log.getUserId()))
                .filter(log -> action == null || action.equals(log.getAction()))
                .filter(log -> resourceType == null || resourceType.equals(log.getResourceType()))
                .filter(log -> resourceId == null || resourceId.equals(log.getResourceId()))
                .filter(log -> status == null || status.equals(log.getStatus()))
                .filter(log -> startDate == null || log.getTimestamp().after(startDate))
                .filter(log -> endDate == null || log.getTimestamp().before(endDate))
                .filter(log -> userRole == null || userRole.equals(log.getUserRole()))
                .filter(log -> departmentId == null || departmentId.equals(log.getDepartmentId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList()
        );
    }

    /**
     * Lấy audit logs của user
     */
    public ApiResponse<List<AuditLog>> getUserAuditLogs(String userId, Date startDate, Date endDate) {
        List<AuditLog> logs;
        if (startDate != null && endDate != null) {
            logs = auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, startDate, endDate);
        } else {
            logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        }
        return new ApiResponse<>(true, "User audit logs retrieved successfully", logs);
    }

    /**
     * Lấy audit logs theo action
     */
    public ApiResponse<List<AuditLog>> getAuditLogsByAction(String action, Date startDate, Date endDate) {
        List<AuditLog> logs;
        if (startDate != null && endDate != null) {
            logs = auditLogRepository.findByActionAndTimestampBetweenOrderByTimestampDesc(action, startDate, endDate);
        } else {
            logs = auditLogRepository.findByActionOrderByTimestampDesc(action);
        }
        return new ApiResponse<>(true, "Action audit logs retrieved successfully", logs);
    }

    /**
     * Lấy thống kê audit logs
     */
    public ApiResponse<Map<String, Object>> getAuditStatistics(Date startDate, Date endDate) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        Map<String, Object> stats = Map.of(
            "totalLogs", logs.size(),
            "successLogs", logs.stream().filter(log -> "SUCCESS".equals(log.getStatus())).count(),
            "failedLogs", logs.stream().filter(log -> "FAILED".equals(log.getStatus())).count(),
            "uniqueUsers", logs.stream().map(AuditLog::getUserId).distinct().count(),
            "uniqueActions", logs.stream().map(AuditLog::getAction).distinct().count()
        );

        return new ApiResponse<>(true, "Audit statistics retrieved successfully", stats);
    }

    /**
     * Phát hiện hoạt động đáng ngờ
     */
    public ApiResponse<List<AuditLog>> detectSuspiciousActivity(Date since) {
        List<AuditLog> failedLogs = auditLogRepository.findFailedActivitiesSince(since);
        return new ApiResponse<>(true, "Suspicious activities detected", failedLogs);
    }

    /**
     * Tìm kiếm audit logs với pagination
     */
    public ApiResponse<PaginatedResponse<AuditLog>> searchAuditLogsWithPagination(
            String userId, String action, String resourceType, String resourceId,
            String status, Date startDate, Date endDate, String userRole,
            String departmentId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage;

        if (userId != null && startDate != null && endDate != null) {
            auditLogPage = auditLogRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate, pageable);
        } else if (action != null && startDate != null && endDate != null) {
            auditLogPage = auditLogRepository.findByActionAndTimestampBetween(action, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            auditLogPage = auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else if (userId != null) {
            auditLogPage = auditLogRepository.findByUserId(userId, pageable);
        } else if (action != null) {
            auditLogPage = auditLogRepository.findByAction(action, pageable);
        } else if (resourceType != null) {
            auditLogPage = auditLogRepository.findByResourceType(resourceType, pageable);
        } else if (status != null) {
            auditLogPage = auditLogRepository.findByStatus(status, pageable);
        } else if (userRole != null) {
            auditLogPage = auditLogRepository.findByUserRole(userRole, pageable);
        } else if (departmentId != null) {
            auditLogPage = auditLogRepository.findByDepartmentId(departmentId, pageable);
        } else {
            auditLogPage = auditLogRepository.findAll(pageable);
        }

        PaginatedResponse<AuditLog> paginatedResponse = PaginatedResponse.fromPage(auditLogPage);
        return new ApiResponse<>(true, "Audit logs retrieved successfully", paginatedResponse);
    }

    /**
     * Lấy audit logs của user với pagination
     */
    public ApiResponse<PaginatedResponse<AuditLog>> getUserAuditLogsWithPagination(
            String userId, Date startDate, Date endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage;

        if (startDate != null && endDate != null) {
            auditLogPage = auditLogRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate, pageable);
        } else {
            auditLogPage = auditLogRepository.findByUserId(userId, pageable);
        }

        PaginatedResponse<AuditLog> paginatedResponse = PaginatedResponse.fromPage(auditLogPage);
        return new ApiResponse<>(true, "User audit logs retrieved successfully", paginatedResponse);
    }

    /**
     * Lấy audit logs theo action với pagination
     */
    public ApiResponse<PaginatedResponse<AuditLog>> getAuditLogsByActionWithPagination(
            String action, Date startDate, Date endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage;

        if (startDate != null && endDate != null) {
            auditLogPage = auditLogRepository.findByActionAndTimestampBetween(action, startDate, endDate, pageable);
        } else {
            auditLogPage = auditLogRepository.findByAction(action, pageable);
        }

        PaginatedResponse<AuditLog> paginatedResponse = PaginatedResponse.fromPage(auditLogPage);
        return new ApiResponse<>(true, "Audit logs by action retrieved successfully", paginatedResponse);
    }

    /**
     * Lấy recent activities với pagination
     */
    public ApiResponse<PaginatedResponse<AuditLog>> getRecentActivitiesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage = auditLogRepository.findRecentActivities(pageable);

        PaginatedResponse<AuditLog> paginatedResponse = PaginatedResponse.fromPage(auditLogPage);
        return new ApiResponse<>(true, "Recent activities retrieved successfully", paginatedResponse);
    }

    public void logAudit(String userId, String action, String resource, String details) {
        log.info("[AUDIT] Ghi log audit: userId={}, action={}, resource={}, details={}", userId, action, resource, details);
    }

    public void logBusinessError(String action, String resourceType, String resourceId, String description, String errorMessage) {
        log.error("[BUSINESS_ERROR] Action: {}, ResourceType: {}, ResourceId: {}, Description: {}, ErrorMessage: {}", action, resourceType, resourceId, description, errorMessage);
    }
}
