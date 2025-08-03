package com.admin.event_management_backend_java_spring.school.controller;

import com.admin.event_management_backend_java_spring.school.model.School;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.school.payload.request.SchoolRequest;
import com.admin.event_management_backend_java_spring.school.payload.response.SchoolResponse;
import com.admin.event_management_backend_java_spring.school.service.SchoolService;
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
@RequestMapping("/api/schools")
@PreAuthorize("hasRole('GLOBAL_ADMIN')")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @Operation(summary = "Tạo trường học", description = "Tạo mới một trường học.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSchool(@Valid @RequestBody SchoolRequest req , @RequestBody String createBy) {
        ApiResponse<?> response = schoolService.createSchool(req,createBy);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Lấy thông tin trường học theo ID", description = "Trả về thông tin chi tiết của trường học theo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchoolById(
            @Parameter(description = "ID của trường học") @PathVariable String id) {
        ApiResponse<SchoolResponse> response = schoolService.getSchoolById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy danh sách tất cả trường học", description = "Trả về danh sách tất cả trường học.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getAllSchools() {
        ApiResponse<List<SchoolResponse>> response = schoolService.getAllSchools();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy danh sách trường học đang hoạt động", description = "Trả về danh sách các trường học đang hoạt động.")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getActiveSchools() {
        ApiResponse<List<SchoolResponse>> response = schoolService.getActiveSchools();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật trường học", description = "Cập nhật thông tin trường học theo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateSchool(
            @Parameter(description = "ID của trường học") @PathVariable String id,
            @Valid @RequestBody SchoolRequest req) {
        ApiResponse<?> response = schoolService.updateSchool(id, req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Cập nhật trạng thái trường học", description = "Cập nhật trạng thái hoạt động của trường học theo ID.")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SchoolResponse>> updateSchoolStatus(
            @Parameter(description = "ID của trường học") @PathVariable String id,
            @Parameter(description = "Trạng thái mới của trường học") @RequestParam School.SchoolStatus status,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        ApiResponse<SchoolResponse> response = schoolService.updateSchoolStatus(id, status, updatedBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired-subscriptions")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getExpiredSubscriptions() {
        ApiResponse<List<SchoolResponse>> response = schoolService.getExpiredSubscriptions();
        return ResponseEntity.ok(response);
    }
}
