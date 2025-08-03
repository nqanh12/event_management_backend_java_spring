package com.admin.event_management_backend_java_spring.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum ErrorCode {
    USER_NOT_FOUND(1000, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTS(1001, "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1002, "Invalid password", HttpStatus.BAD_REQUEST),
    PERMISSION_DENIED(1003, "Permission denied", HttpStatus.FORBIDDEN),
    EVENT_NOT_FOUND(1004, "Event not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_NOT_FOUND(1005, "Department not found", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND(1006, "Course not found", HttpStatus.NOT_FOUND),
    REGISTRATION_NOT_FOUND(1007, "Registration not found", HttpStatus.NOT_FOUND),
    INVALID_ROLE(1008, "Invalid role", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(1009, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(1010, "Bad request", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1011, "Invalid or expired token", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1012, "Invalid input", HttpStatus.BAD_REQUEST),
    SCHOOL_NOT_FOUND(1013, "School not found", HttpStatus.NOT_FOUND),
    STUDENT_ID_EXISTS(1014, "Student ID already exists", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1015, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED(1016, "Authentication failed", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(1017, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(1018, "Token generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_PARSING_FAILED(1019, "Token parsing failed", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
