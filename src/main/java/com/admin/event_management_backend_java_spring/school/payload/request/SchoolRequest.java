package com.admin.event_management_backend_java_spring.school.payload.request;

import com.admin.event_management_backend_java_spring.school.model.School;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class SchoolRequest {

    @NotBlank(message = "School code is required")
    private String code;

    @NotBlank(message = "School name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String website;
    private String contactPerson;
    private String contactPhone;

    @Email(message = "Invalid contact email format")
    private String contactEmail;


    private Integer defaultTrainingPoints = 10;
    private Integer defaultSocialPoints = 5;
    private Integer defaultPenaltyPoints = 5;

    private String billingAddress;
    private String taxId;
    private String paymentMethod;
    private Map<String, String> customSettings;


}
