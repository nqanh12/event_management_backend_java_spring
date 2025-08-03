package com.admin.event_management_backend_java_spring.registration.controller;

import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.registration.payload.request.RegistrationRequest;
import com.admin.event_management_backend_java_spring.registration.payload.response.RegistrationResponse;
import com.admin.event_management_backend_java_spring.registration.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @PreAuthorize("hasAnyRole('STUDENT', 'GUEST')")
    @Operation(summary = "Đăng ký sự kiện", description = "Đăng ký tham gia sự kiện cho người dùng hiện tại.")
    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerEvent(@Valid @RequestBody RegistrationRequest req, Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<RegistrationResponse> response = registrationService.registerEvent(userId, req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('FACULTY_SCANNER', 'SCHOOL_SCANNER')")
    @Operation(summary = "Check-in sự kiện", description = "Check-in cho sự kiện theo ID.")
    @PostMapping("/{eventId}/check-in")
    public ResponseEntity<ApiResponse<RegistrationResponse>> checkIn(
            @Parameter(description = "ID của sự kiện") @PathVariable String eventId,
            Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<RegistrationResponse> response = registrationService.checkIn(userId, eventId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('FACULTY_SCANNER', 'SCHOOL_SCANNER')")
    @Operation(summary = "Check-out sự kiện", description = "Check-out cho sự kiện theo ID.")
    @PostMapping("/{eventId}/check-out")
    public ResponseEntity<ApiResponse<RegistrationResponse>> checkOut(
            @Parameter(description = "ID của sự kiện") @PathVariable String eventId,
            Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<RegistrationResponse> response = registrationService.checkOut(userId, eventId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'GUEST')")
    @PutMapping("/{eventId}/cancel")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancelRegistration(@PathVariable String eventId, Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<RegistrationResponse> response = registrationService.cancelRegistration(userId, eventId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'GUEST')")
    @GetMapping("/my-registrations")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations(Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<List<RegistrationResponse>> response = registrationService.getUserRegistrations(userId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'GUEST')")
    @GetMapping("/my-registrations/{status}")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrationsByStatus(
            @PathVariable String status, Authentication authentication) {
        String userId = authentication.getName();
        Registration.RegistrationStatus registrationStatus;
        try {
            registrationStatus = Registration.RegistrationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Status không hợp lệ", null));
        }
        ApiResponse<List<RegistrationResponse>> response = registrationService.getUserRegistrationsByStatus(userId, registrationStatus);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
} 