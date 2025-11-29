package com.admin.event_management_backend_java_spring.leadership.model;

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
 * Entity lưu trữ thông tin chức vụ cán bộ của sinh viên (TC5)
 */
@Document(collection = "student_leaderships")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentLeadership {
    @Id
    private String id;
    
    @DBRef
    @Indexed
    private User student;
    
    private LeadershipType type;        // CLASS, UNION, CLUB, TEAM
    private LeadershipPosition position; // LEADER, VICE_LEADER, MEMBER, etc.
    private String organizationName;    // Tên lớp/CLB/đội
    private String organizationId;     // ID của tổ chức (nếu có)
    
    private Semester semester;          // Kỳ học áp dụng
    private Date startDate;
    private Date endDate;
    private Boolean isActive = true;
    
    private Integer pointsAwarded;     // Điểm TC5 được cộng (tự động tính từ position)
    
    private String description;
    private String verifiedBy;          // Người xác minh
    private Date verifiedAt;
    private Boolean isVerified = false;
    
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;
    
    /**
     * Tính điểm TC5 dựa trên chức vụ
     */
    public void calculatePoints() {
        if (position != null) {
            this.pointsAwarded = position.getPoints();
        }
    }
}

