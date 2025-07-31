package com.admin.event_management_backend_java_spring.user.service;

import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.admin.event_management_backend_java_spring.user.payload.response.UserResponse;
import com.admin.event_management_backend_java_spring.user.payload.request.UpdateProfileRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.ChangePasswordRequest;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import com.admin.event_management_backend_java_spring.user.payload.request.AdminCreateUserRequest;
import java.util.Random;
import com.admin.event_management_backend_java_spring.integration.MailService;
import com.admin.event_management_backend_java_spring.user.payload.response.BulkCreateUserResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import com.admin.event_management_backend_java_spring.user.payload.request.ForgotPasswordRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.ResetPasswordRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.UpdatePointsRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.UpdatePointsRequest;

import java.util.UUID;
import java.util.Date;
import com.admin.event_management_backend_java_spring.payload.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.admin.event_management_backend_java_spring.audit.service.AuditService;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private AuditService auditService;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public UserResponse toUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setStudentId(user.getStudentId());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);
        dto.setClassName(user.getClassName());
        dto.setCohort(user.getCohort());
        dto.setAcademicYear(user.calculateAcademicYear());
        dto.setCurrentYear(user.calculateCurrentYear());
        // Tính tổng training points từ 8 học kỳ
        int totalTrainingPoints = 0;
        if (user.getTrainingPoints1() != null) totalTrainingPoints += user.getTrainingPoints1().intValue();
        if (user.getTrainingPoints2() != null) totalTrainingPoints += user.getTrainingPoints2().intValue();
        if (user.getTrainingPoints3() != null) totalTrainingPoints += user.getTrainingPoints3().intValue();
        if (user.getTrainingPoints4() != null) totalTrainingPoints += user.getTrainingPoints4().intValue();
        if (user.getTrainingPoints5() != null) totalTrainingPoints += user.getTrainingPoints5().intValue();
        if (user.getTrainingPoints6() != null) totalTrainingPoints += user.getTrainingPoints6().intValue();
        if (user.getTrainingPoints7() != null) totalTrainingPoints += user.getTrainingPoints7().intValue();
        if (user.getTrainingPoints8() != null) totalTrainingPoints += user.getTrainingPoints8().intValue();
        dto.setTrainingPoints(totalTrainingPoints);
        dto.setSocialPoints(user.getSocialPoints() != null ? user.getSocialPoints().intValue() : 0);
        return dto;
    }


    public ApiResponse<UserResponse> updatePoints(UpdatePointsRequest req, User currentUser) {
        log.info("[ADMIN/USER] Cập nhật điểm cho userId: {} bởi user: {}", req.getUserId(), currentUser != null ? currentUser.getEmail() : "system");
        User user = userRepository.findById(req.getUserId())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // Note: This method is deprecated. Use PointsService for updating points instead.
        // This method is kept for backward compatibility but should not be used for new features.

        // Apply points based on operation type
        Integer finalPoints = req.calculateFinalPoints();
        if (finalPoints != null) {
            switch (req.getOperation()) {
                case ADD:
                case BONUS:
                    // Add to social points (simplified legacy behavior)
                    Double currentSocial = user.getSocialPoints() != null ? user.getSocialPoints() : 0.0;
                    user.setSocialPoints(currentSocial + finalPoints);
                    break;
                case SET:
                    user.setSocialPoints(finalPoints.doubleValue());
                    break;
                default:
                    // For other operations, just add to social points
                    Double currentPoints = user.getSocialPoints() != null ? user.getSocialPoints() : 0.0;
                    user.setSocialPoints(currentPoints + finalPoints);
                    break;
            }
        }

        userRepository.save(user);
        return new ApiResponse<>(true, "Points updated", toUserResponse(user));
    }


