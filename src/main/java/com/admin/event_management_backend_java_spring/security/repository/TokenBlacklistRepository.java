package com.admin.event_management_backend_java_spring.security.repository;

import com.admin.event_management_backend_java_spring.security.model.TokenBlacklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends MongoRepository<TokenBlacklist, String> {
    
    Optional<TokenBlacklist> findByToken(String token);
    
    boolean existsByToken(String token);
    
    List<TokenBlacklist> findByUsername(String username);
    
    List<TokenBlacklist> findByReason(String reason);
    
    @Query("{'expiresAt': {$lt: ?0}}")
    List<TokenBlacklist> findExpiredTokens(LocalDateTime now);
    
    @Query("{'username': ?0, 'reason': ?1}")
    List<TokenBlacklist> findByUsernameAndReason(String username, String reason);
    
    void deleteByToken(String token);
    
    void deleteByUsername(String username);
}
