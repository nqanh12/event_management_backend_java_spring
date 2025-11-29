package com.admin.event_management_backend_java_spring.points.model;

import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.leadership.model.StudentLeadership;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import com.admin.event_management_backend_java_spring.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;
import java.util.List;

/**
 * Entity lưu trữ minh chứng cho các hoạt động điểm rèn luyện
 */
@Document(collection = "points_evidences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsEvidence {
    @Id
    private String id;
    
    @DBRef
    @Indexed
    private User student;
    
    private TrainingCriteria criteria;  // TC1, TC2, TC3, TC4, TC5
    
    private EvidenceType evidenceType;  // CERTIFICATE, PHOTO, DOCUMENT, etc.
    private List<String> fileUrls;      // URLs của các file minh chứng
    private List<String> fileNames;     // Tên các file
    
    private String description;
    private Date evidenceDate;          // Ngày diễn ra hoạt động
    
    @DBRef
    private Event event;                // Nếu liên quan đến sự kiện
    
    @DBRef
    private StudentLeadership leadership; // Nếu liên quan đến chức vụ
    
    private String verifiedBy;          // Người xác minh
    private Date verifiedAt;
    private Boolean isVerified = false;
    private String verificationNote;   // Ghi chú xác minh
    
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;
    
    public enum EvidenceType {
        CERTIFICATE("Giấy chứng nhận"),
        PHOTO("Ảnh"),
        DOCUMENT("Tài liệu"),
        VIDEO("Video"),
        OTHER("Khác");
        
        private final String displayName;
        
        EvidenceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

