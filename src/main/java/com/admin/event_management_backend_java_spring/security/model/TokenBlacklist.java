package com.admin.event_management_backend_java_spring.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "token_blacklist")
public class TokenBlacklist {
    @Id
    private String id;
    
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;
    
    private String token;
    private String username;
    private String reason; // LOGOUT, EXPIRED, REVOKED, etc.
    private LocalDateTime blacklistedAt;
    
    public TokenBlacklist(String token, String username, String reason, LocalDateTime expiresAt) {
        this.token = token;
        this.username = username;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.blacklistedAt = LocalDateTime.now();
    }
}
