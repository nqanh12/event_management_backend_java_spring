package com.admin.event_management_backend_java_spring.research.model;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * Entity lưu trữ thành tích nghiên cứu khoa học (TC1)
 */
@Document(collection = "research_achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchAchievement {
    @Id
    private String id;
    
    @DBRef
    @Indexed
    private User student;
    
    private String researchTitle;
    private String researchDescription;
    private ResearchLevel level;        // NATIONAL, CITY, SCHOOL, DEPARTMENT
    private Integer pointsAwarded;      // +30, +20, +10, +5 (tự động tính từ level)
    
    private Date achievementDate;
    private String certificateUrl;      // URL giấy chứng nhận
    private String certificateNumber;   // Số giấy chứng nhận
    
    private Semester semester;          // Kỳ học áp dụng
    
    private String verifiedBy;          // Người xác minh
    private Date verifiedAt;
    private Boolean isVerified = false;
    private String verificationNote;
    
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;
    
    /**
     * Tính điểm dựa trên cấp độ
     */
    public void calculatePoints() {
        if (level != null) {
            this.pointsAwarded = level.getPoints();
        }
    }
}

