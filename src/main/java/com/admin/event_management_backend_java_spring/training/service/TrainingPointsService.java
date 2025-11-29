package com.admin.event_management_backend_java_spring.training.service;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.model.EventLevel;
import com.admin.event_management_backend_java_spring.leadership.model.StudentLeadership;
import com.admin.event_management_backend_java_spring.leadership.repository.StudentLeadershipRepository;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import com.admin.event_management_backend_java_spring.points.model.ViolationType;
import com.admin.event_management_backend_java_spring.points.repository.PointsHistoryRepository;
import com.admin.event_management_backend_java_spring.research.model.ResearchAchievement;
import com.admin.event_management_backend_java_spring.research.repository.ResearchAchievementRepository;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import com.admin.event_management_backend_java_spring.training.model.TrainingPointsByCriteria;
import com.admin.event_management_backend_java_spring.training.model.TrainingPointsEvaluation;
import com.admin.event_management_backend_java_spring.training.repository.TrainingPointsEvaluationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xử lý điểm rèn luyện theo 5 tiêu chí (TC1-TC5)
 */
@Slf4j
@Service
public class TrainingPointsService {
    
    @Autowired
    private TrainingPointsEvaluationRepository evaluationRepository;
    
    @Autowired
    private PointsHistoryRepository pointsHistoryRepository;
    
    @Autowired
    private StudentLeadershipRepository leadershipRepository;
    
    @Autowired
    private ResearchAchievementRepository researchRepository;
    
    /**
     * Tính điểm mặc định cho một học kỳ
     */
    public TrainingPointsByCriteria getDefaultPoints() {
        TrainingPointsByCriteria points = new TrainingPointsByCriteria();
        points.setTc1Points(TrainingCriteria.TC1_LEARNING.getDefaultPoints());
        points.setTc2Points(TrainingCriteria.TC2_REGULATION.getDefaultPoints());
        points.setTc3Points(TrainingCriteria.TC3_ACTIVITIES.getDefaultPoints());
        points.setTc4Points(TrainingCriteria.TC4_COMMUNITY.getDefaultPoints());
        points.setTc5Points(TrainingCriteria.TC5_LEADERSHIP.getDefaultPoints());
        return points;
    }
    
    /**
     * Tính điểm cho một học kỳ dựa trên các hoạt động
     */
    @Transactional
    public TrainingPointsEvaluation calculatePointsForSemester(User user, Semester semester) {
        // Lấy hoặc tạo đánh giá cho học kỳ
        TrainingPointsEvaluation evaluation = evaluationRepository
            .findByStudentIdAndSemesterAndIsDeletedFalse(user.getId(), semester)
            .orElse(new TrainingPointsEvaluation());
        
        evaluation.setStudent(user);
        evaluation.setSemester(semester);
        
        // Bắt đầu với điểm mặc định
        TrainingPointsByCriteria points = getDefaultPoints();
        
        // TC1: Tính điểm học tập (rớt môn, nghiên cứu khoa học)
        calculateTC1Points(user, semester, points);
        
        // TC2: Tính điểm nội quy (vi phạm)
        calculateTC2Points(user, semester, points);
        
        // TC3: Tính điểm hoạt động (từ sự kiện)
        calculateTC3Points(user, semester, points);
        
        // TC4: Tính điểm cộng đồng (từ sự kiện)
        calculateTC4Points(user, semester, points);
        
        // TC5: Tính điểm cán bộ
        calculateTC5Points(user, semester, points);
        
        // Cập nhật vào evaluation
        evaluation.setTc1Points(points.getTc1Points());
        evaluation.setTc2Points(points.getTc2Points());
        evaluation.setTc3Points(points.getTc3Points());
        evaluation.setTc4Points(points.getTc4Points());
        evaluation.setTc5Points(points.getTc5Points());
        
        // Tính tổng và xếp loại
        evaluation.calculateTotalPoints();
        evaluation.determineGrade();
        
        return evaluation;
    }
    
