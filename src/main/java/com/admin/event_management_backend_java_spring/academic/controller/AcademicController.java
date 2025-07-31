package com.admin.event_management_backend_java_spring.academic.controller;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.academic.service.AcademicCalendarService;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/academic")
@CrossOrigin(origins = "*")
public class AcademicController {

    @Autowired
    private AcademicCalendarService academicCalendarService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy thông tin học kỳ hiện tại của user
     */
    @GetMapping("/current-semester/{userId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN', 'ORGANIZER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentSemester(@PathVariable String userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User không tồn tại"));

            Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
            String semesterInfo = academicCalendarService.getCurrentSemesterInfo(user);
            boolean inAcademicPeriod = academicCalendarService.isStudentInAcademicPeriod(user);

            Map<String, Object> result = new HashMap<>();
            result.put("currentSemester", currentSemester);
            result.put("semesterInfo", semesterInfo);
            result.put("inAcademicPeriod", inAcademicPeriod);
            result.put("academicYear", user.getAcademicYear());
            result.put("currentYear", user.getCurrentYear());
            result.put("enrollmentDate", user.getEnrollmentDate());

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin học kỳ thành công", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    /**
     * Cập nhật thông tin niên khóa học cho user
     */
    @PutMapping("/update-academic-info/{userId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateAcademicInfo(
            @PathVariable String userId,
            @RequestParam String academicYear,
            @RequestParam(required = false) String enrollmentDateStr) {

        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User không tồn tại"));

            // Validate academic year format (e.g., "2021-2025")
            if (!academicYear.matches("\\d{4}-\\d{4}")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Định dạng niên khóa không hợp lệ. Sử dụng format: yyyy-yyyy", null));
            }

            user.setAcademicYear(academicYear);
            if (enrollmentDateStr != null && !enrollmentDateStr.isEmpty()) {
                try {
                    // Parse date string (format: yyyy-MM-dd)
                    java.time.LocalDate localDate = java.time.LocalDate.parse(enrollmentDateStr);
                    Date enrollmentDate = Date.from(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    user.setEnrollmentDate(enrollmentDate);
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Định dạng ngày không hợp lệ. Sử dụng format: yyyy-MM-dd", null));
                }
            }

            // Tự động tính toán năm học hiện tại
            academicCalendarService.updateCurrentYear(user);

            User updatedUser = userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật thông tin niên khóa thành công", updatedUser));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách các kỳ học
     */
    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<Semester[]>> getSemesters() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách kỳ học thành công", Semester.values()));
    }

    /**
     * Tính toán học kỳ dựa trên ngày cụ thể
     */
    @PostMapping("/calculate-semester-by-date/{userId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN', 'ORGANIZER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateSemesterByDate(
            @PathVariable String userId,
            @RequestParam String targetDateStr) {

        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User không tồn tại"));

            // Parse target date
            Date targetDate;
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(targetDateStr);
                targetDate = Date.from(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Định dạng ngày không hợp lệ. Sử dụng format: yyyy-MM-dd", null));
            }

            Semester semester = academicCalendarService.calculateSemesterByDate(user, targetDate);

            Map<String, Object> result = new HashMap<>();
            result.put("semester", semester);
            result.put("targetDate", targetDate);
            result.put("semesterDisplayName", semester.getDisplayName());

            return ResponseEntity.ok(new ApiResponse<>(true, "Tính toán học kỳ thành công", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy thông tin chi tiết về học kỳ hiện tại
     */
    @GetMapping("/semester-info/{userId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN', 'ORGANIZER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<String>> getSemesterInfo(@PathVariable String userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User không tồn tại"));

            String semesterInfo = academicCalendarService.getCurrentSemesterInfo(user);

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin học kỳ thành công", semesterInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách tất cả users với thông tin học kỳ hiện tại
     */
    @GetMapping("/all-users-semester-info")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsersSemesterInfo() {
        try {
            java.util.List<User> allUsers = userRepository.findAll();
            java.util.List<Map<String, Object>> usersWithSemesterInfo = new ArrayList<>();

            for (User user : allUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("academicYear", user.getAcademicYear());
                userInfo.put("currentYear", user.getCurrentYear());
                userInfo.put("enrollmentDate", user.getEnrollmentDate());

                if (user.getAcademicYear() != null && user.getCurrentYear() != null) {
                    Semester currentSemester = academicCalendarService.calculateCurrentSemester(user);
                    String semesterInfo = academicCalendarService.getCurrentSemesterInfo(user);
                    boolean inAcademicPeriod = academicCalendarService.isStudentInAcademicPeriod(user);

                    userInfo.put("currentSemester", currentSemester);
                    userInfo.put("semesterInfo", semesterInfo);
                    userInfo.put("inAcademicPeriod", inAcademicPeriod);
                } else {
                    userInfo.put("currentSemester", null);
                    userInfo.put("semesterInfo", "Chưa cập nhật thông tin niên khóa");
                    userInfo.put("inAcademicPeriod", false);
                }

                usersWithSemesterInfo.add(userInfo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalUsers", allUsers.size());
            result.put("users", usersWithSemesterInfo);

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin học kỳ của tất cả users thành công", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}
