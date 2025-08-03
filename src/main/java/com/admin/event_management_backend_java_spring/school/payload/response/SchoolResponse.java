package com.admin.event_management_backend_java_spring.school.payload.response;

import com.admin.event_management_backend_java_spring.school.model.School;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class SchoolResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private School.SchoolStatus status;
    private Boolean enableAuditLog;
    private Boolean enableAdvancedReports;
    private Boolean enableCustomBranding;
    private Integer defaultTrainingPointsPerHour;
    private Integer defaultSocialPointsPerHour;
    private Integer defaultPenaltyPoints;
    private Map<String, String> customSettings;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;

}
