package com.admin.event_management_backend_java_spring.training.model;

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
 * Entity lưu trữ đánh giá và xếp loại điểm rèn luyện cho từng học kỳ
 */
@Document(collection = "training_points_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPointsEvaluation {
    @Id
    private String id;
    
    @DBRef
    @Indexed
    private User student;
    
    private Semester semester;
    
    // Điểm theo 5 tiêu chí
    private Double tc1Points;
    private Double tc2Points;
    private Double tc3Points;
    private Double tc4Points;
    private Double tc5Points;
    
    private Double totalPoints;         // Tổng điểm (TC1+TC2+TC3+TC4+TC5)
    private TrainingGrade grade;         // Xếp loại
    
    // Thông tin kỷ luật
    private Boolean hasReprimand = false;        // Có bị khiển trách
    private Boolean hasWarningOrHigher = false; // Có bị cảnh cáo trở lên
    
    private String evaluatedBy;          // Người đánh giá
    private Date evaluatedAt;
    private String notes;               // Ghi chú đánh giá
    
    // Trạng thái đánh giá
    private EvaluationStatus status = EvaluationStatus.DRAFT;
    
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;
    
    /**
     * Tính tổng điểm
     */
    public void calculateTotalPoints() {
        this.totalPoints = (tc1Points != null ? tc1Points : 0.0) +
                          (tc2Points != null ? tc2Points : 0.0) +
                          (tc3Points != null ? tc3Points : 0.0) +
                          (tc4Points != null ? tc4Points : 0.0) +
                          (tc5Points != null ? tc5Points : 0.0);
    }
    
    /**
     * Xác định xếp loại dựa trên tổng điểm và kỷ luật
     */
    public void determineGrade() {
        calculateTotalPoints();
        TrainingGrade calculatedGrade = TrainingGrade.fromPoints(totalPoints);
        this.grade = TrainingGrade.applyDisciplineRestriction(
            calculatedGrade, 
            hasReprimand, 
            hasWarningOrHigher
        );
    }
    
    public enum EvaluationStatus {
        DRAFT("Nháp"),
        PENDING_REVIEW("Chờ xét duyệt"),
        APPROVED("Đã duyệt"),
        REJECTED("Từ chối"),
        FINALIZED("Hoàn thành");
        
        private final String displayName;
        
        EvaluationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

