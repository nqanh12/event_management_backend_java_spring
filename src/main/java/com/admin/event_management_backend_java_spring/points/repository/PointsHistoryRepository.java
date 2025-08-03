package com.admin.event_management_backend_java_spring.points.repository;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.points.model.PointsHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsHistoryRepository extends MongoRepository<PointsHistory, String> {

    // Tìm lịch sử điểm của user
    List<PointsHistory> findByUserIdOrderByChangedAtDesc(String userId);

    // Tìm lịch sử điểm của user với phân trang
    Page<PointsHistory> findByUserId(String userId, Pageable pageable);

    // Tìm lịch sử điểm theo loại
    List<PointsHistory> findByUserIdAndPointsTypeOrderByChangedAtDesc(String userId, PointsHistory.PointsType pointsType);

    // Tìm lịch sử điểm theo kỳ học
    List<PointsHistory> findByUserIdAndSemesterOrderByChangedAtDesc(String userId, Semester semester);

    // Tìm lịch sử điểm trong khoảng thời gian
    @Query("{'userId': ?0, 'changedAt': {$gte: ?1, $lte: ?2}}")
    List<PointsHistory> findByUserIdAndChangedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm lịch sử điểm theo sự kiện
    List<PointsHistory> findByEventIdOrderByChangedAtDesc(String eventId);

    // Thống kê điểm theo loại
    @Query("{'userId': ?0, 'pointsType': ?1}")
    List<PointsHistory> findByUserIdAndPointsType(String userId, PointsHistory.PointsType pointsType);

    // Tìm lịch sử điểm được thay đổi bởi admin
    List<PointsHistory> findByChangedByOrderByChangedAtDesc(String changedBy);

    // Tìm lịch sử điểm theo lý do
    List<PointsHistory> findByReasonContainingIgnoreCase(String reason);

    // Thống kê tổng điểm thay đổi theo loại
    @Query(value = "{'userId': ?0, 'pointsType': ?1}", fields = "{'pointsChange': 1}")
    List<PointsHistory> findPointsChangeByUserIdAndPointsType(String userId, PointsHistory.PointsType pointsType);
}
