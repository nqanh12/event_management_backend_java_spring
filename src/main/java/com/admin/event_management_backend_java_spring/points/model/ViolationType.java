package com.admin.event_management_backend_java_spring.points.model;

import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;

/**
 * Enum định nghĩa các loại vi phạm và điểm trừ tương ứng
 */
public enum ViolationType {
    // TC1 violations - Ý thức và thái độ học tập
    FAILED_1_SUBJECT(TrainingCriteria.TC1_LEARNING, -1, "Rớt 1 môn"),
    FAILED_2_SUBJECTS(TrainingCriteria.TC1_LEARNING, -3, "Rớt 2 môn"),
    FAILED_3_OR_MORE(TrainingCriteria.TC1_LEARNING, -15, "Rớt ≥3 môn"),
    UNAUTHORIZED_ABSENCE(TrainingCriteria.TC1_LEARNING, -20, "Nghỉ học không phép / cảnh báo học vụ"),
    EXAM_BAN(TrainingCriteria.TC1_LEARNING, -10, "Bị cấm thi"),
    
    // TC2 violations - Chấp hành nội quy, quy định
    NO_CLASS_MEETING(TrainingCriteria.TC2_REGULATION, -5, "Không tham gia SHCD"),
    NO_HEALTH_INSURANCE(TrainingCriteria.TC2_REGULATION, -5, "Không tham gia BHYT"),
    NO_HEALTH_CHECK(TrainingCriteria.TC2_REGULATION, -5, "Không khám sức khỏe theo Thông báo"),
    INAPPROPRIATE_DRESS(TrainingCriteria.TC2_REGULATION, -5, "Trang phục không nghiêm túc"),
    LATE_TUITION(TrainingCriteria.TC2_REGULATION, -5, "Đóng học phí trễ hạn"),
    NO_CLEANLINESS(TrainingCriteria.TC2_REGULATION, -5, "Không giữ vệ sinh"),
    NO_STUDENT_CARD(TrainingCriteria.TC2_REGULATION, -5, "Không đeo thẻ sinh viên"),
    SMOKING_IN_PROHIBITED(TrainingCriteria.TC2_REGULATION, -5, "Hút thuốc nơi cấm"),
    NO_PERSONAL_INFO_UPDATE(TrainingCriteria.TC2_REGULATION, -10, "Không cập nhật thông tin cá nhân"),
    NO_SURVEY_PARTICIPATION(TrainingCriteria.TC2_REGULATION, -5, "Không tham gia khảo sát"),
    INCORRECT_STUDENT_RECORD(TrainingCriteria.TC2_REGULATION, -5, "Hồ sơ sinh viên không đúng"),
    REPRIMAND_DISCIPLINE(TrainingCriteria.TC2_REGULATION, -25, "Vi phạm kỷ luật khiển trách"),
    NO_SCHOOL_REGULATION(TrainingCriteria.TC2_REGULATION, -20, "Không thực hiện quy định nhà trường"),
    OTHER_VIOLATIONS(TrainingCriteria.TC2_REGULATION, -10, "Các vi phạm khác"),
    
    // TC3 violations
    REGISTERED_BUT_NOT_ATTENDED(TrainingCriteria.TC3_ACTIVITIES, -5, "Đăng ký hoạt động nhưng không tham gia"),
    
    // TC4 violations - Ý thức công dân và quan hệ cộng đồng
    ANTI_SOCIAL_NORM_STATEMENT(TrainingCriteria.TC4_COMMUNITY, -25, "Phát ngôn chống chuẩn mực xã hội"),
    DISTURBANCE_SECURITY(TrainingCriteria.TC4_COMMUNITY, -25, "Gây rối, mất an ninh"),
    RESIDENCE_VIOLATION(TrainingCriteria.TC4_COMMUNITY, -15, "Vi phạm cư trú"),
    TRAFFIC_VIOLATION(TrainingCriteria.TC4_COMMUNITY, -15, "Vi phạm luật giao thông");
    
    private final TrainingCriteria criteria;
    private final int pointsDeduction;
    private final String description;
    
    ViolationType(TrainingCriteria criteria, int pointsDeduction, String description) {
        this.criteria = criteria;
        this.pointsDeduction = pointsDeduction;
        this.description = description;
    }
    
    public TrainingCriteria getCriteria() {
        return criteria;
    }
    
    public int getPointsDeduction() {
        return pointsDeduction;
    }
    
    public String getDescription() {
        return description;
    }
}

