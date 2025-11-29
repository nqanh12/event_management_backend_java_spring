package com.admin.event_management_backend_java_spring.audit.controller;

import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.PaginatedResponse;
import com.admin.event_management_backend_java_spring.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AuditController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Tìm kiếm audit logs với nhiều filter
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> searchAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String departmentId) {
        
        ApiResponse<List<AuditLog>> response = auditService.searchAuditLogs(
            userId, action, resourceType, resourceId, status, startDate, endDate, userRole, departmentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tìm kiếm audit logs với nhiều filter và pagination
     */
    @GetMapping("/logs/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLog>>> searchAuditLogsWithPagination(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        ApiResponse<PaginatedResponse<AuditLog>> response = auditService.searchAuditLogsWithPagination(
            userId, action, resourceType, resourceId, status, startDate, endDate, userRole, departmentId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs của một user cụ thể
     */
    @GetMapping("/users/{userId}/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        ApiResponse<List<AuditLog>> response = auditService.getUserAuditLogs(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs của một user cụ thể với pagination
     */
    @GetMapping("/users/{userId}/logs/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLog>>> getUserAuditLogsWithPagination(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        ApiResponse<PaginatedResponse<AuditLog>> response = auditService.getUserAuditLogsWithPagination(userId, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs theo action
     */
    @GetMapping("/actions/{action}/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        ApiResponse<List<AuditLog>> response = auditService.getAuditLogsByAction(action, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs theo action với pagination
     */
    @GetMapping("/actions/{action}/logs/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLog>>> getAuditLogsByActionWithPagination(
            @PathVariable String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        ApiResponse<PaginatedResponse<AuditLog>> response = auditService.getAuditLogsByActionWithPagination(action, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thống kê audit logs
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        ApiResponse<Map<String, Object>> response = auditService.getAuditStatistics(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Phát hiện hoạt động đáng ngờ
     */
    @GetMapping("/suspicious")
    public ResponseEntity<ApiResponse<List<AuditLog>>> detectSuspiciousActivity(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date since) {
        
        ApiResponse<List<AuditLog>> response = auditService.detectSuspiciousActivity(since);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs của resource cụ thể
     */
    @GetMapping("/resources/{resourceType}/{resourceId}/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getResourceAuditLogs(
            @PathVariable String resourceType,
            @PathVariable String resourceId) {
        
        ApiResponse<List<AuditLog>> response = auditService.searchAuditLogs(
            null, null, resourceType, resourceId, null, null, null, null, null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs của resource cụ thể với pagination
     */
    @GetMapping("/resources/{resourceType}/{resourceId}/logs/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLog>>> getResourceAuditLogsWithPagination(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        ApiResponse<PaginatedResponse<AuditLog>> response = auditService.searchAuditLogsWithPagination(
            null, null, resourceType, resourceId, null, null, null, null, null, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs theo department
     */
    @GetMapping("/departments/{departmentId}/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getDepartmentAuditLogs(
            @PathVariable String departmentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        ApiResponse<List<AuditLog>> response = auditService.searchAuditLogs(
            null, null, null, null, null, startDate, endDate, null, departmentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy audit logs theo department với pagination
     */
    @GetMapping("/departments/{departmentId}/logs/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLog>>> getDepartmentAuditLogsWithPagination(
            @PathVariable String departmentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        ApiResponse<PaginatedResponse<AuditLog>> response = auditService.searchAuditLogsWithPagination(
            null, null, null, null, null, startDate, endDate, null, departmentId, page, size);
        return ResponseEntity.ok(response);
    }
}
