package com.admin.event_management_backend_java_spring.research.repository;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.research.model.ResearchAchievement;
import com.admin.event_management_backend_java_spring.research.model.ResearchLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ResearchAchievementRepository extends MongoRepository<ResearchAchievement, String> {
    List<ResearchAchievement> findByStudentIdAndIsDeletedFalse(String studentId);
    List<ResearchAchievement> findByStudentIdAndSemesterAndIsDeletedFalse(String studentId, Semester semester);
    List<ResearchAchievement> findByLevelAndIsDeletedFalse(ResearchLevel level);
    List<ResearchAchievement> findByIsVerifiedTrueAndIsDeletedFalse();
    List<ResearchAchievement> findByIsVerifiedFalseAndIsDeletedFalse();
    
    @Query("{'student.$id': ?0, 'isDeleted': false}")
    List<ResearchAchievement> findByStudentId(String studentId);
    
    @Query("{'student.$id': ?0, 'semester': ?1, 'isDeleted': false}")
    List<ResearchAchievement> findByStudentIdAndSemester(String studentId, Semester semester);
}

