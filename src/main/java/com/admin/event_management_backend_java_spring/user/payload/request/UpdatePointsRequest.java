package com.admin.event_management_backend_java_spring.user.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePointsRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Integer points;
    
    @NotNull(message = "Operation type is required")
    private PointOperation operation;
    
    private String reason;
    
    private String eventId;
    
    private String activityId;
    
    private String description;
    
    private LocalDateTime effectiveDate;
    
    private String adminId;
    
    private Boolean notifyUser = true;
    
    private PointCategory category;
    
    @Min(value = 1, message = "Multiplier must be at least 1")
    private Double multiplier = 1.0;
    
    private Boolean isReversible = true;
    
    public enum PointOperation {
        ADD("Add points"),
        SUBTRACT("Subtract points"),
        SET("Set points"),
        RESET("Reset points"),
        BONUS("Bonus points"),
        PENALTY("Penalty points"),
        TRANSFER("Transfer points"),
        REFUND("Refund points");
        
        private final String description;
        
        PointOperation(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum PointCategory {
        EVENT_PARTICIPATION("Event Participation"),
        ACADEMIC_ACHIEVEMENT("Academic Achievement"),
        VOLUNTEER_WORK("Volunteer Work"),
        LEADERSHIP("Leadership"),
        SPORTS("Sports"),
        CULTURAL_ACTIVITY("Cultural Activity"),
        RESEARCH("Research"),
        COMMUNITY_SERVICE("Community Service"),
        COMPETITION("Competition"),
        WORKSHOP_ATTENDANCE("Workshop Attendance"),
        PENALTY("Penalty"),
        BONUS("Bonus"),
        CORRECTION("Correction"),
        TRANSFER("Transfer"),
        OTHER("Other");
        
        private final String displayName;
        
        PointCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Calculate final points based on operation and multiplier
     */
    public Integer calculateFinalPoints() {
        if (points == null) {
            return 0;
        }
        
        int basePoints = points;
        
        // Apply multiplier if specified
        if (multiplier != null && multiplier != 1.0) {
            basePoints = (int) Math.round(basePoints * multiplier);
        }
        
        // Apply operation-specific logic
        switch (operation) {
            case SUBTRACT:
            case PENALTY:
                return -Math.abs(basePoints);
            case ADD:
            case BONUS:
                return Math.abs(basePoints);
            case SET:
            case RESET:
                return basePoints;
            default:
                return basePoints;
        }
    }
    
    /**
     * Validate the request
     */
    public boolean isValid() {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        if (points == null || points <= 0) {
            return false;
        }
        
        if (operation == null) {
            return false;
        }
        
        // Validate specific operations
        switch (operation) {
            case TRANSFER:
                return eventId != null || activityId != null;
            case REFUND:
                return reason != null && !reason.trim().isEmpty();
            case SET:
            case RESET:
                return adminId != null;
            default:
                return true;
        }
    }
    
    /**
     * Get audit message for this operation
     */
    public String getAuditMessage() {
        StringBuilder message = new StringBuilder();
        message.append(operation.getDescription())
               .append(" ")
               .append(calculateFinalPoints())
               .append(" points");
        
        if (category != null) {
            message.append(" for ").append(category.getDisplayName());
        }
        
        if (reason != null && !reason.trim().isEmpty()) {
            message.append(" - ").append(reason);
        }
        
        return message.toString();
    }
    
    /**
     * Check if operation requires approval
     */
    public boolean requiresApproval() {
        if (points == null) {
            return false;
        }
        
        // Large point operations require approval
        int finalPoints = Math.abs(calculateFinalPoints());
        if (finalPoints > 100) {
            return true;
        }
        
        // Certain operations always require approval
        switch (operation) {
            case SET:
            case RESET:
            case PENALTY:
            case TRANSFER:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get notification message for user
     */
    public String getNotificationMessage() {
        if (!notifyUser) {
            return null;
        }
        
        int finalPoints = calculateFinalPoints();
        String pointsText = Math.abs(finalPoints) + " point" + (Math.abs(finalPoints) == 1 ? "" : "s");
        
        switch (operation) {
            case ADD:
            case BONUS:
                return "You have earned " + pointsText + "!";
            case SUBTRACT:
            case PENALTY:
                return "You have lost " + pointsText + ".";
            case SET:
                return "Your points have been set to " + points + ".";
            case RESET:
                return "Your points have been reset.";
            case TRANSFER:
                return pointsText + " have been transferred.";
            case REFUND:
                return pointsText + " have been refunded.";
            default:
                return "Your points have been updated.";
        }
    }
    
    /**
     * Set effective date to now if not specified
     */
    public void setDefaultEffectiveDate() {
        if (effectiveDate == null) {
            effectiveDate = LocalDateTime.now();
        }
    }
}
