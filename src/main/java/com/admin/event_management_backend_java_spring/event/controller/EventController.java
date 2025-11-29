package com.admin.event_management_backend_java_spring.event.controller;

import com.admin.event_management_backend_java_spring.event.payload.request.EventRequest;
import com.admin.event_management_backend_java_spring.event.payload.response.EventResponse;
import com.admin.event_management_backend_java_spring.event.service.EventService;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY_ADMIN', 'ADMIN', 'ORGANIZER')")
    @Operation(summary = "Tạo sự kiện mới", description = "Tạo mới một sự kiện với các thông tin chi tiết.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(@Valid @RequestBody EventRequest req , @RequestBody String organizerId ) {
        ApiResponse<?> response = eventService.createEvent(req,organizerId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        ApiResponse<List<EventResponse>> response = eventService.getAllEvents();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<EventResponse>> approveEvent(@PathVariable String id, Authentication authentication) {
        String approverId = authentication.getName();
        ApiResponse<EventResponse> response = eventService.approveEvent(id, approverId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY_ADMIN', 'ADMIN', 'ORGANIZER')")
    @Operation(summary = "Cập nhật sự kiện", description = "Cập nhật thông tin sự kiện theo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(
            @Parameter(description = "ID của sự kiện") @PathVariable String id,
            @Valid @RequestBody EventRequest req , @RequestBody String organizerId) {
        ApiResponse<?> response = eventService.updateEvent(id, req , organizerId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY_ADMIN', 'ADMIN', 'ORGANIZER')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        ApiResponse<EventResponse> response = eventService.cancelEvent(id, userId);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}
