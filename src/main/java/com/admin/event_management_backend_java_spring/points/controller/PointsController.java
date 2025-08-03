package com.admin.event_management_backend_java_spring.points.controller;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.payload.response.EventPointsReportResponse;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.points.payload.request.BulkUpdatePointsRequest;
import com.admin.event_management_backend_java_spring.points.payload.request.ManualPointsProcessingRequest;
import com.admin.event_management_backend_java_spring.points.payload.request.UpdatePointsRequest;
import com.admin.event_management_backend_java_spring.points.payload.response.BulkUpdatePointsResult;
import com.admin.event_management_backend_java_spring.points.payload.response.ManualPointsProcessingResult;
import com.admin.event_management_backend_java_spring.points.payload.response.PointsProcessingDashboardResponse;
import com.admin.event_management_backend_java_spring.points.service.PointsService;
import com.admin.event_management_backend_java_spring.user.payload.response.UserPointsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*")
public class PointsController {
    @Autowired
    private PointsService pointsService;

    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
    @PostMapping("/events/{eventId}/manual-update")
    public ResponseEntity<ApiResponse<BulkUpdatePointsResult>> manualUpdatePointsForEvent(
            @PathVariable String eventId,
            @Valid @RequestBody BulkUpdatePointsRequest req) {
        ApiResponse<BulkUpdatePointsResult> response = pointsService.manualUpdatePointsForEvent(
            eventId, req.getTrainingPointsToAdd(), req.getSocialPointsToAdd());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
    @GetMapping("/events/{eventId}/report")
    public ResponseEntity<ApiResponse<EventPointsReportResponse>> getEventPointsReport(@PathVariable String eventId) {
        ApiResponse<EventPointsReportResponse> response = pointsService.getEventPointsReport(eventId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
    @PostMapping("/events/{eventId}/manual-process")
    public ResponseEntity<ApiResponse<ManualPointsProcessingResult>> manualProcessPoints(
            @PathVariable String eventId,
            @Valid @RequestBody ManualPointsProcessingRequest req,
            Authentication authentication) {
        String adminId = authentication.getName();
        ApiResponse<ManualPointsProcessingResult> response = pointsService.manualProcessPoints(req, adminId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
    @GetMapping("/events/{eventId}/pending")
    public ResponseEntity<ApiResponse<?>> getPendingManualProcessing(@PathVariable String eventId) {
        ApiResponse<?> response = pointsService.getPendingManualProcessing(eventId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'FACULTY_ADMIN', 'SCHOOL_MANAGER')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PointsProcessingDashboardResponse>> getPointsProcessingDashboard() {
        ApiResponse<PointsProcessingDashboardResponse> response = pointsService.getPointsProcessingDashboard();
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật điểm rèn luyện cho kỳ học cụ thể
     */
    @PutMapping("/training")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN')")
    public ResponseEntity<ApiResponse<UserPointsResponse>> updateTrainingPoints(
            @Valid @RequestBody UpdatePointsRequest request,
            @RequestParam String adminId) {

        ApiResponse<UserPointsResponse> response = pointsService.updateTrainingPoints(request, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật điểm hoạt động xã hội
     */
    @PutMapping("/social")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN')")
    public ResponseEntity<ApiResponse<UserPointsResponse>> updateSocialPoints(
            @Valid @RequestBody UpdatePointsRequest request,
            @RequestParam String adminId) {

        ApiResponse<UserPointsResponse> response = pointsService.updateSocialPoints(request, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin điểm của user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN', 'ORGANIZER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserPointsResponse>> getUserPoints(@PathVariable String userId) {

        ApiResponse<UserPointsResponse> response = pointsService.getUserPoints(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy lịch sử điểm của user
     */
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'SCHOOL_MANAGER', 'FACULTY_ADMIN', 'ORGANIZER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<PointsHistory>>> getUserPointsHistory(@PathVariable String userId) {

        ApiResponse<List<PointsHistory>> response = pointsService.getUserPointsHistory(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách các kỳ học
     */
    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<Semester[]>> getSemesters() {

        ApiResponse<Semester[]> response = new ApiResponse<>(true, "Lấy danh sách kỳ học thành công", Semester.values());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin điểm của user hiện tại
     */
    @GetMapping("/my-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPointsResponse>> getMyPoints(@RequestParam String userId) {

        ApiResponse<UserPointsResponse> response = pointsService.getUserPoints(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy lịch sử điểm của user hiện tại
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PointsHistory>>> getMyPointsHistory(@RequestParam String userId) {

        ApiResponse<List<PointsHistory>> response = pointsService.getUserPointsHistory(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<?>> updatePoints(@Valid @RequestBody UpdatePointsRequest req) {
        ApiResponse<?> response = pointsService.updatePoints(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<?>> manualPointsProcessing(@Valid @RequestBody ManualPointsProcessingRequest req) {
        ApiResponse<?> response = pointsService.manualPointsProcessing(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<?>> bulkUpdatePoints(@Valid @RequestBody BulkUpdatePointsRequest req) {
        ApiResponse<?> response = pointsService.bulkUpdatePoints(req);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}
