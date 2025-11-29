package com.admin.event_management_backend_java_spring.user.service;

import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import com.admin.event_management_backend_java_spring.integration.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final MailService mailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem có bị khóa tạm thời không
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new Date())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Kiểm tra role để quyết định có gửi OTP không
        boolean shouldSendOtp = shouldSendOtpForUser(user);
        if (!shouldSendOtp) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Tạo OTP
        String otp = generateOtp();
        Date expiryTime = new Date(System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000));

        // Lưu OTP vào database
        user.setTwoFactorOtp(otp);
        user.setTwoFactorOtpExpiry(expiryTime);
        user.setTwoFactorVerified(false);
        userRepository.save(user);

        // Gửi email sử dụng template
        mailService.sendOtpMail(user.getEmail(), otp);
    }

    // Kiểm tra xem có nên gửi OTP cho user không
    private boolean shouldSendOtpForUser(User user) {
        // Admin luôn cần 2FA
        if (isAdminRole(user.getRole())) {
            return true;
        }
        
        // Student và Organization chỉ gửi OTP nếu đã bật 2FA
        if (isStudentOrOrganizationRole(user.getRole())) {
            return user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled();
        }
        
        // Các role khác không gửi OTP
        return false;
    }

    // Kiểm tra xem có phải admin role không
    private boolean isAdminRole(User.UserRole role) {
        return role == User.UserRole.ADMIN || 
               role == User.UserRole.FACULTY_ADMIN;
    }

    // Kiểm tra xem có phải student hoặc organization role không
    private boolean isStudentOrOrganizationRole(User.UserRole role) {
        return role == User.UserRole.STUDENT || 
               role == User.UserRole.ORGANIZER;
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem có bị khóa tạm thời không
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new Date())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Kiểm tra OTP
        if (user.getTwoFactorOtp() == null || 
            user.getTwoFactorOtpExpiry() == null || 
            user.getTwoFactorOtpExpiry().before(new Date()) ||
            !user.getTwoFactorOtp().equals(otp)) {
            
            // Tăng số lần thử sai
            user.setFailed2faAttempts(user.getFailed2faAttempts() + 1);
            
            // Nếu vượt quá số lần cho phép, khóa tạm thời
            if (user.getFailed2faAttempts() >= MAX_FAILED_ATTEMPTS) {
                Date lockUntil = new Date(System.currentTimeMillis() + (LOCK_DURATION_MINUTES * 60 * 1000));
                user.setLock2faUntil(lockUntil);
            }
            
            userRepository.save(user);
            return false;
        }

        // OTP đúng, reset các trường liên quan
        user.setTwoFactorVerified(true);
        user.setFailed2faAttempts(0);
        user.setLock2faUntil(null);
        user.setTwoFactorOtp(null);
        user.setTwoFactorOtpExpiry(null);
        userRepository.save(user);

        return true;
    }

    public void enable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Chỉ cho phép student và organization bật 2FA
        if (!isStudentOrOrganizationRole(user.getRole())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    public void disable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Chỉ cho phép student và organization tắt 2FA
        if (!isStudentOrOrganizationRole(user.getRole())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorVerified(false);
        user.setTwoFactorOtp(null);
        user.setTwoFactorOtpExpiry(null);
        user.setFailed2faAttempts(0);
        user.setLock2faUntil(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }


} 