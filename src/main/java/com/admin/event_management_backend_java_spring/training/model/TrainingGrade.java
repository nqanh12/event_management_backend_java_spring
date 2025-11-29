package com.admin.event_management_backend_java_spring.training.model;

/**
 * Enum định nghĩa xếp loại điểm rèn luyện
 */
public enum TrainingGrade {
    EXCELLENT("Xuất sắc", 90, 100),
    GOOD("Tốt", 80, 89),
    FAIR("Khá", 65, 79),
    AVERAGE("Trung bình", 50, 64),
    WEAK("Yếu", 35, 49),
    POOR("Kém", 0, 34);
    
    private final String displayName;
    private final int minPoints;
    private final int maxPoints;
    
    TrainingGrade(String displayName, int minPoints, int maxPoints) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMinPoints() {
        return minPoints;
    }
    
    public int getMaxPoints() {
        return maxPoints;
    }
    
    /**
     * Xác định xếp loại dựa trên tổng điểm
     */
    public static TrainingGrade fromPoints(double totalPoints) {
        for (TrainingGrade grade : values()) {
            if (totalPoints >= grade.minPoints && totalPoints <= grade.maxPoints) {
                return grade;
            }
        }
        return POOR;
    }
    
    /**
     * Kiểm tra xem điểm có bị giới hạn bởi kỷ luật không
     * - Bị kỷ luật mức khiển trách → tối đa chỉ được xếp Yếu
     * - Bị kỷ luật mức cảnh cáo trở lên → xếp loại Kém
     */
    public static TrainingGrade applyDisciplineRestriction(TrainingGrade calculatedGrade, boolean hasReprimand, boolean hasWarningOrHigher) {
        if (hasWarningOrHigher) {
            return POOR;
        }
        if (hasReprimand && calculatedGrade.ordinal() < WEAK.ordinal()) {
            return WEAK;
        }
        return calculatedGrade;
    }
}

