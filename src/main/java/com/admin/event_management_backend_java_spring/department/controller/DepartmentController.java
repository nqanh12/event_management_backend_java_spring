package com.admin.event_management_backend_java_spring.department.controller;

import com.admin.event_management_backend_java_spring.department.payload.request.DepartmentRequest;
import com.admin.event_management_backend_java_spring.department.payload.request.UpdateDepartmentPenaltyRequest;
import com.admin.event_management_backend_java_spring.department.service.DepartmentService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Tạo khoa/phòng ban", description = "Tạo mới một khoa hoặc phòng ban.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createDepartment(@Valid @RequestBody DepartmentRequest req) {
        ApiResponse<?> response = departmentService.createDepartment(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Cập nhật khoa/phòng ban", description = "Cập nhật thông tin khoa/phòng ban theo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateDepartment(
            @Parameter(description = "ID của khoa/phòng ban") @PathVariable String id,
            @Valid @RequestBody DepartmentRequest req) {
        ApiResponse<?> response = departmentService.updateDepartment(id, req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Xóa khoa/phòng ban", description = "Xóa khoa/phòng ban theo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDepartment(
            @Parameter(description = "ID của khoa/phòng ban") @PathVariable String id) {
        ApiResponse<?> response = departmentService.deleteDepartment(id);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @Operation(summary = "Lấy danh sách tất cả khoa/phòng ban", description = "Trả về danh sách tất cả khoa/phòng ban.")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllDepartments() {
        ApiResponse<?> response = departmentService.getAllDepartments();
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Lấy thông tin khoa/phòng ban theo ID", description = "Trả về thông tin chi tiết của khoa/phòng ban theo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getDepartmentById(
            @Parameter(description = "ID của khoa/phòng ban") @PathVariable String id) {
        ApiResponse<?> response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Tìm kiếm khoa/phòng ban theo tên", description = "Tìm kiếm khoa/phòng ban theo tên.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> getDepartmentByName(
            @Parameter(description = "Tên khoa/phòng ban") @RequestParam String name) {
        ApiResponse<?> response = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN', 'FACULTY_ADMIN')")
    @PutMapping("/{id}/penalty-points")
    public ResponseEntity<ApiResponse<?>> updateDepartmentPenaltyPoints(
            @PathVariable String id, 
            @Valid @RequestBody UpdateDepartmentPenaltyRequest req) {
        ApiResponse<?> response = departmentService.updateDepartmentPenaltyPoints(id, req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
} 