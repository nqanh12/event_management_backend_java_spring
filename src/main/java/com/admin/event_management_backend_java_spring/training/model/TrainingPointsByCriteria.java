package com.admin.event_management_backend_java_spring.training.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model lưu điểm theo 5 tiêu chí cho một học kỳ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPointsByCriteria {
    private Double tc1Points = 20.0;  // Điểm mặc định TC1
    private Double tc2Points = 25.0;  // Điểm mặc định TC2
    private Double tc3Points = 0.0;   // TC3 không mặc định
    private Double tc4Points = 25.0;  // Điểm mặc định TC4
    private Double tc5Points = 0.0;   // TC5 không mặc định
    
    /**
     * Tính tổng điểm rèn luyện
     */
    public Double getTotalPoints() {
        return tc1Points + tc2Points + tc3Points + tc4Points + tc5Points;
    }
    
    /**
     * Lấy điểm theo tiêu chí
     */
    public Double getPointsByCriteria(TrainingCriteria criteria) {
        switch (criteria) {
            case TC1_LEARNING:
                return tc1Points;
            case TC2_REGULATION:
                return tc2Points;
            case TC3_ACTIVITIES:
                return tc3Points;
            case TC4_COMMUNITY:
                return tc4Points;
            case TC5_LEADERSHIP:
                return tc5Points;
            default:
                return 0.0;
        }
    }
    
    /**
     * Cập nhật điểm theo tiêu chí
     */
    public void updatePointsByCriteria(TrainingCriteria criteria, Double points) {
        switch (criteria) {
            case TC1_LEARNING:
                this.tc1Points = points;
                break;
            case TC2_REGULATION:
                this.tc2Points = points;
                break;
            case TC3_ACTIVITIES:
                this.tc3Points = points;
                break;
            case TC4_COMMUNITY:
                this.tc4Points = points;
                break;
            case TC5_LEADERSHIP:
                this.tc5Points = points;
                break;
        }
    }
    
    /**
     * Cộng điểm theo tiêu chí
     */
    public void addPointsByCriteria(TrainingCriteria criteria, Double points) {
        Double current = getPointsByCriteria(criteria);
        updatePointsByCriteria(criteria, current + points);
    }
}

