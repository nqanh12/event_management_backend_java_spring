package com.admin.event_management_backend_java_spring.registration.payload.response;

import lombok.Data;
import java.util.Date;

@Data
public class RegistrationResponse {
    private String id;
    private String eventName;
    private String userName;
    private String status;
    private Date checkInTime;
    private Date checkOutTime;
    private Integer pointsAwarded;
} 