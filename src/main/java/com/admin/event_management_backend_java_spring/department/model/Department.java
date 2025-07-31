package com.admin.event_management_backend_java_spring.department.model;

import com.admin.event_management_backend_java_spring.school.model.School;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    private String id;
    private String name;

    // Penalty points configuration
    private Integer trainingPointsPenalty = 4; // Default penalty for training events
    private Integer socialPointsPenalty = 10;   // Default penalty for social events

    // Multi-tenancy support
    @DBRef
    private School school;

    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;


}
