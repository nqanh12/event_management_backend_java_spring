package com.admin.event_management_backend_java_spring.event.payload.response;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class EventResponse {
    private String id;
    private String name;
    private String type;
    private Date startTime;
    private Date endTime;
    private String location;
    private String status;
    private String departmentName;
    private String courseName;
    private String organizerName;
    private Integer maxParticipants;
    private String note;
    private Boolean allowCancelRegistration;
    private List<Integer> allowedCohorts;
    private Boolean allowAllCohorts;
} 