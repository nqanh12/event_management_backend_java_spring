package com.admin.event_management_backend_java_spring.user.repository;

import com.admin.event_management_backend_java_spring.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Optional<User> findByResetToken(String resetToken);
    
    // Pagination methods
    Page<User> findAll(Pageable pageable);
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    Page<User> findByDepartmentId(String departmentId, Pageable pageable);
    Page<User> findByRoleAndDepartmentId(User.UserRole role, String departmentId, Pageable pageable);
    Page<User> findByIsDeletedFalse(Pageable pageable);
    List<User> findByIsDeletedFalse();
    Page<User> findByRoleAndIsDeletedFalse(User.UserRole role, Pageable pageable);
    Page<User> findByDepartmentIdAndIsDeletedFalse(String departmentId, Pageable pageable);
    Page<User> findByRoleAndDepartmentIdAndIsDeletedFalse(User.UserRole role, String departmentId, Pageable pageable);
    
    // Search methods with pagination
    @Query("{'$or': [{'fullName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}, {'studentId': {$regex: ?0, $options: 'i'}}]}")
    Page<User> searchUsers(String searchTerm, Pageable pageable);
    
    @Query("{'role': ?0, '$or': [{'fullName': {$regex: ?1, $options: 'i'}}, {'email': {$regex: ?1, $options: 'i'}}, {'studentId': {$regex: ?1, $options: 'i'}}]}")
    Page<User> searchUsersByRole(User.UserRole role, String searchTerm, Pageable pageable);
    
    // Count methods for performance
    long countByRole(User.UserRole role);
    long countByDepartmentId(String departmentId);
    long countByRoleAndDepartmentId(User.UserRole role, String departmentId);
    
    // Top students by points (optimized)
    @Query(value = "{'role': 'STUDENT'}", sort = "{'socialPoints': -1}")
    List<User> findTopStudentsBySocialPoints(Pageable pageable);
    
    // Active users (recent login)
    @Query("{'lastLogin': {$gte: ?0}}")
    Page<User> findActiveUsers(java.util.Date since, Pageable pageable);
} 