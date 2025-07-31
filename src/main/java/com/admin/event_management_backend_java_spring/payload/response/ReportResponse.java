package com.admin.event_management_backend_java_spring.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private String reportId;
    private String reportName;
    private String reportType;
    private ReportFormat format;
    private ReportStatus status;
    
    private Date generatedAt;
    private String generatedBy;
    private Date requestedAt;
    
    private String downloadUrl;
    private String filePath;
    private Long fileSize;
    private String fileName;
    
    private ReportParameters parameters;
    private ReportSummary summary;
    private List<ReportError> errors;
    
    private Map<String, Object> metadata;
    
    public enum ReportFormat {
        PDF, EXCEL, CSV, JSON
    }
    
    public enum ReportStatus {
        PENDING, GENERATING, COMPLETED, FAILED, EXPIRED
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportParameters {
        private Date startDate;
        private Date endDate;
        private String departmentId;
        private String eventType;
        private String userRole;
        private List<String> includeFields;
        private List<String> filters;
        private Map<String, Object> additionalParams;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSummary {
        private long totalRecords;
        private long processedRecords;
        private long filteredRecords;
        private double processingTimeSeconds;
        private Map<String, Long> recordCounts;
        private Map<String, Object> statistics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportError {
        private String errorCode;
        private String errorMessage;
        private String field;
        private Object value;
        private Date timestamp;
    }
    
    // Factory methods
    public static ReportResponse pending(String reportId, String reportName, String reportType) {
        ReportResponse response = new ReportResponse();
        response.setReportId(reportId);
        response.setReportName(reportName);
        response.setReportType(reportType);
        response.setStatus(ReportStatus.PENDING);
        response.setRequestedAt(new Date());
        return response;
    }
    
    public static ReportResponse completed(String reportId, String downloadUrl, ReportSummary summary) {
        ReportResponse response = new ReportResponse();
        response.setReportId(reportId);
        response.setStatus(ReportStatus.COMPLETED);
        response.setGeneratedAt(new Date());
        response.setDownloadUrl(downloadUrl);
        response.setSummary(summary);
        return response;
    }
    
    public static ReportResponse failed(String reportId, List<ReportError> errors) {
        ReportResponse response = new ReportResponse();
        response.setReportId(reportId);
        response.setStatus(ReportStatus.FAILED);
        response.setGeneratedAt(new Date());
        response.setErrors(errors);
        return response;
    }
    
    // Utility methods
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    public boolean isPending() {
        return status == ReportStatus.PENDING || status == ReportStatus.GENERATING;
    }
    
    public void addError(String errorCode, String errorMessage) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(new ReportError(errorCode, errorMessage, null, null, new Date()));
    }
    
    public void addError(String errorCode, String errorMessage, String field, Object value) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(new ReportError(errorCode, errorMessage, field, value, new Date()));
    }
}
