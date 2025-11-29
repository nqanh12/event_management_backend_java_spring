package com.admin.event_management_backend_java_spring.registration.model;

import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    @Id
    private String id;
    @DBRef
    @Indexed
    private Event event;
    @DBRef
    @Indexed
    private User user;
    private RegistrationStatus status;
    private Date checkInTime;
    private Date checkOutTime;
    private Integer pointsAwarded;
    
    // Points processing tracking
    private PointsProcessingStatus pointsProcessingStatus = PointsProcessingStatus.PENDING;
    private String pointsProcessingReason;
    private String processedBy;
    private Date processedAt;

    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;

    public enum RegistrationStatus {
        REGISTERED, ATTENDED, ABSENT, CANCELLED
    }
    
    public enum PointsProcessingStatus {
        PENDING, AUTO_AWARDED, MANUAL_AWARDED, MANUAL_DEDUCTED, MANUAL_PENALIZED, MANUAL_IGNORED, PROCESSED
    }
} 