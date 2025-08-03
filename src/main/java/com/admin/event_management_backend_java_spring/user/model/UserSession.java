package com.admin.event_management_backend_java_spring.user.model;
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
@Document(collection = "user_sessions")
public class UserSession {
    @Id
    private String id;  // ID của session
    
    @Indexed
    private String username; // Email của user
    
    @Indexed
    private String token; // JWT token
    
    private String userAgent; // Thông tin trình duyệt
    private String ipAddress; // Địa chỉ IP
    private LocalDateTime loginTime; // Thời gian đăng nhập
    private LocalDateTime lastActivity; // Thời gian hoạt động gần nhất
    private boolean active; // Trạng thái hoạt động
    
    public UserSession(String username, String token, String userAgent, String ipAddress) {
        this.username = username;
        this.token = token;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.active = true;
    }
} 