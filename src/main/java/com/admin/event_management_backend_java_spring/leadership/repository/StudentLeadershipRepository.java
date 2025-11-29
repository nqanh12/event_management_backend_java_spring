package com.admin.event_management_backend_java_spring.leadership.repository;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.leadership.model.StudentLeadership;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StudentLeadershipRepository extends MongoRepository<StudentLeadership, String> {
    List<StudentLeadership> findByStudentIdAndIsDeletedFalse(String studentId);
    List<StudentLeadership> findByStudentIdAndSemesterAndIsDeletedFalse(String studentId, Semester semester);
    List<StudentLeadership> findByIsActiveTrueAndIsDeletedFalse();
    List<StudentLeadership> findByIsVerifiedTrueAndIsDeletedFalse();
    
    @Query("{'student.$id': ?0, 'isDeleted': false}")
    List<StudentLeadership> findByStudentId(String studentId);
    
    @Query("{'student.$id': ?0, 'semester': ?1, 'isDeleted': false}")
    List<StudentLeadership> findByStudentIdAndSemester(String studentId, Semester semester);
    
    @Query("{'student.$id': ?0, 'isActive': true, 'isDeleted': false}")
    List<StudentLeadership> findActiveByStudentId(String studentId);
}