    /**
     * Tính điểm TC1: Học tập
     */
    private void calculateTC1Points(User user, Semester semester, TrainingPointsByCriteria points) {
        // Lấy các vi phạm TC1 từ lịch sử điểm
        List<PointsHistory> tc1Histories = pointsHistoryRepository.findByUserIdAndSemesterOrderByChangedAtDesc(
            user.getId(), semester
        );
        
        for (PointsHistory history : tc1Histories) {
            if (history.getTrainingCriteria() == TrainingCriteria.TC1_LEARNING && 
                history.getViolationType() != null) {
                // Trừ điểm theo vi phạm
                points.addPointsByCriteria(TrainingCriteria.TC1_LEARNING, 
                    (double) history.getViolationType().getPointsDeduction());
            } else if (history.getTrainingCriteria() == TrainingCriteria.TC1_LEARNING && 
                      history.getPointsChange() != null && history.getPointsChange() > 0) {
                // Cộng điểm (nghiên cứu khoa học)
                points.addPointsByCriteria(TrainingCriteria.TC1_LEARNING, history.getPointsChange());
            }
        }
        
        // Lấy thành tích nghiên cứu khoa học
        List<ResearchAchievement> researchList = researchRepository
            .findByStudentIdAndSemesterAndIsDeletedFalse(user.getId(), semester);
        
        for (ResearchAchievement research : researchList) {
            if (research.getIsVerified() && research.getPointsAwarded() != null) {
                points.addPointsByCriteria(TrainingCriteria.TC1_LEARNING, 
                    (double) research.getPointsAwarded());
            }
        }
        
        // Giới hạn tối đa TC1 là 20 điểm (nhưng có thể vượt nếu có nghiên cứu khoa học)
        // Theo quy trình: "Phần điểm vượt quá 20 điểm vẫn được cộng vào điểm tổng nhưng TC1 chỉ ghi tối đa 20"
        // Vì vậy không giới hạn ở đây, chỉ giới hạn khi hiển thị
    }
    
    /**
     * Tính điểm TC2: Nội quy
     */
    private void calculateTC2Points(User user, Semester semester, TrainingPointsByCriteria points) {
        // Lấy các vi phạm TC2 từ lịch sử điểm
        List<PointsHistory> tc2Histories = pointsHistoryRepository.findByUserIdAndSemesterOrderByChangedAtDesc(
            user.getId(), semester
        );
        
        for (PointsHistory history : tc2Histories) {
            if (history.getTrainingCriteria() == TrainingCriteria.TC2_REGULATION && 
                history.getViolationType() != null) {
                // Trừ điểm theo vi phạm
                points.addPointsByCriteria(TrainingCriteria.TC2_REGULATION, 
                    (double) history.getViolationType().getPointsDeduction());
            }
        }
        
        // Đảm bảo không âm
        if (points.getTc2Points() < 0) {
            points.setTc2Points(0.0);
        }
    }
    
    /**
     * Tính điểm TC3: Hoạt động chính trị - xã hội - văn hóa - thể thao
     * (Tính từ sự kiện có trainingCriteria = TC3_ACTIVITIES)
     */
    private void calculateTC3Points(User user, Semester semester, TrainingPointsByCriteria points) {
        // Lấy các điểm từ sự kiện TC3
        List<PointsHistory> tc3Histories = pointsHistoryRepository.findByUserIdAndSemesterOrderByChangedAtDesc(
            user.getId(), semester
        );
        
        for (PointsHistory history : tc3Histories) {
            if (history.getTrainingCriteria() == TrainingCriteria.TC3_ACTIVITIES && 
                history.getPointsChange() != null && history.getPointsChange() > 0) {
                points.addPointsByCriteria(TrainingCriteria.TC3_ACTIVITIES, history.getPointsChange());
            } else if (history.getTrainingCriteria() == TrainingCriteria.TC3_ACTIVITIES && 
                      history.getViolationType() != null) {
                // Trừ điểm nếu đăng ký nhưng không tham gia
                points.addPointsByCriteria(TrainingCriteria.TC3_ACTIVITIES, 
                    (double) history.getViolationType().getPointsDeduction());
            }
        }
        
        // Đảm bảo không âm
        if (points.getTc3Points() < 0) {
            points.setTc3Points(0.0);
        }
    }
    
    /**
     * Tính điểm TC4: Công dân và cộng đồng
     * (Tính từ sự kiện có trainingCriteria = TC4_COMMUNITY)
     */
    private void calculateTC4Points(User user, Semester semester, TrainingPointsByCriteria points) {
        // Lấy các điểm từ sự kiện TC4
        List<PointsHistory> tc4Histories = pointsHistoryRepository.findByUserIdAndSemesterOrderByChangedAtDesc(
            user.getId(), semester
        );
        
        for (PointsHistory history : tc4Histories) {
            if (history.getTrainingCriteria() == TrainingCriteria.TC4_COMMUNITY && 
                history.getPointsChange() != null) {
                if (history.getPointsChange() > 0) {
                    // Cộng điểm từ hoạt động
                    points.addPointsByCriteria(TrainingCriteria.TC4_COMMUNITY, history.getPointsChange());
                } else if (history.getViolationType() != null) {
                    // Trừ điểm vi phạm
                    points.addPointsByCriteria(TrainingCriteria.TC4_COMMUNITY, 
                        (double) history.getViolationType().getPointsDeduction());
                }
            }
        }
        
        // Đảm bảo không âm
        if (points.getTc4Points() < 0) {
            points.setTc4Points(0.0);
        }
    }
    
