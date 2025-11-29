package com.admin.event_management_backend_java_spring.points.repository;

import com.admin.event_management_backend_java_spring.points.model.PointsEvidence;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PointsEvidenceRepository extends MongoRepository<PointsEvidence, String> {
    List<PointsEvidence> findByStudentIdAndIsDeletedFalse(String studentId);
    List<PointsEvidence> findByStudentIdAndCriteriaAndIsDeletedFalse(String studentId, TrainingCriteria criteria);
    List<PointsEvidence> findByIsVerifiedTrueAndIsDeletedFalse();
    List<PointsEvidence> findByIsVerifiedFalseAndIsDeletedFalse();
    
    @Query("{'student.$id': ?0, 'isDeleted': false}")
    List<PointsEvidence> findByStudentId(String studentId);
    
    @Query("{'student.$id': ?0, 'criteria': ?1, 'isDeleted': false}")
    List<PointsEvidence> findByStudentIdAndCriteria(String studentId, TrainingCriteria criteria);
    
    @Query("{'event.$id': ?0, 'isDeleted': false}")
    List<PointsEvidence> findByEventId(String eventId);
}

