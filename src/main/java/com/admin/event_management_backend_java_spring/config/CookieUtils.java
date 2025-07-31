package com.admin.event_management_backend_java_spring.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CookieUtils {
    
    private static final String JWT_COOKIE_NAME = "jwt_token";
    
    @Value("${jwt.cookie.domain:}")
    private String cookieDomain;
    
    @Value("${jwt.cookie.secure:true}")
    private boolean cookieSecure;
    
    @Value("${jwt.cookie.same-site:Strict}")
    private String cookieSameSite;
    
    /**
     * Tạo JWT cookie với HttpOnly flag và SameSite attribute
     */
    public void createJwtCookie(HttpServletResponse response, String token, Date expiration) {
        // Tạo cookie cơ bản
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            jwtCookie.setDomain(cookieDomain);
        }
        
        // Tính thời gian hết hạn cookie (giây)
        int maxAge = (int) ((expiration.getTime() - System.currentTimeMillis()) / 1000);
        jwtCookie.setMaxAge(maxAge);
        
        // Thêm cookie vào response
        response.addCookie(jwtCookie);
        
        // Thiết lập SameSite attribute bằng cách override header Set-Cookie
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(JWT_COOKIE_NAME).append("=").append(token);
        cookieHeader.append("; HttpOnly");
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        cookieHeader.append("; Path=/");
        cookieHeader.append("; Max-Age=").append(maxAge);
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieHeader.append("; Domain=").append(cookieDomain);
        }
        
        response.setHeader("Set-Cookie", cookieHeader.toString());
    }
    
    /**
     * Xóa JWT cookie
     */
    public void deleteJwtCookie(HttpServletResponse response) {
        // Tạo cookie để xóa
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            jwtCookie.setDomain(cookieDomain);
        }
        
        jwtCookie.setMaxAge(0); // Xóa cookie ngay lập tức
        
        // Thêm cookie vào response
        response.addCookie(jwtCookie);
        
        // Thiết lập SameSite attribute khi xóa
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(JWT_COOKIE_NAME).append("=");
        cookieHeader.append("; HttpOnly");
        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }
        cookieHeader.append("; Path=/");
        cookieHeader.append("; Max-Age=0");
        cookieHeader.append("; SameSite=").append(cookieSameSite);
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieHeader.append("; Domain=").append(cookieDomain);
        }
        
        response.setHeader("Set-Cookie", cookieHeader.toString());
    }
    
    /**
     * Lấy JWT token từ cookie hoặc Authorization header
     */
    public String getJwtToken(HttpServletRequest request) {
        // Thử lấy từ cookie trước
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // Fallback: lấy từ Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem request có chứa JWT token không
     */
    public boolean hasJwtToken(HttpServletRequest request) {
        return getJwtToken(request) != null;
    }
} 