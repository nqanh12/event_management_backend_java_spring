package com.admin.event_management_backend_java_spring.department.service;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.payload.request.DepartmentRequest;
import com.admin.event_management_backend_java_spring.department.payload.request.UpdateDepartmentPenaltyRequest;
import com.admin.event_management_backend_java_spring.department.payload.response.DepartmentResponse;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    private DepartmentResponse toDepartmentResponse(Department dep) {
        DepartmentResponse dto = new DepartmentResponse();
        dto.setId(dep.getId());
        dto.setName(dep.getName());
        dto.setTrainingPointsPenalty(dep.getTrainingPointsPenalty());
        dto.setSocialPointsPenalty(dep.getSocialPointsPenalty());
        
        
        // Audit fields
        dto.setCreatedBy(dep.getCreatedBy());
        dto.setUpdatedBy(dep.getUpdatedBy());
        dto.setCreatedAt(dep.getCreatedAt());
        dto.setUpdatedAt(dep.getUpdatedAt());

        return dto;
    }

    public ApiResponse<?> createDepartment(DepartmentRequest req) {
        log.info("[DEPARTMENT] Tạo khoa/phòng ban mới: {}", req.getName());
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || !(user instanceof org.springframework.security.core.userdetails.User)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        String userId = ((org.springframework.security.core.userdetails.User) user).getUsername();
        
        Department department = new Department();
        department.setName(req.getName());
        
        // Set penalty points with defaults if not provided
        department.setTrainingPointsPenalty(req.getTrainingPointsPenalty() != null ? 
            req.getTrainingPointsPenalty() : 4);
        department.setSocialPointsPenalty(req.getSocialPointsPenalty() != null ? 
            req.getSocialPointsPenalty() : 10);
        
        // Set audit fields
        Date now = new Date();
        department.setCreatedAt(now);
        department.setUpdatedAt(now);
        department.setCreatedBy(userId);
        department.setUpdatedBy(userId);
        
        departmentRepository.save(department);
        return new ApiResponse<>(true, "Department created successfully", toDepartmentResponse(department));
    }

    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentResponse> responses = departments.stream().map(this::toDepartmentResponse).toList();
        return new ApiResponse<>(true, "Departments retrieved successfully", responses);
    }

    public ApiResponse<?> updateDepartment(String id, DepartmentRequest req) {
        log.info("[DEPARTMENT] Cập nhật khoa/phòng ban id: {} - {}", id, req.getName());
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || !(user instanceof org.springframework.security.core.userdetails.User)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        String userId = ((org.springframework.security.core.userdetails.User) user).getUsername();
        
        Department department = getDepartmentOrThrow(id);
        department.setName(req.getName());
        
        // Update penalty points if provided
        if (req.getTrainingPointsPenalty() != null) {
            department.setTrainingPointsPenalty(req.getTrainingPointsPenalty());
        }
        if (req.getSocialPointsPenalty() != null) {
            department.setSocialPointsPenalty(req.getSocialPointsPenalty());
        }
        
        // Update audit fields
        department.setUpdatedAt(new Date());
        department.setUpdatedBy(userId);
        
        departmentRepository.save(department);
        return new ApiResponse<>(true, "Department updated successfully", toDepartmentResponse(department));
    }

    public ApiResponse<?> deleteDepartment(String id) {
        log.info("[DEPARTMENT] Xóa khoa/phòng ban id: {}", id);
        Department department = getDepartmentOrThrow(id);
        departmentRepository.delete(department);
        return new ApiResponse<>(true, "Department deleted successfully", null);
    }

    public Department getDepartmentOrThrow(String id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND, "Department not found"));
    }
    
    public ApiResponse<DepartmentResponse> getDepartmentById(String id) {
        Department department = getDepartmentOrThrow(id);
        return new ApiResponse<>(true, "Department retrieved successfully", toDepartmentResponse(department));
    }
    
    public ApiResponse<DepartmentResponse> updateDepartmentPenaltyPoints(String id, UpdateDepartmentPenaltyRequest req) {
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || !(user instanceof org.springframework.security.core.userdetails.User)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        String userId = ((org.springframework.security.core.userdetails.User) user).getUsername();
        
        Department department = getDepartmentOrThrow(id);
        
        // Update penalty points
        if (req.getTrainingPointsPenalty() != null) {
            department.setTrainingPointsPenalty(req.getTrainingPointsPenalty());
        }
        if (req.getSocialPointsPenalty() != null) {
            department.setSocialPointsPenalty(req.getSocialPointsPenalty());
        }
        
        // Update audit fields
        department.setUpdatedAt(new Date());
        department.setUpdatedBy(userId);
        
        departmentRepository.save(department);
        return new ApiResponse<>(true, "Department penalty points updated successfully", toDepartmentResponse(department));
    }
    
    public Optional<Department> findByName(String name) {
        return departmentRepository.findByName(name);
    }
    
    public Department findByNameOrThrow(String name) {
        return departmentRepository.findByName(name)
            .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND, "Department not found with name: " + name));
    }
    
    public ApiResponse<DepartmentResponse> getDepartmentByName(String name) {
        Department department = findByNameOrThrow(name);
        return new ApiResponse<>(true, "Department retrieved successfully", toDepartmentResponse(department));
    }
}
