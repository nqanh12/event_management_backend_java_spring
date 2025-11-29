package com.admin.event_management_backend_java_spring.feedback.payload.response;

import lombok.Data;
import java.util.Date;

@Data
public class FeedbackResponse {
    private String id;
    private String userName;
    private String eventName;
    private String content;
    private Integer rating;
    private String response;
    private Date createdAt;
}
