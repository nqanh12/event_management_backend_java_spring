package com.admin.event_management_backend_java_spring.feedback.model;

import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.event.model.Event;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    private String id;
    @DBRef
    private User user;
    @DBRef
    private Event event;
    private String content;
    private Integer rating; // Rating từ 1-5
    private String response;
    private Date createdAt;
} 