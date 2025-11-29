package com.admin.event_management_backend_java_spring.event.model;

import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.event.model.EventLevel;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import com.admin.event_management_backend_java_spring.user.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    private String id;
    private String name;
    private EventType type;
    private Date startTime;
    private Date endTime;
    private String location;
    @DBRef
    @Indexed
    private Department department;
    @DBRef
    private User organizer;
    private EventStatus status = EventStatus.PENDING;
    @DBRef
    private List<User> approvalChain;
    private String qrCode;
    private Integer maxParticipants;
    private String note;
    private Boolean allowCancelRegistration = true;
    private String updatedBy;
    private Date updatedAt;
    @Indexed
    private Boolean isDeleted = false;
    private Date deletedAt;
    private String createdBy;
    private Date createdAt;


    public enum EventScope {
        SCHOOL, DEPARTMENT
    }
    private EventScope scope;

    // Custom points configuration
    private Boolean useCustomPoints = false;
    private Integer trainingPointsReward;
    private Integer socialPointsReward;

    // Tiêu chí đánh giá điểm rèn luyện (TC1-TC5)
    private TrainingCriteria trainingCriteria; // TC3_ACTIVITIES hoặc TC4_COMMUNITY
    
    // Cấp độ sự kiện
    private EventLevel eventLevel; // SCHOOL, DEPARTMENT, CLUB, LOCAL, EXTERNAL, etc.

    // Khóa học được phép đăng ký
    private List<Integer> allowedCohorts; // Ví dụ: [2021, 2022, 2023] - chỉ cho phép khóa 2021, 2022, 2023 đăng ký
    private Boolean allowAllCohorts = true; // Nếu true thì tất cả khóa học đều được phép đăng ký

    public enum EventType {
        TRAINING, SOCIAL
    }
    public enum EventStatus {
        PENDING, APPROVED, ONGOING, COMPLETED, CANCELLED
    }

    // Helper method để kiểm tra user có được phép đăng ký không
    public boolean isUserAllowedToRegister(User user) {
        if (scope == EventScope.SCHOOL) {
            return user.getRole() == User.UserRole.STUDENT;
        } else if (scope == EventScope.DEPARTMENT) {
            return user.getRole() == User.UserRole.STUDENT && user.getDepartment() != null && department != null && user.getDepartment().getId().equals(department.getId());
        }
        // fallback: giữ logic cũ
        if (allowAllCohorts) {
            return true;
        }
        if (allowedCohorts == null || allowedCohorts.isEmpty()) {
            return true;
        }
        return user.getCohort() != null && allowedCohorts.contains(user.getCohort());
    }
}
