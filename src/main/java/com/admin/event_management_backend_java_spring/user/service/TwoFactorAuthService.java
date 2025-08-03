package com.admin.event_management_backend_java_spring.user.service;

import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem 2FA có được bật không
        if (!user.getTwoFactorEnabled()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Kiểm tra xem có bị khóa tạm thời không
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new Date())) {
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

        // Gửi email
        sendOtpEmail(user.getEmail(), otp);
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

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    public void disable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

    private void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("2FA Verification Code");
            message.setText("Your 2FA verification code is: " + otp + "\n\nThis code will expire in " + OTP_EXPIRY_MINUTES + " minutes.");
            
            mailSender.send(message);
            log.info("2FA OTP sent to email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send 2FA OTP email", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
} 