package com.admin.event_management_backend_java_spring.department.repository;

import com.admin.event_management_backend_java_spring.department.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByName(String name);
} 