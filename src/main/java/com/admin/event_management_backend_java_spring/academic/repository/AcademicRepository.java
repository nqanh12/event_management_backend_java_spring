package com.admin.event_management_backend_java_spring.academic.repository;

import com.admin.event_management_backend_java_spring.academic.model.AcademicSemester;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicRepository extends MongoRepository<AcademicSemester, String> {
    
    /**
     * Tìm học kỳ theo mã học kỳ
     */
    Optional<AcademicSemester> findBySemesterCode(String semesterCode);
    
    /**
     * Tìm học kỳ theo năm học
     */
    List<AcademicSemester> findByAcademicYearOrderByStartDateAsc(String academicYear);
    
    /**
     * Tìm học kỳ đang hoạt động
     */
    Optional<AcademicSemester> findByStatus(AcademicSemester.AcademicSemesterStatus status);
    
    /**
     * Tìm học kỳ hiện tại (đang trong thời gian diễn ra)
     */
    @Query("{'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    Optional<AcademicSemester> findCurrentSemester(Date currentDate);
    
    /**
     * Tìm học kỳ theo khoảng thời gian
     */
    @Query("{'startDate': {$lte: ?1}, 'endDate': {$gte: ?0}}")
    List<AcademicSemester> findSemestersInDateRange(Date startDate, Date endDate);
    
    /**
     * Tìm học kỳ theo semester enum
     */
    List<AcademicSemester> findBySemesterOrderByStartDateDesc(com.admin.event_management_backend_java_spring.academic.model.Semester semester);
    
    /**
     * Kiểm tra xem có học kỳ nào đang mở đăng ký không
     */
    @Query("{'registrationStartDate': {$lte: ?0}, 'registrationEndDate': {$gte: ?0}}")
    List<AcademicSemester> findSemestersWithOpenRegistration(Date currentDate);
    
    /**
     * Tìm học kỳ gần nhất theo thời gian bắt đầu
     */
    Optional<AcademicSemester> findTopByOrderByStartDateDesc();
    
    /**
     * Tìm học kỳ theo trạng thái, sắp xếp theo thời gian
     */
    List<AcademicSemester> findByStatusOrderByStartDateDesc(AcademicSemester.AcademicSemesterStatus status);
    
    /**
     * Kiểm tra xem có trùng lặp thời gian học kỳ không
     */
    @Query("{'$or': [" +
           "{'startDate': {$lte: ?1}, 'endDate': {$gte: ?0}}, " +
           "{'startDate': {$lte: ?0}, 'endDate': {$gte: ?1}}" +
           "], '_id': {$ne: ?2}}")
    List<AcademicSemester> findOverlappingSemesters(Date startDate, Date endDate, String excludeId);
    
    /**
     * Đếm số học kỳ theo năm học
     */
    long countByAcademicYear(String academicYear);
    
    /**
     * Tìm tất cả năm học đã có
     */
    @Query(value = "{}", fields = "{'academicYear': 1}")
    List<AcademicSemester> findDistinctAcademicYears();
}
