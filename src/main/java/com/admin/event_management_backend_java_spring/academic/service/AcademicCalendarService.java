package com.admin.event_management_backend_java_spring.academic.service;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.user.model.User;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class AcademicCalendarService {
    
    /**
     * Tính học kỳ hiện tại dựa trên niên khóa học của sinh viên
     * Theo hệ thống giáo dục Việt Nam:
     * - Học kỳ 1: Tháng 9 - Tháng 12
     * - Học kỳ 2: Tháng 1 - Tháng 5
     * - Học kỳ 3 (hè): Tháng 6 - Tháng 8
     */
    public Semester calculateCurrentSemester(User user) {
        if (user.getAcademicYear() == null || user.getCurrentYear() == null) {
            // Fallback: sử dụng kỳ 1 nếu không có thông tin
            return Semester.SEMESTER_1;
        }
        
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        
        // Phân tích niên khóa học (ví dụ: "2021-2025")
        String[] academicYears = user.getAcademicYear().split("-");
        if (academicYears.length != 2) {
            return Semester.SEMESTER_1;
        }
        
        // Tính năm học hiện tại (1, 2, 3, 4)
        int studentYear = user.getCurrentYear();
        if (studentYear < 1 || studentYear > 4) {
            return Semester.SEMESTER_1;
        }
        
        // Tính học kỳ dựa trên tháng hiện tại
        Semester semester;
        if (currentMonth >= 9 && currentMonth <= 12) {
            // Học kỳ 1
            semester = getSemesterByYear(studentYear, 1);
        } else if (currentMonth >= 1 && currentMonth <= 5) {
            // Học kỳ 2
            semester = getSemesterByYear(studentYear, 2);
        } else {
            // Học kỳ hè (tháng 6-8) - thường ít sự kiện
            semester = getSemesterByYear(studentYear, 1); // Fallback về kỳ 1
        }
        
        return semester;
    }
    
    /**
     * Lấy học kỳ dựa trên năm học và kỳ trong năm
     */
    private Semester getSemesterByYear(int studentYear, int semesterInYear) {
        // Công thức: (năm học - 1) * 2 + kỳ trong năm
        int semesterNumber = (studentYear - 1) * 2 + semesterInYear;
        
        switch (semesterNumber) {
            case 1: return Semester.SEMESTER_1;
            case 2: return Semester.SEMESTER_2;
            case 3: return Semester.SEMESTER_3;
            case 4: return Semester.SEMESTER_4;
            case 5: return Semester.SEMESTER_5;
            case 6: return Semester.SEMESTER_6;
            case 7: return Semester.SEMESTER_7;
            case 8: return Semester.SEMESTER_8;
            default: return Semester.SEMESTER_1;
        }
    }
    
    /**
     * Tính năm học hiện tại của sinh viên dựa trên ngày nhập học
     */
    public int calculateCurrentStudentYear(User user) {
        if (user.getEnrollmentDate() == null) {
            return 1; // Mặc định năm 1
        }
        
        LocalDate enrollmentDate = user.getEnrollmentDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        
        // Tính số năm đã trôi qua kể từ khi nhập học
        int yearsPassed = now.getYear() - enrollmentDate.getYear();
        
        // Nếu chưa đến tháng 9 của năm học mới, vẫn tính là năm cũ
        if (now.getMonthValue() < 9) {
            yearsPassed--;
        }
        
        // Giới hạn từ 1-4 năm
        return Math.max(1, Math.min(4, yearsPassed + 1));
    }
    
    /**
     * Cập nhật thông tin năm học hiện tại cho user
     */
    public void updateCurrentYear(User user) {
        int currentYear = calculateCurrentStudentYear(user);
        user.setCurrentYear(currentYear);
    }
    
    /**
     * Kiểm tra xem sinh viên có đang trong thời gian học không
     */
    public boolean isStudentInAcademicPeriod(User user) {
        if (user.getCurrentYear() == null || user.getCurrentYear() > 4) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        
        // Thời gian học chính: tháng 9 - tháng 5
        return (currentMonth >= 9 && currentMonth <= 12) || 
               (currentMonth >= 1 && currentMonth <= 5);
    }
    
    /**
     * Lấy thông tin chi tiết về học kỳ hiện tại
     */
    public String getCurrentSemesterInfo(User user) {
        Semester currentSemester = calculateCurrentSemester(user);
        int studentYear = user.getCurrentYear() != null ? user.getCurrentYear() : 1;
        String academicYear = user.getAcademicYear() != null ? user.getAcademicYear() : "N/A";
        
        return String.format("Năm %d - %s - %s", studentYear, academicYear, currentSemester.getDisplayName());
    }
    
    /**
     * Tính toán học kỳ dựa trên ngày cụ thể
     */
    public Semester calculateSemesterByDate(User user, Date targetDate) {
        if (user.getCurrentYear() == null) {
            return Semester.SEMESTER_1;
        }
        
        LocalDate date = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = date.getMonthValue();
        
        int semesterInYear;
        if (month >= 9 && month <= 12) {
            semesterInYear = 1; // Học kỳ 1
        } else if (month >= 1 && month <= 5) {
            semesterInYear = 2; // Học kỳ 2
        } else {
            semesterInYear = 1; // Học kỳ hè - fallback về kỳ 1
        }
        
        return getSemesterByYear(user.getCurrentYear(), semesterInYear);
    }
}