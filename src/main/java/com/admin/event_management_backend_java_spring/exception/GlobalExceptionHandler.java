package com.admin.event_management_backend_java_spring.exception;

import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.apache.catalina.connector.ClientAbortException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        log.error("AppException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getErrorCode() == ErrorCode.PERMISSION_DENIED) status = HttpStatus.FORBIDDEN;
        if (ex.getErrorCode() == ErrorCode.USER_NOT_FOUND) status = HttpStatus.NOT_FOUND;
        if (ex.getErrorCode() == ErrorCode.EVENT_NOT_FOUND) status = HttpStatus.NOT_FOUND;
        if (ex.getErrorCode() == ErrorCode.DEPARTMENT_NOT_FOUND) status = HttpStatus.NOT_FOUND;
        if (ex.getErrorCode() == ErrorCode.COURSE_NOT_FOUND) status = HttpStatus.NOT_FOUND;
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle client abort exceptions - these occur when client disconnects before response is sent.
     * This is a normal scenario and should not be logged as an error.
     */
    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbortException(Exception ex) {
        // Log at debug level instead of error - this is normal client behavior
        log.debug("Client disconnected before response was sent: {}", ex.getMessage());
        // Don't return response as connection is already closed
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOther(Exception ex) {
        log.error("Unhandled Exception: {}", ex.getMessage(), ex);
        ApiResponse<Object> response = new ApiResponse<>(false, "Internal server error", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "Invalid input");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
} 