package com.admin.event_management_backend_java_spring.user.controller;

import com.admin.event_management_backend_java_spring.user.payload.request.AuthenticationRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.IntrospectRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.LogoutRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.SendOtpRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.VerifyOtpRequest;
import com.admin.event_management_backend_java_spring.user.payload.response.AuthenticationResponse;
import com.admin.event_management_backend_java_spring.user.payload.response.IntrospectResponse;
import com.admin.event_management_backend_java_spring.user.service.AuthenticationService;
import com.admin.event_management_backend_java_spring.user.service.TwoFactorAuthService;
import com.admin.event_management_backend_java_spring.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody LogoutRequest request) {
        try {
            authenticationService.logout(request);
            return ResponseEntity.ok(ApiResponse.success("Logout successful", "Logout successful"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Logout failed: " + e.getMessage()));
        }
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) {
        try {
            IntrospectResponse response = authenticationService.introspect(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Token introspection completed"));
        } catch (Exception e) {
            log.error("Token introspection failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Token introspection failed: " + e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody SendOtpRequest request) {
        try {
            twoFactorAuthService.sendOtp(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", "OTP sent to your email"));
        } catch (Exception e) {
            log.error("Failed to send OTP", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            boolean isValid = twoFactorAuthService.verifyOtp(request.getEmail(), request.getOtp());
            if (isValid) {
                // Sau khi xác thực OTP thành công, tạo token
                AuthenticationRequest authRequest = AuthenticationRequest.builder()
                        .email(request.getEmail())
                        .build();
                AuthenticationResponse response = authenticationService.authenticateAfter2FA(authRequest);
                return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid OTP"));
            }
        } catch (Exception e) {
            log.error("OTP verification failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("OTP verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<ApiResponse<String>> enable2FA(@RequestBody SendOtpRequest request) {
        try {
            twoFactorAuthService.enable2FA(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("2FA enabled", "2FA has been enabled for your account"));
        } catch (Exception e) {
            log.error("Failed to enable 2FA", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to enable 2FA: " + e.getMessage()));
        }
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<ApiResponse<String>> disable2FA(@RequestBody SendOtpRequest request) {
        try {
            twoFactorAuthService.disable2FA(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("2FA disabled", "2FA has been disabled for your account"));
        } catch (Exception e) {
            log.error("Failed to disable 2FA", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to disable 2FA: " + e.getMessage()));
        }
    }
} 