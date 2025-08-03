package com.admin.event_management_backend_java_spring.academic.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "academic_semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSemester {
    @Id
    private String id;
    
    private String semesterCode;        // VD: "2024-2025-HK1" 
    private String semesterName;        // VD: "Học kỳ 1 năm học 2024-2025"
    private String academicYear;        // VD: "2024-2025"
    private Semester semester;          // SEMESTER_1, SEMESTER_2, etc.
    
    private Date startDate;             // Ngày bắt đầu học kỳ
    private Date endDate;               // Ngày kết thúc học kỳ
    private Date registrationStartDate; // Ngày bắt đầu đăng ký môn học
    private Date registrationEndDate;   // Ngày kết thúc đăng ký môn học
    
    private AcademicSemesterStatus status;
    private String description;
    
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    
    public enum AcademicSemesterStatus {
        UPCOMING,    
        ACTIVE,      
        COMPLETED,   
        ARCHIVED     
    }
    
    public boolean isActive() {
        return status == AcademicSemesterStatus.ACTIVE;
    }
    
    public boolean isRegistrationOpen() {
        Date now = new Date();
        return registrationStartDate != null && registrationEndDate != null &&
               now.after(registrationStartDate) && now.before(registrationEndDate);
    }
    
    public boolean isInProgress() {
        Date now = new Date();
        return startDate != null && endDate != null &&
               now.after(startDate) && now.before(endDate);
    }
    
}
