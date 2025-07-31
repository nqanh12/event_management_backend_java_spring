package com.admin.event_management_backend_java_spring.user.service;

import com.admin.event_management_backend_java_spring.user.model.UserSession;
import com.admin.event_management_backend_java_spring.user.repository.UserSessionRepository;
import com.admin.event_management_backend_java_spring.security.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserSessionService {
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    /**
     * Tạo session mới khi user login
     */
    public UserSession createSession(String username, String token, String userAgent, String ipAddress) {
        UserSession session = new UserSession(username, token, userAgent, ipAddress);
        return userSessionRepository.save(session);
    }
    
    /**
     * Cập nhật last activity của session
     */
    public void updateLastActivity(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            session.setLastActivity(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }
    
    /**
     * Đóng session khi logout
     */
    public void closeSession(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });
    }
    
    /**
     * Lấy tất cả active sessions của user
     */
    public List<UserSession> getActiveSessions(String username) {
        return userSessionRepository.findByUsernameAndActiveTrue(username);
    }
    
    /**
     * Lấy tất cả sessions của user
     */
    public List<UserSession> getAllSessions(String username) {
        return userSessionRepository.findByUsername(username);
    }
    
    /**
     * Force logout tất cả sessions của user
     */
    public void forceLogoutAllSessions(String username) {
        List<UserSession> activeSessions = getActiveSessions(username);
        
        for (UserSession session : activeSessions) {
            // Thêm token vào blacklist
            tokenBlacklistService.blacklistToken(session.getToken(), "FORCE_LOGOUT");
            // Đóng session
            session.setActive(false);
            userSessionRepository.save(session);
        }
    }
    
    /**
     * Đóng session cụ thể
     */
    public void closeSpecificSession(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });
    }
    
    /**
     * Lấy số lượng active sessions của user
     */
    public long getActiveSessionCount(String username) {
        return userSessionRepository.countByUsernameAndActiveTrue(username);
    }
    
    /**
     * Cleanup inactive sessions (scheduled task)
     */
    @Scheduled(fixedRate = 1800000) // Chạy mỗi 30 phút
    public void cleanupInactiveSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // Sessions không hoạt động trong 24h
        List<UserSession> inactiveSessions = userSessionRepository.findInactiveSessions(threshold);
        
        for (UserSession session : inactiveSessions) {
            if (session.isActive()) {
                session.setActive(false);
                userSessionRepository.save(session);
            }
        }
    }
    
    /**
     * Lấy thông tin session theo token
     */
    public UserSession getSessionByToken(String token) {
        return userSessionRepository.findByToken(token).orElse(null);
    }
    
    /**
     * Lấy tất cả active sessions
     */
    public List<UserSession> getAllActiveSessions() {
        // TODO: Implement this method in repository
        return userSessionRepository.findAll().stream()
                .filter(UserSession::isActive)
                .toList();
    }
} 