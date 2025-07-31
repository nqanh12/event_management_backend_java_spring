package com.admin.event_management_backend_java_spring.registration.repository;

import com.admin.event_management_backend_java_spring.registration.model.Registration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String> {
    
    // Tìm tất cả đăng ký của một user
    List<Registration> findByUserId(String userId);
    
    // Tìm đăng ký của user với status cụ thể
    List<Registration> findByUserIdAndStatus(String userId, Registration.RegistrationStatus status);
    
    // Tìm đăng ký của user cho một event cụ thể
    Registration findByUserIdAndEventId(String userId, String eventId);
    
    // Kiểm tra xem user đã đăng ký event chưa
    boolean existsByUserIdAndEventId(String userId, String eventId);
    
    // Count methods for performance
    long countByStatus(Registration.RegistrationStatus status);

    List<Registration> findByUserIdAndIsDeletedFalse(String userId);
    List<Registration> findByUserIdAndStatusAndIsDeletedFalse(String userId, Registration.RegistrationStatus status);
    Registration findByUserIdAndEventIdAndIsDeletedFalse(String userId, String eventId);
    List<Registration> findByEventIdAndIsDeletedFalse(String eventId);
} 