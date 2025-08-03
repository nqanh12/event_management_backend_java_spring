package com.admin.event_management_backend_java_spring.academic.payload.response;

import com.admin.event_management_backend_java_spring.academic.model.AcademicSemester;
import com.admin.event_management_backend_java_spring.academic.model.Semester;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSemesterResponse {
    
    private String id;
    private String semesterCode;
    private String semesterName;
    private String academicYear;
    private Semester semester;
    
    private Date startDate;
    private Date endDate;
    private Date registrationStartDate;
    private Date registrationEndDate;
    
    private AcademicSemester.AcademicSemesterStatus status;
    private String description;
    
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Additional computed fields
    private boolean isActive;
    private boolean isRegistrationOpen;
    private boolean isInProgress;
    private long daysRemaining;
    private long daysUntilStart;
    
    public static AcademicSemesterResponse fromEntity(AcademicSemester entity) {
        if (entity == null) {
            return null;
        }
        
        AcademicSemesterResponse response = new AcademicSemesterResponse();
        response.setId(entity.getId());
        response.setSemesterCode(entity.getSemesterCode());
        response.setSemesterName(entity.getSemesterName());
        response.setAcademicYear(entity.getAcademicYear());
        response.setSemester(entity.getSemester());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setRegistrationStartDate(entity.getRegistrationStartDate());
        response.setRegistrationEndDate(entity.getRegistrationEndDate());
        response.setStatus(entity.getStatus());
        response.setDescription(entity.getDescription());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedBy(entity.getUpdatedBy());
        
        // Set computed fields
        response.setActive(entity.isActive());
        response.setRegistrationOpen(entity.isRegistrationOpen());
        response.setInProgress(entity.isInProgress());
        
        // Calculate days remaining and days until start
        Date now = new Date();
        if (entity.getEndDate() != null) {
            long diffInMillies = entity.getEndDate().getTime() - now.getTime();
            response.setDaysRemaining(Math.max(0, diffInMillies / (1000 * 60 * 60 * 24)));
        }
        
        if (entity.getStartDate() != null) {
            long diffInMillies = entity.getStartDate().getTime() - now.getTime();
            response.setDaysUntilStart(Math.max(0, diffInMillies / (1000 * 60 * 60 * 24)));
        }
        
        return response;
    }
}
