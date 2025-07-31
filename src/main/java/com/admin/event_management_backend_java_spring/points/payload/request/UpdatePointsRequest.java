package com.admin.event_management_backend_java_spring.points.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.admin.event_management_backend_java_spring.academic.model.Semester;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePointsRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    private Semester semester;
    
    private Double trainingPoints;
    
    private Double socialPoints;
    
    private String reason;
    
    private String description;
    
    // Getter methods for backward compatibility
    public Semester getSemester() {
        return semester;
    }
    
    public Double getTrainingPoints() {
        return trainingPoints;
    }
    
    public Double getSocialPoints() {
        return socialPoints;
    }
    
    public String getDescription() {
        return description;
    }
}
