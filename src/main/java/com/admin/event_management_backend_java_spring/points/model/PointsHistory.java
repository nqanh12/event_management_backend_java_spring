package com.admin.event_management_backend_java_spring.points.model;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "points_history")
public class PointsHistory {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String userEmail;
    private String fullName;

    // Loại điểm thay đổi
    private PointsType pointsType;
    
    // Tiêu chí đánh giá (TC1-TC5)
    private TrainingCriteria trainingCriteria;

    // Kỳ học (chỉ áp dụng cho Training Points)
    private Semester semester;

    // Điểm cũ và mới
    private Double oldPoints;
    private Double newPoints;
    private Double pointsChange;
    
    // Loại vi phạm (nếu là trừ điểm)
    private ViolationType violationType;
    private String violationDetails;

    // Thông tin thay đổi
    private String reason;
    private String description;
    private String changedBy; // Admin/User thực hiện thay đổi

    @Indexed
    private LocalDateTime changedAt;

    // Thông tin sự kiện (nếu có)
    private String eventId;
    private String eventName;

    public enum PointsType {
        TRAINING_POINTS("Điểm rèn luyện"),
        SOCIAL_POINTS("Điểm hoạt động xã hội");

        private final String displayName;

        PointsType(String displayName) {
            this.displayName = displayName;
        }

    }

}
