package com.admin.event_management_backend_java_spring.school.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.Map;

@Document(collection = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // Mã trường học

    @Indexed(unique = true)
    private String name; // Tên trường học

    private String description;
    private String address;
    private String phone;
    private String email;
    private String website;
    
    // Thông tin liên hệ
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    
    // Cấu hình hệ thống
    private SchoolStatus status = SchoolStatus.ACTIVE;
    
    
    // Cấu hình điểm
    private int defaultTrainingPoints = 4;
    private int defaultSocialPoints = 10;
    private int defaultPenaltyPoints = 10;
    
    // Thông tin thanh toán
    private String billingAddress;
    private String taxId;
    private String paymentMethod;
    
    // Metadata
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Custom settings
    private Map<String, Object> customSettings;
    
    public enum SchoolStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_ACTIVATION
    }
    
} 