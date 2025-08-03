package com.admin.event_management_backend_java_spring.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendBulkMailAsync(List<String> recipients, String subject, String content) {
        for (String recipient : recipients) {
            try {
                sendMail(recipient, subject, content);
                Thread.sleep(100); // Small delay to avoid rate limiting
            } catch (Exception e) {
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEventNotificationAsync(List<String> recipients, String eventName, String eventTime, String eventLocation) {
        String subject = "Thông báo sự kiện: " + eventName;
        String content = String.format(
            "Sự kiện: %s\nThời gian: %s\nĐịa điểm: %s\n\nVui lòng tham gia đầy đủ và đúng giờ!",
            eventName, eventTime, eventLocation
        );
        
        return sendBulkMailAsync(recipients, subject, content);
    }

    /**
     * Gửi OTP xác thực 2FA qua email (HTML)
     */
    public void sendOtpMail(String to, String otp) {
        String subject = "Mã xác thực đăng nhập (2FA)";
        Map<String, String> vars = Map.of("otp", otp);
        sendHtmlMail(to, subject, "otp-email.html", vars);
    }

    /**
     * Gửi cảnh báo đăng nhập thiết bị lạ (HTML)
     */
    public void sendLoginAlertMail(String to, String ip, String userAgent, Date time) {
        String subject = "Cảnh báo đăng nhập mới";
        Map<String, String> vars = Map.of(
            "ip", ip,
            "user_agent", userAgent,
            "time", time.toString()
        );
        sendHtmlMail(to, subject, "login-alert.html", vars);
    }

    /**
     * Gửi cảnh báo bảo mật (đổi mật khẩu, bật/tắt 2FA, ...) (HTML)
     */
    public void sendSecurityAlertMail(String to, String action, Date time) {
        String subject = "Cảnh báo bảo mật tài khoản";
        Map<String, String> vars = Map.of(
            "action", action,
            "time", time.toString()
        );
        sendHtmlMail(to, subject, "security-alert.html", vars);
    }

    /**
     * Gửi email HTML sử dụng template và biến động
     */
    public void sendHtmlMail(String to, String subject, String templateName, Map<String, String> variables) {
        try {
            String htmlContent = renderTemplate(templateName, variables);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

    /**
     * Đọc file template và thay thế biến {{var}}
     */
    private String renderTemplate(String templateName, Map<String, String> variables) throws IOException {
        String path = "templates/email/" + templateName;
        InputStream is = new ClassPathResource(path).getInputStream();
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return template;
    }
} 