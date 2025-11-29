package com.admin.event_management_backend_java_spring.user.repository;

import com.admin.event_management_backend_java_spring.user.model.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    
    Optional<UserSession> findByToken(String token);
    
    List<UserSession> findByUsername(String username);
    
    List<UserSession> findByUsernameAndActiveTrue(String username);
    
    @Query("{'lastActivity': {$lt: ?0}}")
    List<UserSession> findInactiveSessions(LocalDateTime threshold);
    
    @Query("{'active': true}")
    List<UserSession> findByActiveTrue();
    
    @Query("{'active': true}")
    Page<UserSession> findByActiveTrue(Pageable pageable);
    
    void deleteByToken(String token);
    
    void deleteByUsername(String username);
    
    long countByUsernameAndActiveTrue(String username);
} 