//    public ApiResponse<UserResponse> updateProfile(UpdateProfileRequest req, String userId) {
//        log.info("[USER] Cập nhật profile cho userId: {}", userId);
//        User user = userRepository.findById(userId)
//            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
//        user.setFullName(req.getFullName());
//        if (req.getDepartmentId() != null) {
//            Department dep = departmentRepository.findById(req.getDepartmentId())
//                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND, "Department not found"));
//            user.setDepartment(dep);
//        }
//        if (req.getClassName() != null) {
//            user.setClassName(req.getClassName());
//        }
//        if (req.getCohort() != null) {
//            user.setCohort(req.getCohort());
//            // Tự động cập nhật academic year và current year
//            user.setAcademicYear(user.calculateAcademicYear());
//            user.setCurrentYear(user.calculateCurrentYear());
//        }
//        userRepository.save(user);
//        return new ApiResponse<>(true, "Profile updated", toUserResponse(user));
//    }


    public ApiResponse<String> changePassword(ChangePasswordRequest req, String userId) {
        log.info("[USER] Đổi mật khẩu cho userId: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, "Old password incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return new ApiResponse<>(true, "Password changed successfully", null);
    }


    public ApiResponse<UserResponse> getUserProfile(String mail) {
        User user = userRepository.findByEmail(mail)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        return new ApiResponse<>(true, "Success", toUserResponse(user));
    }


    public ApiResponse<UserResponse> adminCreateUser(AdminCreateUserRequest req) {
        // Lấy email người thực hiện từ context (SecurityContextHolder)
        String createdByEmail = null;
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                createdByEmail = userDetails.getUsername();
            } else if (authentication != null && authentication.getPrincipal() instanceof String) {
                createdByEmail = (String) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.warn("[ADMIN] Không lấy được email từ context, dùng ADMIN mặc định");
        }
        log.info("[ADMIN] Tạo user mới với email: {} và role: {}", req.getEmail(), req.getRole());
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            log.warn("[ADMIN] Email đã tồn tại: {}", req.getEmail());
            throw new AppException(ErrorCode.EMAIL_EXISTS, "Email already exists");
        }

        // Kiểm tra mã sinh viên nếu tạo tài khoản sinh viên
        if ("STUDENT".equals(req.getRole()) && req.getStudentId() != null) {
            if (userRepository.findByStudentId(req.getStudentId()).isPresent()) {
                log.warn("[ADMIN] Student ID đã tồn tại: {}", req.getStudentId());
                throw new AppException(ErrorCode.STUDENT_ID_EXISTS, "Student ID already exists");
            }
        }

        User.UserRole role;
        try {
            role = User.UserRole.valueOf(req.getRole());
        } catch (Exception e) {
            log.error("[ADMIN] Role không hợp lệ: {}", req.getRole());
            throw new AppException(ErrorCode.INVALID_ROLE, "Invalid role");
        }

        // Cập nhật danh sách role được phép tạo
        if (role != User.UserRole.SCHOOL_MANAGER && role != User.UserRole.FACULTY_ADMIN &&
            role != User.UserRole.ORGANIZER && role != User.UserRole.FACULTY_SCANNER &&
            role != User.UserRole.SCHOOL_SCANNER && role != User.UserRole.STUDENT) {
            throw new AppException(ErrorCode.PERMISSION_DENIED,
                "Only allowed roles: SCHOOL_MANAGER, FACULTY_ADMIN, ORGANIZER, FACULTY_SCANNER, SCHOOL_SCANNER, STUDENT");
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("[ADMIN] Department không tồn tại: {}", req.getDepartmentId());
                    return new AppException(ErrorCode.DEPARTMENT_NOT_FOUND, "Department not found");
                });
        }

        String rawPassword = generateRandomPassword(10);
        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setRole(role);
        user.setDepartment(department);
        user.setStudentId(req.getStudentId()); // Mã sinh viên
        user.setClassName(req.getClassName()); // Học lớp
        user.setCohort(req.getCohort()); // Khóa học
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedBy(createdByEmail); // hoặc lấy từ context nếu có
        user.setCreatedAt(new Date());
        user.setUpdatedBy(createdByEmail); // hoặc lấy từ context nếu có
        user.setUpdatedAt(new Date());
        user.setIsDeleted(false);
        user.setDeletedAt(null);

        // Thiết lập thông tin mặc định cho sinh viên
        if (role == User.UserRole.STUDENT) {
            if (req.getCohort() != null) {
                user.setAcademicYear(user.calculateAcademicYear());
                user.setCurrentYear(user.calculateCurrentYear());
            } else {
                user.setAcademicYear("2021-2025"); // Mặc định, có thể cập nhật sau
                user.setCurrentYear(1); // Mặc định năm 1
            }
            user.setEnrollmentDate(new Date());

            // Điểm công tác xã hội mặc định là 0
            user.setSocialPoints(0.0);

            // Điểm rèn luyện mặc định lấy từ trường (school)
            Integer defaultTrainingPoints = null;
            if (department != null && department.getSchool() != null) {
                defaultTrainingPoints = department.getSchool().getDefaultTrainingPoints();
            }
            if (defaultTrainingPoints == null) {
                defaultTrainingPoints = 0; // fallback nếu không có cấu hình
            }
            // Gán cho kỳ 1, các kỳ khác để 0
            user.setTrainingPoints1(defaultTrainingPoints.doubleValue());
            user.setTrainingPoints2(0.0);
            user.setTrainingPoints3(0.0);
            user.setTrainingPoints4(0.0);
            user.setTrainingPoints5(0.0);
            user.setTrainingPoints6(0.0);
            user.setTrainingPoints7(0.0);
            user.setTrainingPoints8(0.0);
        }

        userRepository.save(user);

        // Gửi email thông báo tài khoản mới (HTML)
        Map<String, String> vars = new HashMap<>();
        vars.put("email", req.getEmail());
        vars.put("full_name", req.getFullName());
        vars.put("student_id", req.getStudentId() != null ? req.getStudentId() : "");
        vars.put("class_name", req.getClassName() != null ? req.getClassName() : "");
        vars.put("password", rawPassword);
        String subject = "[Event Management] Tài khoản của bạn";
        mailService.sendHtmlMail(req.getEmail(), subject, "account-created.html", vars);
        log.info("[ADMIN] User mới đã được tạo: email={}, role={}, department={}", req.getEmail(), req.getRole(), department != null ? department.getName() : "N/A");
        // Ghi log Audit
        auditService.logActivity(
            "USER_REGISTER",
            "USER",
            user.getId(),
            "Tạo user mới: " + user.getEmail(),
            null,
            Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole() != null ? user.getRole().name() : null,
                "department", department != null ? department.getName() : null
            ),
            "SUCCESS",
            null
        );
        return new ApiResponse<>(true, "User created and email sent", toUserResponse(user));
    }


    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }


    public ApiResponse<BulkCreateUserResult> bulkCreateUsersFromExcel(MultipartFile file) {
        int success = 0, fail = 0;
        ArrayList<String> failDetails = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua header
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String email = row.getCell(0).getStringCellValue();
                    String fullName = row.getCell(1).getStringCellValue();
                    String roleStr = row.getCell(2).getStringCellValue();
                    String departmentId = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : null;
                    String studentId = row.getCell(4) != null ? row.getCell(4).getStringCellValue() : null;
                    String className = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : null;

                    AdminCreateUserRequest req = new AdminCreateUserRequest();
                    req.setEmail(email);
                    req.setFullName(fullName);
                    req.setRole(roleStr);
                    req.setDepartmentId(departmentId);
                    req.setStudentId(studentId);
                    req.setClassName(className);
                    this.adminCreateUser(req); // Sẽ gửi email luôn
                    success++;
                } catch (Exception e) {
                    fail++;
                    failDetails.add("Row " + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, "File error: " + e.getMessage(), null);
        }
        BulkCreateUserResult result = new BulkCreateUserResult();
        result.setSuccessCount(success);
        result.setFailCount(fail);
        result.setFailDetails(failDetails);
        return new ApiResponse<>(true, "Bulk import completed", result);
    }

    public ApiResponse<String> forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 30)); // 30 phút
        userRepository.save(user);
        String resetLink = "https://your-frontend-url/reset-password?token=" + token;
        String subject = "[Event Management] Đặt lại mật khẩu";
        Map<String, String> vars = new HashMap<>();
        vars.put("reset_link", resetLink);
        vars.put("user_email", user.getEmail());
        vars.put("request_time", new java.util.Date().toString());
        vars.put("ip_address", ""); // Có thể lấy từ request nếu truyền vào
        vars.put("user_agent", ""); // Có thể lấy từ request nếu truyền vào
        mailService.sendHtmlMail(user.getEmail(), subject, "reset-password.html", vars);
        return new ApiResponse<>(true, "Đã gửi email đặt lại mật khẩu", null);
    }

    // Xóa hàm resetPassword(ResetPasswordRequest req) cũ và các tham chiếu đến ErrorCode không tồn tại
    // Đã thay thế bằng resetPasswordWithOtp(String email, String otp, String newPassword) ở trên

    public void sendResetPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));
        user.setTwoFactorOtp(otp);
        user.setTwoFactorOtpExpiry(new java.util.Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 phút
        user.setTwoFactorVerified(false);
        user.setFailed2faAttempts(0);
        user.setLock2faUntil(null);
        userRepository.save(user);
        mailService.sendOtpMail(user.getEmail(), otp);
    }

    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (user.getLock2faUntil() != null && user.getLock2faUntil().after(new java.util.Date())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tài khoản bị khóa xác thực OTP tạm thời. Vui lòng thử lại sau.");
        }
        if (user.getTwoFactorOtp() == null || user.getTwoFactorOtpExpiry() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP chưa được gửi hoặc đã hết hạn");
        }
        if (user.getTwoFactorOtpExpiry().before(new java.util.Date())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP đã hết hạn. Vui lòng yêu cầu lại OTP.");
        }
        if (!user.getTwoFactorOtp().equals(otp)) {
            int fail = user.getFailed2faAttempts() != null ? user.getFailed2faAttempts() + 1 : 1;
            user.setFailed2faAttempts(fail);
            if (fail >= 5) {
                user.setLock2faUntil(new java.util.Date(System.currentTimeMillis() + 10 * 60 * 1000)); // Khóa 10 phút
            }
            userRepository.save(user);
            throw new AppException(ErrorCode.BAD_REQUEST, "OTP không đúng");
        }
        // Đúng OTP, xác thực thành công
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTwoFactorOtp(null);
        user.setTwoFactorOtpExpiry(null);
        user.setTwoFactorVerified(true);
        user.setFailed2faAttempts(0);
        user.setLock2faUntil(null);
        userRepository.save(user);
    }

    /**
     * Lấy danh sách users với pagination
     */
    public ApiResponse<PaginatedResponse<UserResponse>> getUsersWithPagination(
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(userResponsePage);

        return new ApiResponse<>(true, "Users retrieved successfully", paginatedResponse);
    }

    /**
     * Tìm kiếm users với pagination
     */
    public ApiResponse<PaginatedResponse<UserResponse>> searchUsers(
            String searchTerm, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);

        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(userResponsePage);

        return new ApiResponse<>(true, "Users search completed", paginatedResponse);
    }

    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        List<UserResponse> responses = users.stream().map(this::toUserResponse).toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    public ApiResponse<PaginatedResponse<UserResponse>> getUsersByRole(User.UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> userPage = userRepository.findByRoleAndIsDeletedFalse(role, pageable);
        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(userResponsePage);
        return new ApiResponse<>(true, "Users by role retrieved successfully", paginatedResponse);
    }

    public ApiResponse<PaginatedResponse<UserResponse>> getUsersByDepartment(String departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> userPage = userRepository.findByDepartmentIdAndIsDeletedFalse(departmentId, pageable);
        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(userResponsePage);
        return new ApiResponse<>(true, "Users by department retrieved successfully", paginatedResponse);
    }

    /**
     * Lấy top students với pagination (tối ưu hóa)
     */
    public ApiResponse<PaginatedResponse<UserResponse>> getTopStudents(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<User> topStudents = userRepository.findTopStudentsBySocialPoints(pageable);

        List<UserResponse> userResponses = topStudents.stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());

        // Tạo PaginatedResponse thủ công vì method trả về List
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            page, size, userRepository.countByRole(User.UserRole.STUDENT),
            (int) Math.ceil((double) userRepository.countByRole(User.UserRole.STUDENT) / size),
            page < (int) Math.ceil((double) userRepository.countByRole(User.UserRole.STUDENT) / size) - 1,
            page > 0, page == 0, page == (int) Math.ceil((double) userRepository.countByRole(User.UserRole.STUDENT) / size) - 1
        );

        PaginatedResponse<UserResponse> paginatedResponse = new PaginatedResponse<>(userResponses, paginationInfo);

        return new ApiResponse<>(true, "Top students retrieved successfully", paginatedResponse);
    }

    /**
     * Lấy active users với pagination
     */
    public ApiResponse<PaginatedResponse<UserResponse>> getActiveUsers(
            Date since, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastLogin").descending());
        Page<User> userPage = userRepository.findActiveUsers(since, pageable);

        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(userResponsePage);

        return new ApiResponse<>(true, "Active users retrieved successfully", paginatedResponse);
    }

    /**
     * Lưu user (dùng cho cập nhật trạng thái 2FA, hoặc các cập nhật khác)
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Tìm user theo email hoặc refresh token
     */
    public User findByEmailOrRefreshToken(String email, String refreshToken) {
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        if (refreshToken != null) {
            return userRepository.findAll().stream()
                .filter(u -> refreshToken.equals(u.getRefreshToken()))
                .findFirst().orElse(null);
        }
        return null;
    }
}
