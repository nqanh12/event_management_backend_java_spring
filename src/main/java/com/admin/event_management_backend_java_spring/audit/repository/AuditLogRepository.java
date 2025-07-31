package com.admin.event_management_backend_java_spring.audit.repository;

import com.admin.event_management_backend_java_spring.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    
    // Pagination methods
    Page<AuditLog> findAll(Pageable pageable);
    
    // Tìm theo user với pagination
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    // Tìm theo action với pagination
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    // Tìm theo resource type với pagination
    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);
    
    // Tìm theo resource ID với pagination
    Page<AuditLog> findByResourceId(String resourceId, Pageable pageable);
    
    // Tìm theo status với pagination
    Page<AuditLog> findByStatus(String status, Pageable pageable);
    
    // Tìm theo khoảng thời gian với pagination
    Page<AuditLog> findByTimestampBetween(Date startDate, Date endDate, Pageable pageable);
    
    // Tìm theo user và khoảng thời gian với pagination
    Page<AuditLog> findByUserIdAndTimestampBetween(String userId, Date startDate, Date endDate, Pageable pageable);
    
    // Tìm theo action và khoảng thời gian với pagination
    Page<AuditLog> findByActionAndTimestampBetween(String action, Date startDate, Date endDate, Pageable pageable);
    
    // Tìm theo user role với pagination
    Page<AuditLog> findByUserRole(String userRole, Pageable pageable);
    
    // Tìm theo department với pagination
    Page<AuditLog> findByDepartmentId(String departmentId, Pageable pageable);
    
    // Tìm theo IP address (cho security) với pagination
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);
    
    // Custom query để tìm kiếm phức tạp với pagination
    @Query("{'$and': [{'timestamp': {'$gte': ?0, '$lte': ?1}}, {'$or': [{'action': ?2}, {'resourceType': ?3}]}]}")
    Page<AuditLog> findComplexQuery(Date startDate, Date endDate, String action, String resourceType, Pageable pageable);
    
    // Đếm số lượng log theo action trong khoảng thời gian
    @Query(value = "{'action': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}", count = true)
    long countByActionAndTimestampBetween(String action, Date startDate, Date endDate);
    
    // Đếm số lượng log theo user trong khoảng thời gian
    @Query(value = "{'userId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}", count = true)
    long countByUserIdAndTimestampBetween(String userId, Date startDate, Date endDate);
    
    // Tìm các hoạt động đáng ngờ (nhiều lần thất bại) với pagination
    @Query("{'status': 'FAILED', 'timestamp': {'$gte': ?0}}")
    Page<AuditLog> findFailedActivitiesSince(Date since, Pageable pageable);
    
    // Recent activities with pagination
    @Query(value = "{}", sort = "{'timestamp': -1}")
    Page<AuditLog> findRecentActivities(Pageable pageable);
    
    // Count methods for performance
    long countByUserId(String userId);
    long countByAction(String action);
    long countByStatus(String status);
    long countByUserRole(String userRole);
    long countByDepartmentId(String departmentId);
    
    // Additional methods needed by AuditService and DashboardService
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(String userId, Date startDate, Date endDate);
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);
    List<AuditLog> findByActionAndTimestampBetweenOrderByTimestampDesc(String action, Date startDate, Date endDate);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Date startDate, Date endDate);
    
    // Tìm các hoạt động đáng ngờ (nhiều lần thất bại) - version without pagination
    @Query("{'status': 'FAILED', 'timestamp': {'$gte': ?0}}")
    List<AuditLog> findFailedActivitiesSince(Date since);
}
