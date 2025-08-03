package com.admin.event_management_backend_java_spring.school.repository;

import com.admin.event_management_backend_java_spring.school.model.School;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends MongoRepository<School, String> {

    Optional<School> findByCode(String code);

    Optional<School> findByName(String name);

    List<School> findByStatus(School.SchoolStatus status);

    @Query("{'subscriptionEndDate': {$lt: ?0}}")
    List<School> findExpiredSubscriptions(java.util.Date currentDate);

    @Query("{'status': 'ACTIVE', 'subscriptionEndDate': {$gt: ?0}}")
    List<School> findActiveSchools(java.util.Date currentDate);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
