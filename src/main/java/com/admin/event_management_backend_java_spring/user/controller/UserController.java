package com.admin.event_management_backend_java_spring.user.controller;

import com.admin.event_management_backend_java_spring.user.payload.request.UpdatePointsRequest;
import com.admin.event_management_backend_java_spring.user.payload.response.UserResponse;
import com.admin.event_management_backend_java_spring.user.payload.response.BulkCreateUserResult;
import com.admin.event_management_backend_java_spring.user.payload.request.ChangePasswordRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.UpdateProfileRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.AdminCreateUserRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.ForgotPasswordRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.ResetPasswordRequest;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.service.UserService;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.payload.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.util.Calendar;
import java.util.Date;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Cập nhật điểm người dùng", description = "Cập nhật điểm cho người dùng hiện tại.")
    @PutMapping("/points")
    public ResponseEntity<ApiResponse<UserResponse>> updatePoints(@Valid @RequestBody UpdatePointsRequest req, Authentication authentication) {
        String currentUserId = authentication.getName();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        ApiResponse<UserResponse> response = userService.updatePoints(req, currentUser);
        return ResponseEntity.status(response.isSuccess() ? 200 : 403).body(response);
    }

//    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cập nhật thông tin hồ sơ người dùng hiện tại.")
//    @PutMapping("/profile")
//    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest req, Authentication authentication) {
//        String userId = authentication.getName();
//        ApiResponse<UserResponse> response = userService.updateProfile(req, userId);
//        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
//    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng hiện tại.")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest req, Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<String> response = userService.changePassword(req, userId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin hồ sơ của người dùng hiện tại.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String mail = authentication.getName();
        ApiResponse<UserResponse> response = userService.getUserProfile(mail);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin-create")
    public ResponseEntity<ApiResponse<UserResponse>> adminCreateUser(@Valid @RequestBody AdminCreateUserRequest req) {
        ApiResponse<UserResponse> response = userService.adminCreateUser(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-create")
    public ResponseEntity<ApiResponse<BulkCreateUserResult>> bulkCreateUsers(@RequestParam("file") MultipartFile file) {
        ApiResponse<BulkCreateUserResult> response = userService.bulkCreateUsersFromExcel(file);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        ApiResponse<String> response = userService.forgotPassword(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        ApiResponse<PaginatedResponse<UserResponse>> response = userService.getUsersWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> searchUsers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ApiResponse<PaginatedResponse<UserResponse>> response = userService.searchUsers(searchTerm, page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            ApiResponse<PaginatedResponse<UserResponse>> response = userService.getUsersByRole(userRole, page, size);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid role: " + role, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsersByDepartment(
            @PathVariable String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ApiResponse<PaginatedResponse<UserResponse>> response = userService.getUsersByDepartment(departmentId, page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/top-students")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getTopStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<PaginatedResponse<UserResponse>> response = userService.getTopStudents(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getActiveUsers(
            @RequestParam(defaultValue = "7") int daysAgo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        Date since = cal.getTime();

        ApiResponse<PaginatedResponse<UserResponse>> response = userService.getActiveUsers(since, page, size);
        return ResponseEntity.ok(response);
    }
}
