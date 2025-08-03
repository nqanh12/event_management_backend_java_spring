package com.admin.event_management_backend_java_spring.user.payload.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class AdminCreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private String role; // SCHOOL_MANAGER, FACULTY_ADMIN, ORGANIZER, SCANNER, STUDENT

    private String departmentId;
    private String studentId; // Mã sinh viên - chỉ cần thiết khi role = STUDENT
    private String className; // Học lớp - chỉ cần thiết khi role = STUDENT
    private Integer cohort; // Khóa học - chỉ cần thiết khi role = STUDENT
} 