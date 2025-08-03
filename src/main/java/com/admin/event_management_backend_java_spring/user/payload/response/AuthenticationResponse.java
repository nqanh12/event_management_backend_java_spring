package com.admin.event_management_backend_java_spring.user.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private boolean authenticated;
    private boolean requires2FA;
    private String message;
    private String role;
    private String departmentId;
    private Date expiresAt;
} 