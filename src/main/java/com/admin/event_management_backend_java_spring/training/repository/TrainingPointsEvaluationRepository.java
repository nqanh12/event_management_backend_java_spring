package com.admin.event_management_backend_java_spring.training.repository;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.training.model.TrainingPointsEvaluation;
import com.admin.event_management_backend_java_spring.training.model.TrainingGrade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainingPointsEvaluationRepository extends MongoRepository<TrainingPointsEvaluation, String> {
    Optional<TrainingPointsEvaluation> findByStudentIdAndSemesterAndIsDeletedFalse(String studentId, Semester semester);
    List<TrainingPointsEvaluation> findByStudentIdAndIsDeletedFalse(String studentId);
    List<TrainingPointsEvaluation> findBySemesterAndIsDeletedFalse(Semester semester);
    List<TrainingPointsEvaluation> findByGradeAndIsDeletedFalse(TrainingGrade grade);
    
    @Query("{'student.$id': ?0, 'isDeleted': false}")
    List<TrainingPointsEvaluation> findByStudentId(String studentId);
    
    @Query("{'student.$id': ?0, 'semester': ?1, 'isDeleted': false}")
    Optional<TrainingPointsEvaluation> findByStudentIdAndSemester(String studentId, Semester semester);
    
    @Query("{'semester': ?0, 'status': ?1, 'isDeleted': false}")
    List<TrainingPointsEvaluation> findBySemesterAndStatus(Semester semester, TrainingPointsEvaluation.EvaluationStatus status);
}

