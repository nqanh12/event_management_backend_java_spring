package com.admin.event_management_backend_java_spring.user.controller;

import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.payload.request.LoginRequest;
import com.admin.event_management_backend_java_spring.user.service.UserService;
import com.admin.event_management_backend_java_spring.user.service.CustomUserDetailsService;
import com.admin.event_management_backend_java_spring.user.service.UserSessionService;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.config.CookieUtils;
import com.admin.event_management_backend_java_spring.integration.MailService;
import com.admin.event_management_backend_java_spring.audit.service.AuditService;
import com.admin.event_management_backend_java_spring.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import jakarta.servlet.http.Cookie;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private MailService mailService;
    @Autowired
    private AuditService auditService;

    @Autowired
    private CookieUtils cookieUtils;

    /**
     * Tạo JWT cookie với các thuộc tính bảo mật
     */
    private void createJwtCookie(HttpServletResponse response, String token, Date expiration) {
        cookieUtils.createJwtCookie(response, token, expiration);
    }

    /**
     * Xóa JWT cookie
     */
    private void deleteJwtCookie(HttpServletResponse response) {
        cookieUtils.deleteJwtCookie(response);
    }

    private void createRefreshTokenCookie(HttpServletResponse response, String refreshToken, Date expiry) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) ((expiry.getTime() - System.currentTimeMillis()) / 1000));
        response.addCookie(cookie);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        User saved = userService.register(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        // Kiểm tra 2FA bắt buộc cho các role admin/manager
        boolean require2fa = false;
        if (user.getRole() == User.UserRole.GLOBAL_ADMIN || user.getRole() == User.UserRole.SCHOOL_MANAGER || user.getRole() == User.UserRole.FACULTY_ADMIN) {
            require2fa = true;
        }
        // Chống brute-force OTP
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new Date())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản bị khóa xác thực 2FA tạm thời. Vui lòng thử lại sau."));
        }
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) || require2fa) {
            // Sinh OTP, gửi mail, lưu vào user
            String otp = String.valueOf(100000 + (int)(Math.random() * 900000));
            user.setTwoFactorOtp(otp);
            user.setTwoFactorOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 phút
            user.setTwoFactorVerified(false);
            user.setFailed2faAttempts(0);
            userService.save(user);
            mailService.sendOtpMail(user.getEmail(), otp);
            auditService.log2faAction(user.getId(), "2FA_OTP_SENT", "Gửi OTP xác thực 2FA qua email");
            return ResponseEntity.ok(Map.of(
                "require_2fa", true,
                "message", "Vui lòng nhập mã OTP được gửi tới email để hoàn tất đăng nhập."
            ));
        }
        // Nếu không cần 2FA, tiếp tục trả access/refresh token
        String jwt = jwtService.generateToken(userDetails);
        Date tokenExpiration = jwtService.extractExpiration(jwt);
        // Sinh refresh token
        String refreshToken = java.util.UUID.randomUUID().toString();
        Date refreshTokenExpiry = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000); // 7 ngày
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(refreshTokenExpiry);
        // Kiểm tra thiết bị lạ
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        boolean isNewDevice = user.getLastLoginIp() == null || user.getLastLoginUserAgent() == null ||
                !user.getLastLoginIp().equals(ipAddress) || !user.getLastLoginUserAgent().equals(userAgent);
        if (isNewDevice && (user.getRole() == User.UserRole.GLOBAL_ADMIN || user.getRole() == User.UserRole.SCHOOL_MANAGER || user.getRole() == User.UserRole.FACULTY_ADMIN)) {
            mailService.sendLoginAlertMail(user.getEmail(), ipAddress, userAgent, new Date());
        }
        user.setLastLoginIp(ipAddress);
        user.setLastLoginUserAgent(userAgent);
        userService.save(user);
        userSessionService.createSession(userDetails.getUsername(), jwt, userAgent, ipAddress);
        createJwtCookie(response, jwt, tokenExpiration);
        createRefreshTokenCookie(response, refreshToken, refreshTokenExpiry);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("role", user.getRole());
        userInfo.put("department", user.getDepartment());
        userInfo.put("className", user.getClassName());
        userInfo.put("cohort", user.getCohort());
        userInfo.put("academicYear", user.getAcademicYear());
        userInfo.put("currentYear", user.getCurrentYear());
        userInfo.put("enrollmentDate", user.getEnrollmentDate());
        userInfo.put("trainingPoints1", user.getTrainingPoints1());
        userInfo.put("trainingPoints2", user.getTrainingPoints2());
        userInfo.put("trainingPoints3", user.getTrainingPoints3());
        userInfo.put("trainingPoints4", user.getTrainingPoints4());
        userInfo.put("trainingPoints5", user.getTrainingPoints5());
        userInfo.put("trainingPoints6", user.getTrainingPoints6());
        userInfo.put("trainingPoints7", user.getTrainingPoints7());
        userInfo.put("trainingPoints8", user.getTrainingPoints8());
        userInfo.put("socialPoints", user.getSocialPoints());
        userInfo.put("lastLogin", user.getLastLogin());
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful");
        // Chỉ trả về token/refreshToken nếu KHÔNG phải là admin/manager
        if (user.getRole() != User.UserRole.GLOBAL_ADMIN &&
            user.getRole() != User.UserRole.SCHOOL_MANAGER &&
            user.getRole() != User.UserRole.FACULTY_ADMIN) {
            responseBody.put("token", jwt);
            responseBody.put("refreshToken", refreshToken);
        }
        responseBody.put("tokenExpiration", tokenExpiration);
        responseBody.put("user", userInfo);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestParam String email, @RequestParam String otp, HttpServletRequest httpRequest, HttpServletResponse response) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        if (user.getTwoFactorOtp() == null || user.getTwoFactorOtpExpiry() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP chưa được gửi hoặc đã hết hạn"));
        }
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new Date())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản bị khóa xác thực 2FA tạm thời. Vui lòng thử lại sau."));
        }
        if (user.getTwoFactorOtpExpiry().before(new Date())) {
            auditService.log2faError(user.getId(), "2FA_OTP_EXPIRED", "OTP hết hạn", "OTP expired");
            return ResponseEntity.badRequest().body(Map.of("error", "OTP đã hết hạn. Vui lòng đăng nhập lại."));
        }
        if (!user.getTwoFactorOtp().equals(otp)) {
            // Tăng số lần nhập sai OTP
            int fail = user.getFailed2faAttempts() != null ? user.getFailed2faAttempts() + 1 : 1;
            user.setFailed2faAttempts(fail);
            if (fail >= 5) {
                user.setLock2faUntil(new Date(System.currentTimeMillis() + 10 * 60 * 1000)); // Khóa 10 phút
                userService.save(user);
                return ResponseEntity.badRequest().body(Map.of("error", "Nhập sai OTP quá nhiều lần. Tài khoản bị khóa xác thực 2FA tạm thời 10 phút."));
            }
            userService.save(user);
            auditService.log2faError(user.getId(), "2FA_OTP_INVALID", "OTP không đúng", "OTP invalid");
            return ResponseEntity.badRequest().body(Map.of("error", "OTP không đúng"));
        }
        // Đúng OTP, xác thực thành công
        user.setTwoFactorVerified(true);
        user.setTwoFactorOtp(null);
        user.setTwoFactorOtpExpiry(null);
        user.setFailed2faAttempts(0);
        user.setLock2faUntil(null);
        // Sinh refresh token
        String refreshToken = java.util.UUID.randomUUID().toString();
        Date refreshTokenExpiry = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000); // 7 ngày
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(refreshTokenExpiry);
        // Kiểm tra thiết bị lạ
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        boolean isNewDevice = user.getLastLoginIp() == null || user.getLastLoginUserAgent() == null ||
                !user.getLastLoginIp().equals(ipAddress) || !user.getLastLoginUserAgent().equals(userAgent);
        if (isNewDevice && (user.getRole() == User.UserRole.GLOBAL_ADMIN || user.getRole() == User.UserRole.SCHOOL_MANAGER || user.getRole() == User.UserRole.FACULTY_ADMIN)) {
            mailService.sendLoginAlertMail(user.getEmail(), ipAddress, userAgent, new Date());
        }
        user.setLastLoginIp(ipAddress);
        user.setLastLoginUserAgent(userAgent);
        userService.save(user);
        auditService.log2faAction(user.getId(), "2FA_VERIFIED", "Xác thực 2FA thành công");
        // Trả JWT như login thành công
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtService.generateToken(userDetails);
        Date tokenExpiration = jwtService.extractExpiration(jwt);
        userSessionService.createSession(user.getEmail(), jwt, userAgent, ipAddress);
        createJwtCookie(response, jwt, tokenExpiration);
        createRefreshTokenCookie(response, refreshToken, refreshTokenExpiry);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("role", user.getRole());
        userInfo.put("department", user.getDepartment());
        userInfo.put("className", user.getClassName());
        userInfo.put("cohort", user.getCohort());
        userInfo.put("academicYear", user.getAcademicYear());
        userInfo.put("currentYear", user.getCurrentYear());
        userInfo.put("enrollmentDate", user.getEnrollmentDate());
        userInfo.put("trainingPoints1", user.getTrainingPoints1());
        userInfo.put("trainingPoints2", user.getTrainingPoints2());
        userInfo.put("trainingPoints3", user.getTrainingPoints3());
        userInfo.put("trainingPoints4", user.getTrainingPoints4());
        userInfo.put("trainingPoints5", user.getTrainingPoints5());
        userInfo.put("trainingPoints6", user.getTrainingPoints6());
        userInfo.put("trainingPoints7", user.getTrainingPoints7());
        userInfo.put("trainingPoints8", user.getTrainingPoints8());
        userInfo.put("socialPoints", user.getSocialPoints());
        userInfo.put("lastLogin", user.getLastLogin());
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login + 2FA thành công");
        // Chỉ trả về token/refreshToken nếu KHÔNG phải là admin/manager
        if (user.getRole() != User.UserRole.GLOBAL_ADMIN &&
            user.getRole() != User.UserRole.SCHOOL_MANAGER &&
            user.getRole() != User.UserRole.FACULTY_ADMIN) {
            responseBody.put("token", jwt);
            responseBody.put("refreshToken", refreshToken);
        }
        responseBody.put("tokenExpiration", tokenExpiration);
        responseBody.put("user", userInfo);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2fa(@RequestParam String email) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        user.setTwoFactorEnabled(true);
        userService.save(user);
        auditService.log2faAction(user.getId(), "2FA_ENABLED", "Bật xác thực 2FA qua email");
        return ResponseEntity.ok(Map.of("message", "Đã bật xác thực 2FA qua email"));
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(@RequestParam String email) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        user.setTwoFactorEnabled(false);
        userService.save(user);
        auditService.log2faAction(user.getId(), "2FA_DISABLED", "Tắt xác thực 2FA qua email");
        return ResponseEntity.ok(Map.of("message", "Đã tắt xác thực 2FA qua email"));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String email) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));
        user.setTwoFactorOtp(otp);
        user.setTwoFactorOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        user.setTwoFactorVerified(false);
        userService.save(user);
        mailService.sendOtpMail(user.getEmail(), otp);
        auditService.log2faAction(user.getId(), "2FA_OTP_RESENT", "Gửi lại OTP xác thực 2FA qua email");
        return ResponseEntity.ok(Map.of("message", "Đã gửi lại OTP tới email"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Lấy refresh token từ cookie
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No refresh token found"));
            }
            // Tìm user theo refresh token
            User user = userService.findByEmailOrRefreshToken(null, refreshToken);
            if (user == null || user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refresh token không hợp lệ"));
            }
            if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().before(new Date())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refresh token đã hết hạn. Vui lòng đăng nhập lại."));
            }
            // Sinh access token mới
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            String newToken = jwtService.generateToken(userDetails);
            Date tokenExpiration = jwtService.extractExpiration(newToken);
            createJwtCookie(response, newToken, tokenExpiration);
            // Rotate refresh token (tùy chọn, tăng bảo mật)
            String newRefreshToken = java.util.UUID.randomUUID().toString();
            Date newRefreshTokenExpiry = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);
            user.setRefreshToken(newRefreshToken);
            user.setRefreshTokenExpiry(newRefreshTokenExpiry);
            userService.save(user);
            createRefreshTokenCookie(response, newRefreshToken, newRefreshTokenExpiry);
            // Thêm token và refreshToken vào body cho mobile nếu KHÔNG phải là admin/manager
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Token refreshed successfully");
            map.put("tokenExpiration", tokenExpiration);
            if (user.getRole() != User.UserRole.GLOBAL_ADMIN &&
                user.getRole() != User.UserRole.SCHOOL_MANAGER &&
                user.getRole() != User.UserRole.FACULTY_ADMIN) {
                map.put("token", newToken);
                map.put("refreshToken", newRefreshToken);
            }
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        try {
            userService.sendResetPasswordOtp(email);
            return ResponseEntity.ok(Map.of("message", "Đã gửi mã OTP đặt lại mật khẩu tới email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        String newPassword = req.get("newPassword");
        if (email == null || otp == null || newPassword == null || email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, OTP và mật khẩu mới là bắt buộc"));
        }
        try {
            userService.resetPasswordWithOtp(email, otp, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Lấy token từ cookie hoặc header
            String token = cookieUtils.getJwtToken(request);

            if (token != null) {
                // Thêm token vào blacklist
                jwtService.blacklistToken(token);
                // Đóng session
                userSessionService.closeSession(token);
            }

            // Xóa cookie
            deleteJwtCookie(response);

            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Logout failed"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus(HttpServletRequest request) {
        try {
            // Lấy token từ cookie hoặc header
            String token = cookieUtils.getJwtToken(request);

            if (token == null) {
                return ResponseEntity.ok(Map.of("authenticated", false, "message", "No token found"));
            }

            // Kiểm tra token có hợp lệ không
            String email = jwtService.extractUsername(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            if (jwtService.validateToken(token, userDetails)) {
                Date tokenExpiration = jwtService.extractExpiration(token);
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "tokenExpiration", tokenExpiration,
                    "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.ok(Map.of("authenticated", false, "message", "Token is invalid or expired"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("authenticated", false, "message", "Authentication check failed"));
        }
    }

    @PostMapping("/logout-all")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<?> logoutAllUsers(@RequestParam String username) {
        try {
            userSessionService.forceLogoutAllSessions(username);
            return ResponseEntity.ok(Map.of("message", "All sessions logged out for user: " + username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to logout all sessions"));
        }
    }

    /**
     * Lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
