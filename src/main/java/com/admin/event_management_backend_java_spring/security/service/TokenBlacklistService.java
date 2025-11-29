package com.admin.event_management_backend_java_spring.security.service;

import com.admin.event_management_backend_java_spring.security.model.TokenBlacklist;
import com.admin.event_management_backend_java_spring.security.model.BlacklistReason;
import com.admin.event_management_backend_java_spring.security.repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenBlacklistService {
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    /**
     * Thêm token vào blacklist
     */
    public void blacklistToken(String token, String reason, String username, LocalDateTime expiration) {
        TokenBlacklist blacklistedToken = new TokenBlacklist(token, username, reason, expiration);
        tokenBlacklistRepository.save(blacklistedToken);
    }
    
    /**
     * Thêm token vào blacklist với thông tin cơ bản
     */
    public void blacklistToken(String token, String reason) {
        TokenBlacklist blacklistedToken = new TokenBlacklist(token, "unknown", reason, LocalDateTime.now().plusDays(1));
        tokenBlacklistRepository.save(blacklistedToken);
    }
    
    /**
     * Kiểm tra xem token có trong blacklist không
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
    
    /**
     * Lấy thông tin token blacklist
     */
    public TokenBlacklist getBlacklistedToken(String token) {
        return tokenBlacklistRepository.findByToken(token).orElse(null);
    }
    
    /**
     * Xóa token khỏi blacklist
     */
    public void removeFromBlacklist(String token) {
        tokenBlacklistRepository.deleteByToken(token);
    }
    
    /**
     * Lấy tất cả token blacklist của một user
     */
    public List<TokenBlacklist> getBlacklistedTokensByUsername(String username) {
        return tokenBlacklistRepository.findByUsername(username);
    }
    
    /**
     * Lấy tất cả token blacklist theo lý do
     */
    public List<TokenBlacklist> getBlacklistedTokensByReason(String reason) {
        return tokenBlacklistRepository.findByReason(reason);
    }
    
    /**
     * Xóa tất cả token của một user khỏi blacklist
     */
    public void removeAllTokensByUsername(String username) {
        tokenBlacklistRepository.deleteByUsername(username);
    }
    
    /**
     * Lấy tất cả token đã hết hạn
     */
    public List<TokenBlacklist> getExpiredTokens() {
        return tokenBlacklistRepository.findExpiredTokens(LocalDateTime.now());
    }
    
    /**
     * Lấy tất cả token blacklist
     */
    public List<TokenBlacklist> getAllBlacklistedTokens() {
        return tokenBlacklistRepository.findAll();
    }
    
    /**
     * Lấy tất cả token blacklist với pagination
     */
    public Page<TokenBlacklist> getAllBlacklistedTokens(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("blacklistedAt").descending());
        return tokenBlacklistRepository.findAll(pageable);
    }
    
    /**
     * Xóa tất cả token đã hết hạn (scheduled task)
     */
    @Scheduled(fixedRate = 3600000) // Chạy mỗi giờ
    public void cleanupExpiredTokens() {
        List<TokenBlacklist> expiredTokens = getExpiredTokens();
        tokenBlacklistRepository.deleteAll(expiredTokens);
    }
    
    /**
     * Thêm token vào blacklist khi logout
     */
    public void logoutToken(String token) {
        blacklistToken(token, BlacklistReason.LOGOUT.name());
    }
    
    /**
     * Thêm token vào blacklist khi bị revoke
     */
    public void revokeToken(String token) {
        blacklistToken(token, BlacklistReason.REVOKED.name());
    }
    
    /**
     * Thêm token vào blacklist khi bị compromise
     */
    public void compromiseToken(String token) {
        blacklistToken(token, BlacklistReason.COMPROMISED.name());
    }
    
    /**
     * Force logout tất cả session của user
     */
    public void forceLogoutUser(String username) {
        // Lấy tất cả token active của user từ UserSessionService
        List<TokenBlacklist> userTokens = getBlacklistedTokensByUsername(username);
        
        // Blacklist tất cả token hiện tại của user
        List<TokenBlacklist> allTokens = tokenBlacklistRepository.findByUsername(username);
        for (TokenBlacklist tokenRecord : allTokens) {
            if (tokenRecord.getExpiresAt().isAfter(LocalDateTime.now())) {
                // Token chưa hết hạn, force blacklist
                tokenRecord.setReason("FORCE_LOGOUT");
                tokenRecord.setBlacklistedAt(LocalDateTime.now());
                tokenBlacklistRepository.save(tokenRecord);
            }
        }
        
        // Notify UserSessionService để đóng tất cả sessions
        // This will be handled by UserSessionService.forceLogoutAllSessions(username)
    }
    
    /**
     * Thêm token vào blacklist khi password thay đổi
     */
    public void passwordChangedToken(String token) {
        blacklistToken(token, BlacklistReason.PASSWORD_CHANGED.name());
    }
    
    /**
     * Thêm token vào blacklist khi account bị disable
     */
    public void accountDisabledToken(String token) {
        blacklistToken(token, BlacklistReason.ACCOUNT_DISABLED.name());
    }
}
