package com.admin.event_management_backend_java_spring.config;

import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepo, DepartmentRepository departmentRepo) {
        return args -> {
            try {
                // Tạo khoa Công nghệ thông tin nếu chưa tồn tại
                Department cnttDepartment = departmentRepo.findByName("Công nghệ thông tin")
                    .orElseGet(() -> {
                        log.info("Creating CNTT department...");
                        Department dept = new Department();
                        dept.setName("Công nghệ thông tin");
                        dept.setTrainingPointsPenalty(44);
                        dept.setSocialPointsPenalty(15);
                        return departmentRepo.save(dept);
                    });

                log.info("CNTT department ready with ID: {}", cnttDepartment.getId());

                // Kiểm tra và tạo admin user mặc định nếu chưa tồn tại
                if (userRepo.findByEmail("chaybon894@gmail.com").isEmpty()) {
                    log.info("Creating default admin user...");

                    User adminUser = new User();
                    adminUser.setEmail("chaybon894@gmail.com");
                    adminUser.setPassword(passwordEncoder.encode("123456789"));
                    adminUser.setFullName("System Administrator");
                    adminUser.setRole(User.UserRole.GLOBAL_ADMIN);
                    adminUser.setDepartment(null);

                    // Không cần thiết lập studentId và className cho admin
                    adminUser.setStudentId(null);
                    adminUser.setClassName(null);

                    // Thiết lập thông tin mặc định
                    adminUser.setAcademicYear(null);
                    adminUser.setEnrollmentDate(null);
                    adminUser.setCurrentYear(null);

                    // Thiết lập points mặc định
                    adminUser.setTrainingPoints1(0.0);
                    adminUser.setTrainingPoints2(0.0);
                    adminUser.setTrainingPoints3(0.0);
                    adminUser.setTrainingPoints4(0.0);
                    adminUser.setTrainingPoints5(0.0);
                    adminUser.setTrainingPoints6(0.0);
                    adminUser.setTrainingPoints7(0.0);
                    adminUser.setTrainingPoints8(0.0);
                    adminUser.setSocialPoints(0.0);

                    // Thiết lập thông tin khác
                    adminUser.setLastLogin(null);
                    adminUser.setResetToken(null);
                    adminUser.setResetTokenExpiry(null);

                    User savedUser = userRepo.save(adminUser);
                    log.info("Default admin user created successfully with ID: {}", savedUser.getId());
                } else {
                    log.info("Default admin user already exists");
                }

                // Kiểm tra và tạo demo student nếu cần
                if (userRepo.findByEmail("student@gmail.com").isEmpty()) {
                    log.info("Creating demo student user...");

                    User demoStudent = new User();
                    demoStudent.setEmail("student@gmail.com");
                    demoStudent.setPassword(passwordEncoder.encode("123456789"));
                    demoStudent.setFullName("Demo Student");
                    demoStudent.setRole(User.UserRole.STUDENT);
                    demoStudent.setDepartment(cnttDepartment); // Gán khoa CNTT

                    // Thiết lập thông tin sinh viên
                    demoStudent.setStudentId("2021000001");
                    demoStudent.setClassName("12DHTH10");
                    demoStudent.setCohort(2021); // Khóa 2021
                    demoStudent.setAcademicYear(demoStudent.calculateAcademicYear()); // Tự động tính: 2021-2025
                    demoStudent.setEnrollmentDate(new Date());
                    demoStudent.setCurrentYear(demoStudent.calculateCurrentYear()); // Tự động tính năm hiện tại

                    // Thiết lập points mẫu
                    Integer defaultTrainingPoints = null;
                    if (cnttDepartment != null && cnttDepartment.getSchool() != null) {
                        defaultTrainingPoints = cnttDepartment.getSchool().getDefaultTrainingPoints ();
                    }
                    if (defaultTrainingPoints == null) {
                        defaultTrainingPoints = 0;
                    }
                    demoStudent.setTrainingPoints1(defaultTrainingPoints.doubleValue());
                    demoStudent.setTrainingPoints2(0.0);
                    demoStudent.setTrainingPoints3(0.0);
                    demoStudent.setTrainingPoints4(0.0);
                    demoStudent.setTrainingPoints5(0.0);
                    demoStudent.setTrainingPoints6(0.0);
                    demoStudent.setTrainingPoints7(0.0);
                    demoStudent.setTrainingPoints8(0.0);
                    demoStudent.setSocialPoints(0.0);

                    // Thiết lập thông tin khác
                    demoStudent.setLastLogin(null);
                    demoStudent.setResetToken(null);
                    demoStudent.setResetTokenExpiry(null);

                    User savedStudent = userRepo.save(demoStudent);
                    log.info("Demo student user created successfully with ID: {} in department: {}",
                            savedStudent.getId(), cnttDepartment.getName());
                } else {
                    log.info("Demo student user already exists");
                }

            } catch (Exception e) {
                log.error("Error during application initialization: {}", e.getMessage(), e);
            }
        };
    }
}