    /**
     * Tính điểm TC5: Cán bộ
     */
    private void calculateTC5Points(User user, Semester semester, TrainingPointsByCriteria points) {
        // Lấy các chức vụ cán bộ trong học kỳ
        List<StudentLeadership> leaderships = leadershipRepository
            .findByStudentIdAndSemesterAndIsDeletedFalse(user.getId(), semester);
        
        int maxPoints = 0;
        for (StudentLeadership leadership : leaderships) {
            if (leadership.getIsActive() && leadership.getIsVerified() && 
                leadership.getPointsAwarded() != null) {
                // Lấy điểm cao nhất (theo quy trình, chỉ cộng một chức vụ)
                maxPoints = Math.max(maxPoints, leadership.getPointsAwarded());
            }
        }
        
        points.setTc5Points((double) maxPoints);
    }
    
    /**
     * Cộng điểm từ sự kiện
     */
    @Transactional
    public void addPointsFromEvent(User user, Event event, int points, Semester semester) {
        TrainingCriteria criteria = event.getTrainingCriteria();
        if (criteria == null) {
            // Nếu không có tiêu chí, tự động xác định
            if (event.getType() == Event.EventType.TRAINING) {
                // Cần xác định dựa trên loại sự kiện
                criteria = TrainingCriteria.TC3_ACTIVITIES; // Mặc định
            } else {
                criteria = TrainingCriteria.TC4_COMMUNITY; // Mặc định
            }
        }
        
        // Tính điểm dựa trên cấp độ sự kiện
        int finalPoints = calculatePointsByEventLevel(event, points);
        
        // Lấy điểm hiện tại
        TrainingPointsEvaluation evaluation = getOrCreateEvaluation(user, semester);
        TrainingPointsByCriteria currentPoints = new TrainingPointsByCriteria();
        currentPoints.setTc1Points(evaluation.getTc1Points() != null ? evaluation.getTc1Points() : 0.0);
        currentPoints.setTc2Points(evaluation.getTc2Points() != null ? evaluation.getTc2Points() : 0.0);
        currentPoints.setTc3Points(evaluation.getTc3Points() != null ? evaluation.getTc3Points() : 0.0);
        currentPoints.setTc4Points(evaluation.getTc4Points() != null ? evaluation.getTc4Points() : 0.0);
        currentPoints.setTc5Points(evaluation.getTc5Points() != null ? evaluation.getTc5Points() : 0.0);
        
        // Cộng điểm
        currentPoints.addPointsByCriteria(criteria, (double) finalPoints);
        
        // Cập nhật evaluation
        evaluation.setTc1Points(currentPoints.getTc1Points());
        evaluation.setTc2Points(currentPoints.getTc2Points());
        evaluation.setTc3Points(currentPoints.getTc3Points());
        evaluation.setTc4Points(currentPoints.getTc4Points());
        evaluation.setTc5Points(currentPoints.getTc5Points());
        evaluation.calculateTotalPoints();
        evaluation.determineGrade();
        
        evaluationRepository.save(evaluation);
        
        // Lưu lịch sử
        PointsHistory history = new PointsHistory();
        history.setUserId(user.getId());
        history.setUserEmail(user.getEmail());
        history.setFullName(user.getFullName());
        history.setPointsType(PointsHistory.PointsType.TRAINING_POINTS);
        history.setTrainingCriteria(criteria);
        history.setSemester(semester);
        history.setOldPoints(currentPoints.getPointsByCriteria(criteria) - finalPoints);
        history.setNewPoints(currentPoints.getPointsByCriteria(criteria));
        history.setPointsChange((double) finalPoints);
        history.setReason("Cộng điểm từ sự kiện: " + event.getName());
        history.setDescription("Sự kiện: " + event.getName());
        history.setChangedBy("SYSTEM");
        history.setChangedAt(LocalDateTime.now());
        history.setEventId(event.getId());
        history.setEventName(event.getName());
        
        pointsHistoryRepository.save(history);
    }
    
    /**
     * Tính điểm dựa trên cấp độ sự kiện
     */
    private int calculatePointsByEventLevel(Event event, int basePoints) {
        EventLevel level = event.getEventLevel();
        if (level == null) {
            return basePoints;
        }
        
        TrainingCriteria criteria = event.getTrainingCriteria();
        if (criteria == null) {
            return basePoints;
        }
        
        // Theo quy trình:
        // TC3: Cấp trường (+6), cấp khoa (+4), CLB (+2), địa phương (+2)
        // TC4: Cấp trường (+5), cấp khoa (+3), ngoài trường (+3)
        
        if (criteria == TrainingCriteria.TC3_ACTIVITIES) {
            switch (level) {
                case SCHOOL:
                    return 6;
                case DEPARTMENT:
                    return 4;
                case CLUB:
                case LOCAL:
                    return 2;
                default:
                    return basePoints;
            }
        } else if (criteria == TrainingCriteria.TC4_COMMUNITY) {
            switch (level) {
                case SCHOOL:
                    return 5;
                case DEPARTMENT:
                    return 3;
                case EXTERNAL:
                    return 3;
                default:
                    return basePoints;
            }
        }
        
        return basePoints;
    }
    
