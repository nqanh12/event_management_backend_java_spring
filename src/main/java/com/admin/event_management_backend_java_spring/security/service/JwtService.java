package com.admin.event_management_backend_java_spring.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.time.LocalDateTime;

@Service
public class JwtService {
    @Value("${jwt.signerKey}")
    private String SECRET_KEY;
    @Value("${jwt.valid-duration}")
    private long EXPIRATION;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public Boolean validateToken(String token, UserDetails userDetails) {
        // Kiểm tra xem token có trong blacklist không
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return false;
        }
        
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Thêm token vào blacklist khi logout
     */
    public void blacklistToken(String token) {
        try {
            String username = extractUsername(token);
            LocalDateTime expiration = extractExpiration(token).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            tokenBlacklistService.blacklistToken(token, "LOGOUT", username, expiration);
        } catch (Exception e) {
            // Nếu không thể parse token, vẫn lưu vào blacklist với thông tin cơ bản
            tokenBlacklistService.blacklistToken(token, "LOGOUT");
        }
    }
    
    /**
     * Revoke token (admin function)
     */
    public void revokeToken(String token) {
        try {
            String username = extractUsername(token);
            LocalDateTime expiration = extractExpiration(token).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            tokenBlacklistService.blacklistToken(token, "REVOKED", username, expiration);
        } catch (Exception e) {
            tokenBlacklistService.blacklistToken(token, "REVOKED");
        }
    }
    
    /**
     * Kiểm tra xem token có bị blacklist không
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isTokenBlacklisted(token);
    }
}
