package com.admin.event_management_backend_java_spring.academic.payload.request;

import com.admin.event_management_backend_java_spring.academic.model.AcademicSemester;
import com.admin.event_management_backend_java_spring.academic.model.Semester;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSemesterRequest {
    
    @NotBlank(message = "Mã học kỳ không được để trống")
    private String semesterCode;
    
    @NotBlank(message = "Tên học kỳ không được để trống")
    private String semesterName;
    
    @NotBlank(message = "Năm học không được để trống")
    private String academicYear;
    
    @NotNull(message = "Học kỳ không được để trống")
    private Semester semester;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date endDate;
    
    private Date registrationStartDate;
    private Date registrationEndDate;
    
    @NotNull(message = "Trạng thái không được để trống")
    private AcademicSemester.AcademicSemesterStatus status;
    private String description;
    
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return startDate.before(endDate);
    }
    
    public boolean isValidRegistrationDateRange() {
        if (registrationStartDate == null || registrationEndDate == null) {
            return true; // Registration dates are optional
        }
        return registrationStartDate.before(registrationEndDate);
    }
    
    public boolean isRegistrationWithinSemester() {
        if (registrationStartDate == null || registrationEndDate == null || 
            startDate == null || endDate == null) {
            return true; // Skip validation if dates are null
        }
        return !registrationStartDate.before(startDate) && !registrationEndDate.after(endDate);
    }
}
