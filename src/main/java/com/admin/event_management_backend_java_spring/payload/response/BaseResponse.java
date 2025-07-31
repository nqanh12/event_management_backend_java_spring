package com.admin.event_management_backend_java_spring.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    
    private boolean success;
    private String message;
    private String code;
    private Date timestamp;
    private String path;
    private Map<String, Object> metadata;
    
    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = new Date();
    }
    
    public BaseResponse(boolean success, String message, String code) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.timestamp = new Date();
    }
    
    public static BaseResponse success(String message) {
        return new BaseResponse(true, message);
    }
    
    public static BaseResponse success(String message, String code) {
        return new BaseResponse(true, message, code);
    }
    
    public static BaseResponse error(String message) {
        return new BaseResponse(false, message);
    }
    
    public static BaseResponse error(String message, String code) {
        return new BaseResponse(false, message, code);
    }
    
    public BaseResponse withPath(String path) {
        this.path = path;
        return this;
    }
    
    public BaseResponse withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    public BaseResponse withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}