    /**
     * Trừ điểm vi phạm
     */
    @Transactional
    public void deductPointsForViolation(User user, ViolationType violationType, 
                                         Semester semester, String reason, String description, 
                                         String changedBy) {
        TrainingCriteria criteria = violationType.getCriteria();
        
        // Lấy điểm hiện tại
        TrainingPointsEvaluation evaluation = getOrCreateEvaluation(user, semester);
        TrainingPointsByCriteria currentPoints = new TrainingPointsByCriteria();
        currentPoints.setTc1Points(evaluation.getTc1Points() != null ? evaluation.getTc1Points() : 
            TrainingCriteria.TC1_LEARNING.getDefaultPoints());
        currentPoints.setTc2Points(evaluation.getTc2Points() != null ? evaluation.getTc2Points() : 
            TrainingCriteria.TC2_REGULATION.getDefaultPoints());
        currentPoints.setTc3Points(evaluation.getTc3Points() != null ? evaluation.getTc3Points() : 0.0);
        currentPoints.setTc4Points(evaluation.getTc4Points() != null ? evaluation.getTc4Points() : 
            TrainingCriteria.TC4_COMMUNITY.getDefaultPoints());
        currentPoints.setTc5Points(evaluation.getTc5Points() != null ? evaluation.getTc5Points() : 0.0);
        
        Double oldPoints = currentPoints.getPointsByCriteria(criteria);
        
        // Trừ điểm
        currentPoints.addPointsByCriteria(criteria, (double) violationType.getPointsDeduction());
        
        // Đảm bảo không âm
        if (currentPoints.getPointsByCriteria(criteria) < 0) {
            currentPoints.updatePointsByCriteria(criteria, 0.0);
        }
        
        Double newPoints = currentPoints.getPointsByCriteria(criteria);
        
        // Cập nhật evaluation
        evaluation.setTc1Points(currentPoints.getTc1Points());
        evaluation.setTc2Points(currentPoints.getTc2Points());
        evaluation.setTc3Points(currentPoints.getTc3Points());
        evaluation.setTc4Points(currentPoints.getTc4Points());
        evaluation.setTc5Points(currentPoints.getTc5Points());
        evaluation.calculateTotalPoints();
        evaluation.determineGrade();
        
        evaluationRepository.save(evaluation);
        
        // Lưu lịch sử
        PointsHistory history = new PointsHistory();
        history.setUserId(user.getId());
        history.setUserEmail(user.getEmail());
        history.setFullName(user.getFullName());
        history.setPointsType(PointsHistory.PointsType.TRAINING_POINTS);
        history.setTrainingCriteria(criteria);
        history.setSemester(semester);
        history.setOldPoints(oldPoints);
        history.setNewPoints(newPoints);
        history.setPointsChange(newPoints - oldPoints);
        history.setViolationType(violationType);
        history.setViolationDetails(violationType.getDescription());
        history.setReason(reason != null ? reason : violationType.getDescription());
        history.setDescription(description);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        
        pointsHistoryRepository.save(history);
    }
    
    /**
     * Lấy hoặc tạo đánh giá cho học kỳ
     */
    private TrainingPointsEvaluation getOrCreateEvaluation(User user, Semester semester) {
        return evaluationRepository
            .findByStudentIdAndSemesterAndIsDeletedFalse(user.getId(), semester)
            .orElseGet(() -> {
                TrainingPointsEvaluation newEval = new TrainingPointsEvaluation();
                newEval.setStudent(user);
                newEval.setSemester(semester);
                TrainingPointsByCriteria defaultPoints = getDefaultPoints();
                newEval.setTc1Points(defaultPoints.getTc1Points());
                newEval.setTc2Points(defaultPoints.getTc2Points());
                newEval.setTc3Points(defaultPoints.getTc3Points());
                newEval.setTc4Points(defaultPoints.getTc4Points());
                newEval.setTc5Points(defaultPoints.getTc5Points());
                newEval.calculateTotalPoints();
                newEval.determineGrade();
                return newEval;
            });
    }
    
    /**
     * Lấy đánh giá điểm rèn luyện cho học kỳ
     */
    public TrainingPointsEvaluation getEvaluation(User user, Semester semester) {
        return evaluationRepository
            .findByStudentIdAndSemesterAndIsDeletedFalse(user.getId(), semester)
            .orElse(null);
    }
    
    /**
     * Tính lại điểm cho tất cả học kỳ của user
     */
    @Transactional
    public void recalculateAllSemesters(User user) {
        for (Semester semester : Semester.values()) {
            calculatePointsForSemester(user, semester);
        }
    }
}

