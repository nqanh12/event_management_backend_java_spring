package com.admin.event_management_backend_java_spring.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.admin.event_management_backend_java_spring.department.model.Department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    // Mã sinh viên - chỉ áp dụng cho role STUDENT
    @Indexed(unique = true, sparse = true) // sparse = true cho phép null values
    private String studentId; // Ví dụ: "2021001234"

    private UserRole role;

    @DBRef
    private Department department;

    // Thay thế courses bằng class (học lớp)
    private String className;

    // Khóa học - năm nhập học
    private Integer cohort; // Ví dụ: 2021, 2022, 2023, 2024
    private String academicYear; // Ví dụ: "2021-2025", "2022-2026" (tự động tính từ cohort)

    // Training Points cho 8 kỳ học riêng biệt
    private Double trainingPoints1 = 0.0;
    private Double trainingPoints2 = 0.0;
    private Double trainingPoints3 = 0.0;
    private Double trainingPoints4 = 0.0;
    private Double trainingPoints5 = 0.0;
    private Double trainingPoints6 = 0.0;
    private Double trainingPoints7 = 0.0;
    private Double trainingPoints8 = 0.0;

    // Social Points tích lũy liên tục trong 4 năm
    private Double socialPoints = 0.0;

    private Date enrollmentDate; // Ngày nhập học
    private Integer currentYear; // Năm học hiện tại (1, 2, 3, 4)

    private Date lastLogin;

    // Thêm cho chức năng reset password
    private String resetToken;
    private Date resetTokenExpiry;

    // 2FA qua email OTP
    private Boolean twoFactorEnabled = false; // Bật/tắt 2FA
    private String twoFactorOtp; // Mã OTP tạm thời
    private Date twoFactorOtpExpiry; // Thời gian hết hạn OTP
    private Boolean twoFactorVerified = false; // Đã xác thực OTP chưa

    // Bảo mật nâng cao
    private String lastLoginIp; // IP đăng nhập gần nhất
    private String lastLoginUserAgent; // User-Agent đăng nhập gần nhất
    private String refreshToken; // Refresh token hiện tại
    private Date refreshTokenExpiry; // Hạn refresh token
    private Integer failed2faAttempts = 0; // Số lần nhập sai OTP liên tiếp
    private Date lock2faUntil; // Nếu bị khóa 2FA tạm thời

    private String createdBy;
    private Date createdAt;

    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;

    public enum UserRole {
            GLOBAL_ADMIN, SCHOOL_MANAGER, FACULTY_ADMIN, ORGANIZER, STUDENT, GUEST, FACULTY_SCANNER, SCHOOL_SCANNER
    }

    // Helper method để tính academic year từ cohort
    public String calculateAcademicYear() {
        if (cohort != null) {
            return cohort + "-" + (cohort + 4);
        }
        return academicYear;
    }

    // Helper method để tính năm học hiện tại
    public Integer calculateCurrentYear() {
        if (cohort != null) {
            int currentYear = java.time.Year.now().getValue();
            int yearInSchool = currentYear - cohort;
            return Math.max(1, Math.min(4, yearInSchool + 1)); // Đảm bảo trong khoảng 1-4
        }
        return currentYear;
    }
}
