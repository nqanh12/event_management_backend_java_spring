package com.admin.event_management_backend_java_spring.user.controller;

import com.admin.event_management_backend_java_spring.user.model.UserSession;
import com.admin.event_management_backend_java_spring.user.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/admin/user-sessions")
@PreAuthorize("hasRole('GLOBAL_ADMIN')")
public class UserSessionController {
    
    @Autowired
    private UserSessionService userSessionService;
    
    /**
     * Lấy tất cả active sessions
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserSession>> getAllActiveSessions() {
        // TODO: Implement pagination
        return ResponseEntity.ok(userSessionService.getAllActiveSessions());
    }
    
    /**
     * Lấy tất cả sessions của một user
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserSession>> getUserSessions(@PathVariable String username) {
        return ResponseEntity.ok(userSessionService.getAllSessions(username));
    }
    
    /**
     * Lấy active sessions của một user
     */
    @GetMapping("/user/{username}/active")
    public ResponseEntity<List<UserSession>> getUserActiveSessions(@PathVariable String username) {
        return ResponseEntity.ok(userSessionService.getActiveSessions(username));
    }
    
    /**
     * Lấy số lượng active sessions của user
     */
    @GetMapping("/user/{username}/count")
    public ResponseEntity<Map<String, Object>> getUserActiveSessionCount(@PathVariable String username) {
        long count = userSessionService.getActiveSessionCount(username);
        return ResponseEntity.ok(Map.of(
            "username", username,
            "activeSessionCount", count
        ));
    }
    
    /**
     * Force logout tất cả sessions của user
     */
    @PostMapping("/user/{username}/force-logout")
    public ResponseEntity<Map<String, String>> forceLogoutUser(@PathVariable String username) {
        try {
            userSessionService.forceLogoutAllSessions(username);
            return ResponseEntity.ok(Map.of("message", "All sessions logged out for user: " + username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to logout user sessions"));
        }
    }
    
    /**
     * Đóng session cụ thể
     */
    @DeleteMapping("/session/{token}")
    public ResponseEntity<Map<String, String>> closeSession(@PathVariable String token) {
        try {
            userSessionService.closeSpecificSession(token);
            return ResponseEntity.ok(Map.of("message", "Session closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to close session"));
        }
    }
    
    /**
     * Lấy thông tin session theo token
     */
    @GetMapping("/session/{token}")
    public ResponseEntity<UserSession> getSessionInfo(@PathVariable String token) {
        UserSession session = userSessionService.getSessionByToken(token);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 