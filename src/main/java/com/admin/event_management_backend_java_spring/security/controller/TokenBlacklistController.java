package com.admin.event_management_backend_java_spring.security.controller;

import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.PaginatedResponse;
import com.admin.event_management_backend_java_spring.security.model.TokenBlacklist;
import com.admin.event_management_backend_java_spring.security.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/admin/token-blacklist")
@PreAuthorize("hasRole('ADMIN')")
public class TokenBlacklistController {
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    /**
     * Lấy tất cả token blacklist với pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<TokenBlacklist>>> getAllBlacklistedTokens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TokenBlacklist> tokensPage = tokenBlacklistService.getAllBlacklistedTokens(page, size);
        PaginatedResponse<TokenBlacklist> paginatedResponse = PaginatedResponse.fromPage(tokensPage);
        return ResponseEntity.ok(new ApiResponse<>(true, "Blacklisted tokens retrieved successfully", paginatedResponse));
    }
    
    /**
     * Lấy token blacklist theo username
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<TokenBlacklist>> getBlacklistedTokensByUsername(@PathVariable String username) {
        return ResponseEntity.ok(tokenBlacklistService.getBlacklistedTokensByUsername(username));
    }
    
    /**
     * Lấy token blacklist theo lý do
     */
    @GetMapping("/reason/{reason}")
    public ResponseEntity<List<TokenBlacklist>> getBlacklistedTokensByReason(@PathVariable String reason) {
        return ResponseEntity.ok(tokenBlacklistService.getBlacklistedTokensByReason(reason));
    }
    
    /**
     * Kiểm tra xem token có bị blacklist không
     */
    @GetMapping("/check/{token}")
    public ResponseEntity<Map<String, Object>> checkTokenStatus(@PathVariable String token) {
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        TokenBlacklist blacklistedToken = null;
        
        if (isBlacklisted) {
            blacklistedToken = tokenBlacklistService.getBlacklistedToken(token);
        }
        
        return ResponseEntity.ok(Map.of(
            "isBlacklisted", isBlacklisted,
            "tokenInfo", blacklistedToken
        ));
    }
    
    /**
     * Xóa token khỏi blacklist
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@PathVariable String token) {
        try {
            tokenBlacklistService.removeFromBlacklist(token);
            return ResponseEntity.ok(Map.of("message", "Token removed from blacklist"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to remove token from blacklist"));
        }
    }
    
    /**
     * Xóa tất cả token của một user khỏi blacklist
     */
    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, String>> removeAllTokensByUsername(@PathVariable String username) {
        try {
            tokenBlacklistService.removeAllTokensByUsername(username);
            return ResponseEntity.ok(Map.of("message", "All tokens removed from blacklist for user: " + username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to remove tokens from blacklist"));
        }
    }
    
    /**
     * Revoke token (admin function)
     */
    @PostMapping("/revoke/{token}")
    public ResponseEntity<Map<String, String>> revokeToken(@PathVariable String token) {
        try {
            tokenBlacklistService.revokeToken(token);
            return ResponseEntity.ok(Map.of("message", "Token revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to revoke token"));
        }
    }
    
    /**
     * Mark token as compromised
     */
    @PostMapping("/compromise/{token}")
    public ResponseEntity<Map<String, String>> compromiseToken(@PathVariable String token) {
        try {
            tokenBlacklistService.compromiseToken(token);
            return ResponseEntity.ok(Map.of("message", "Token marked as compromised"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to mark token as compromised"));
        }
    }
    
    /**
     * Cleanup expired tokens manually
     */
    // @PostMapping("/cleanup")
    // public ResponseEntity<Map<String, Object>> cleanupExpiredTokens() {
    //     try {
    //         List<TokenBlacklist> expiredTokens = tokenBlacklistService.getExpiredTokens();
    //         tokenBlacklistService.cleanupExpiredTokens();
    //         return ResponseEntity.ok(Map.of(
    //             "message", "Cleanup completed",
    //             "removedCount", expiredTokens.size()
    //         ));
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(Map.of("error", "Failed to cleanup expired tokens"));
    //     }
    // }
}
