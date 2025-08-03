package com.admin.event_management_backend_java_spring.registration.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Registration type is required")
    private String registrationType;
} 