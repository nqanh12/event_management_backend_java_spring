package com.admin.event_management_backend_java_spring.integration.service;

import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class ExternalIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.integration.timeout:30000}")
    private int timeout;
    
    @Value("${app.integration.retry-attempts:3}")
    private int retryAttempts;
    
    @Value("${app.integration.enabled:true}")
    private boolean integrationEnabled;
    
    public ExternalIntegrationService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Tích hợp với hệ thống quản lý sinh viên
     */
    public ApiResponse<Map<String, Object>> syncStudentData(String studentId) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            String url = "/api/students/" + studentId;
            Map<String, Object> studentData = makeRequest(url, HttpMethod.GET, null, Map.class);
            
            return new ApiResponse<>(true, "Student data synced successfully", studentData);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to sync student data: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tích hợp với hệ thống email
     */
    public ApiResponse<String> sendEmailNotification(String recipient, String subject, String content, String template) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("recipient", recipient);
            emailData.put("subject", subject);
            emailData.put("content", content);
            emailData.put("template", template);
            
            String url = "/api/email/send";
            Map<String, Object> response = makeRequest(url, HttpMethod.POST, emailData, Map.class);
            
            String messageId = (String) response.get("messageId");
            return new ApiResponse<>(true, "Email sent successfully", messageId);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to send email: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tích hợp với hệ thống SMS
     */
    public ApiResponse<String> sendSmsNotification(String phoneNumber, String message) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            Map<String, Object> smsData = new HashMap<>();
            smsData.put("phoneNumber", phoneNumber);
            smsData.put("message", message);
            
            String url = "/api/sms/send";
            Map<String, Object> response = makeRequest(url, HttpMethod.POST, smsData, Map.class);
            
            String messageId = (String) response.get("messageId");
            return new ApiResponse<>(true, "SMS sent successfully", messageId);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to send SMS: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tích hợp với hệ thống báo cáo tổng hợp
     */
    public ApiResponse<String> generateExternalReport(String reportType, Map<String, Object> parameters) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportType", reportType);
            reportData.put("parameters", parameters);
            
            String url = "/api/reports/generate";
            Map<String, Object> response = makeRequest(url, HttpMethod.POST, reportData, Map.class);
            
            String reportId = (String) response.get("reportId");
            return new ApiResponse<>(true, "Report generation started", reportId);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to generate report: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tích hợp với hệ thống xác thực SSO
     */
    public ApiResponse<Map<String, Object>> validateSsoToken(String token) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            String url = "/api/auth/validate";
            Map<String, Object> userInfo = makeRequest(url, HttpMethod.GET, null, Map.class, headers);
            
            return new ApiResponse<>(true, "Token validated successfully", userInfo);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to validate SSO token: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tích hợp với hệ thống lưu trữ file
     */
    public ApiResponse<String> uploadFile(byte[] fileContent, String fileName, String contentType) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Simulate file upload
            Map<String, Object> uploadData = new HashMap<>();
            uploadData.put("fileName", fileName);
            uploadData.put("contentType", contentType);
            uploadData.put("size", fileContent.length);
            
            String url = "/api/files/upload";
            Map<String, Object> response = makeRequest(url, HttpMethod.POST, uploadData, Map.class, headers);
            
            String fileUrl = (String) response.get("fileUrl");
            return new ApiResponse<>(true, "File uploaded successfully", fileUrl);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to upload file: " + e.getMessage(), null);
        }
    }
    
    /**
     * Kiểm tra trạng thái kết nối với các hệ thống external
     */
    public ApiResponse<Map<String, Object>> checkExternalSystemsHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        if (!integrationEnabled) {
            healthStatus.put("status", "DISABLED");
            healthStatus.put("message", "Integration is disabled");
            return new ApiResponse<>(true, "Integration status retrieved", healthStatus);
        }
        
        List<String> services = List.of("email", "sms", "storage", "sso", "reporting");
        Map<String, String> serviceStatus = new HashMap<>();
        boolean allHealthy = true;
        
        for (String service : services) {
            try {
                String url = "/api/health/" + service;
                makeRequest(url, HttpMethod.GET, null, Map.class);
                serviceStatus.put(service, "HEALTHY");
            } catch (Exception e) {
                serviceStatus.put(service, "UNHEALTHY");
                allHealthy = false;
            }
        }
        
        healthStatus.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        healthStatus.put("services", serviceStatus);
        healthStatus.put("timestamp", new java.util.Date());
        
        return new ApiResponse<>(true, "Health check completed", healthStatus);
    }
    
    /**
     * Generic method to make HTTP requests with retry logic
     */
    private <T> T makeRequest(String url, HttpMethod method, Object requestBody, Class<T> responseType) {
        return makeRequest(url, method, requestBody, responseType, new HttpHeaders());
    }
    
    private <T> T makeRequest(String url, HttpMethod method, Object requestBody, Class<T> responseType, HttpHeaders headers) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                if (headers == null) {
                    headers = new HttpHeaders();
                }
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else {
                    throw new RestClientException("HTTP " + response.getStatusCode() + ": " + response.getBody());
                }
            } catch (Exception e) {
                lastException = e;
                if (attempt < retryAttempts) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed after " + retryAttempts + " attempts", lastException);
    }
    
    /**
     * Sync data từ external systems
     */
    public ApiResponse<Map<String, Object>> syncExternalData(String dataType, Map<String, Object> filters) {
        if (!integrationEnabled) {
            return new ApiResponse<>(false, "Integration is disabled", null);
        }
        
        try {
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("dataType", dataType);
            syncRequest.put("filters", filters);
            syncRequest.put("timestamp", new java.util.Date());
            
            String url = "/api/sync/" + dataType;
            Map<String, Object> response = makeRequest(url, HttpMethod.POST, syncRequest, Map.class);
            
            return new ApiResponse<>(true, "Data sync completed", response);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to sync external data: " + e.getMessage(), null);
        }
    }
}
