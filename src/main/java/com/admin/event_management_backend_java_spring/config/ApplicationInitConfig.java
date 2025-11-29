package com.admin.event_management_backend_java_spring.config;

import com.admin.event_management_backend_java_spring.academic.model.Semester;
import com.admin.event_management_backend_java_spring.department.model.Department;
import com.admin.event_management_backend_java_spring.department.repository.DepartmentRepository;
import com.admin.event_management_backend_java_spring.event.model.Event;
import com.admin.event_management_backend_java_spring.event.model.EventLevel;
import com.admin.event_management_backend_java_spring.event.repository.EventRepository;
import com.admin.event_management_backend_java_spring.leadership.model.LeadershipPosition;
import com.admin.event_management_backend_java_spring.leadership.model.LeadershipType;
import com.admin.event_management_backend_java_spring.leadership.model.StudentLeadership;
import com.admin.event_management_backend_java_spring.leadership.repository.StudentLeadershipRepository;
import com.admin.event_management_backend_java_spring.registration.model.Registration;
import com.admin.event_management_backend_java_spring.registration.repository.RegistrationRepository;
import com.admin.event_management_backend_java_spring.research.model.ResearchAchievement;
import com.admin.event_management_backend_java_spring.research.model.ResearchLevel;
import com.admin.event_management_backend_java_spring.research.repository.ResearchAchievementRepository;
import com.admin.event_management_backend_java_spring.training.model.TrainingCriteria;
import com.admin.event_management_backend_java_spring.training.model.TrainingPointsEvaluation;
import com.admin.event_management_backend_java_spring.training.repository.TrainingPointsEvaluationRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepo,
            DepartmentRepository departmentRepo,
            EventRepository eventRepo,
            RegistrationRepository registrationRepo,
            StudentLeadershipRepository leadershipRepo,
            ResearchAchievementRepository researchRepo,
            TrainingPointsEvaluationRepository evaluationRepo) {
        return args -> {
            try {
                // Danh sách các khoa cần tạo
                List<String> departmentNames = List.of(
                    "Công nghệ thực phẩm",
                    "Sinh học và môi trường",
                    "Công nghệ hóa học",
                    "Công nghệ thông tin",
                    "Công nghệ điện - điện tử",
                    "May - thiết kế thời trang",
                    "Công nghệ cơ khí",
                    "Tài chính kế toán",
                    "Ngoại ngữ",
                    "Quản trị kinh doanh",
                    "Khoa học ứng dụng",
                    "Lý luận chính trị",
                    "Luật",
                    "Giáo dục thể chất và quốc phòng, an ninh",
                    "Du lịch và ẩm thực",
                    "Thương mại"
                );

                // Tạo tất cả các khoa nếu chưa tồn tại
                for (String deptName : departmentNames) {
                    departmentRepo.findByName(deptName).orElseGet(() -> {
                        log.info("Creating department: {}", deptName);
                        Department dept = new Department();
                        dept.setName(deptName);
                        // Sử dụng cùng giá trị penalty như CNTT
                        dept.setTrainingPointsPenalty(44);
                        dept.setSocialPointsPenalty(15);
                        Department saved = departmentRepo.save(dept);
                        log.info("Department '{}' created with ID: {}", deptName, saved.getId());
                        return saved;
                    });
                }

                log.info("All departments initialized successfully");

                // Lấy khoa CNTT cho demo student
                Department cnttDepartment = departmentRepo.findByName("Công nghệ thông tin")
                    .orElseThrow(() -> new RuntimeException("CNTT department not found"));

                // Kiểm tra và tạo admin user mặc định nếu chưa tồn tại
                if (userRepo.findByEmail("chaybon894@gmail.com").isEmpty()) {
                    log.info("Creating default admin user...");

                    User adminUser = new User();
                    adminUser.setEmail("chaybon894@gmail.com");
                    adminUser.setPassword(passwordEncoder.encode("123456789"));
                    adminUser.setFullName("System Administrator");
                    adminUser.setRole(User.UserRole.ADMIN);
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
                    adminUser.setTwoFactorEnabled(true);

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

                    // Thiết lập points mẫu (giá trị mặc định cho hệ thống 1 trường)
                    Integer defaultTrainingPoints = 4; // Giá trị mặc định
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
                    demoStudent.setTwoFactorEnabled(true);

                    User savedStudent = userRepo.save(demoStudent);
                    log.info("Demo student user created successfully with ID: {} in department: {}",
                            savedStudent.getId(), cnttDepartment.getName());
                } else {
                    log.info("Demo student user already exists");
                }

                // Kiểm tra và tạo FACULTY_ADMIN user nếu cần
                if (userRepo.findByEmail("ngan102116@donga.edu.vn").isEmpty()) {
                    log.info("Creating FACULTY_ADMIN user...");

                    User facultyAdmin = new User();
                    facultyAdmin.setEmail("ngan102116@donga.edu.vn");
                    facultyAdmin.setPassword(passwordEncoder.encode("123456789"));
                    facultyAdmin.setFullName("Faculty Administrator");
                    facultyAdmin.setRole(User.UserRole.FACULTY_ADMIN);
                    facultyAdmin.setDepartment(cnttDepartment); // Gán khoa CNTT

                    // Không cần thiết lập studentId và className cho FACULTY_ADMIN
                    facultyAdmin.setStudentId(null);
                    facultyAdmin.setClassName(null);

                    // Thiết lập thông tin mặc định
                    facultyAdmin.setAcademicYear(null);
                    facultyAdmin.setEnrollmentDate(null);
                    facultyAdmin.setCurrentYear(null);

                    // Thiết lập points mặc định
                    facultyAdmin.setTrainingPoints1(0.0);
                    facultyAdmin.setTrainingPoints2(0.0);
                    facultyAdmin.setTrainingPoints3(0.0);
                    facultyAdmin.setTrainingPoints4(0.0);
                    facultyAdmin.setTrainingPoints5(0.0);
                    facultyAdmin.setTrainingPoints6(0.0);
                    facultyAdmin.setTrainingPoints7(0.0);
                    facultyAdmin.setTrainingPoints8(0.0);
                    facultyAdmin.setSocialPoints(0.0);

                    // Thiết lập thông tin khác
                    facultyAdmin.setLastLogin(null);
                    facultyAdmin.setResetToken(null);
                    facultyAdmin.setResetTokenExpiry(null);
                    facultyAdmin.setTwoFactorEnabled(true); // 2FA bắt buộc cho FACULTY_ADMIN

                    User savedFacultyAdmin = userRepo.save(facultyAdmin);
                    log.info("FACULTY_ADMIN user created successfully with ID: {} in department: {}",
                            savedFacultyAdmin.getId(), cnttDepartment.getName());
                } else {
                    log.info("FACULTY_ADMIN user already exists");
                }

                // Kiểm tra và tạo ORGANIZER user nếu cần
                if (userRepo.findByEmail("davimo5485@moondyal.com").isEmpty()) {
                    log.info("Creating ORGANIZER user...");

                    User organizer = new User();
                    organizer.setEmail("davimo5485@moondyal.com");
                    organizer.setPassword(passwordEncoder.encode("123456789"));
                    organizer.setFullName("Event Organizer");
                    organizer.setRole(User.UserRole.ORGANIZER);
                    organizer.setDepartment(cnttDepartment); // Gán khoa CNTT
                    organizer.setTwoFactorEnabled(true);

                    // Không cần thiết lập studentId và className cho ORGANIZER
                    organizer.setStudentId(null);
                    organizer.setClassName(null);

                    // Thiết lập thông tin mặc định
                    organizer.setAcademicYear(null);
                    organizer.setEnrollmentDate(null);
                    organizer.setCurrentYear(null);

                    // Thiết lập points mặc định
                    organizer.setTrainingPoints1(0.0);
                    organizer.setTrainingPoints2(0.0);
                    organizer.setTrainingPoints3(0.0);
                    organizer.setTrainingPoints4(0.0);
                    organizer.setTrainingPoints5(0.0);
                    organizer.setTrainingPoints6(0.0);
                    organizer.setTrainingPoints7(0.0);
                    organizer.setTrainingPoints8(0.0);
                    organizer.setSocialPoints(0.0);

                    // Thiết lập thông tin khác
                    organizer.setLastLogin(null);
                    organizer.setResetToken(null);
                    organizer.setResetTokenExpiry(null);
                    organizer.setTwoFactorEnabled(false); // 2FA không bắt buộc cho ORGANIZER

                    User savedOrganizer = userRepo.save(organizer);
                    log.info("ORGANIZER user created successfully with ID: {} in department: {}",
                            savedOrganizer.getId(), cnttDepartment.getName());
                } else {
                    log.info("ORGANIZER user already exists");
                }

                // ========== TẠO DỮ LIỆU MẪU CHO DASHBOARD ==========

                // Tạo 15 sinh viên mẫu từ các khoa khác nhau
                List<User> sampleStudents = createSampleStudents(userRepo, departmentRepo, passwordEncoder);
                log.info("Created {} sample students", sampleStudents.size());

                // Tạo 15 sự kiện mẫu
                List<Event> sampleEvents = createSampleEvents(eventRepo, departmentRepo, userRepo);
                log.info("Created {} sample events", sampleEvents.size());

                // Tạo đăng ký sự kiện
                createSampleRegistrations(registrationRepo, sampleStudents, sampleEvents);
                log.info("Created sample registrations");

                // Tạo chức vụ cán bộ
                createSampleLeaderships(leadershipRepo, sampleStudents);
                log.info("Created sample leaderships");

                // Tạo nghiên cứu khoa học
                createSampleResearch(researchRepo, sampleStudents);
                log.info("Created sample research achievements");

                // Tạo đánh giá điểm rèn luyện
                createSampleEvaluations(evaluationRepo, sampleStudents);
                log.info("Created sample training points evaluations");

            } catch (Exception e) {
                log.error("Error during application initialization: {}", e.getMessage(), e);
            }
        };
    }

    // ========== CÁC METHOD TẠO DỮ LIỆU MẪU ==========

    private List<User> createSampleStudents(UserRepository userRepo, DepartmentRepository departmentRepo, PasswordEncoder passwordEncoder) {
        List<User> students = new ArrayList<>();

        // Danh sách tên sinh viên mẫu
        String[] firstNames = {"Nguyễn Văn", "Trần Thị", "Lê Hoàng", "Phạm Minh", "Hoàng Văn",
                               "Vũ Thị", "Đặng Văn", "Bùi Thị", "Đỗ Văn", "Ngô Thị",
                               "Dương Văn", "Phan Thị", "Võ Văn", "Lý Thị", "Đinh Văn"};
        String[] lastNames = {"An", "Bình", "Chi", "Dũng", "Em", "Giang", "Hùng", "Lan", "Mai", "Nam",
                             "Oanh", "Phong", "Quỳnh", "Sơn", "Thảo"};

        List<Department> departments = departmentRepo.findAll();
        if (departments.isEmpty()) {
            log.warn("No departments found, skipping student creation");
            return students;
        }

        Random random = new Random();
        int studentCounter = 2021000001;

        for (int i = 0; i < 15; i++) {
            String email = "student" + (i + 1) + "@iuh.edu.vn";
            if (userRepo.findByEmail(email).isPresent()) {
                continue; // Bỏ qua nếu đã tồn tại
            }

            User student = new User();
            student.setEmail(email);
            student.setPassword(passwordEncoder.encode("123456789"));
            student.setFullName(firstNames[i % firstNames.length] + " " + lastNames[i % lastNames.length]);
            student.setRole(User.UserRole.STUDENT);
            student.setDepartment(departments.get(i % departments.size()));

            // Thông tin sinh viên
            int cohort = 2021 + (i % 4); // Khóa 2021-2024
            student.setStudentId(String.valueOf(studentCounter++));
            student.setClassName("12DH" + departments.get(i % departments.size()).getName().substring(0, 2).toUpperCase() + (10 + i % 5));
            student.setCohort(cohort);
            student.setAcademicYear(student.calculateAcademicYear());
            student.setEnrollmentDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * (2024 - cohort))));
            student.setCurrentYear(student.calculateCurrentYear());

            // Điểm mẫu ngẫu nhiên
            student.setTrainingPoints1(20.0 + random.nextDouble() * 30); // 20-50
            student.setTrainingPoints2(15.0 + random.nextDouble() * 25);
            student.setTrainingPoints3(10.0 + random.nextDouble() * 20);
            student.setTrainingPoints4(0.0);
            student.setTrainingPoints5(0.0);
            student.setTrainingPoints6(0.0);
            student.setTrainingPoints7(0.0);
            student.setTrainingPoints8(0.0);
            student.setSocialPoints(5.0 + random.nextDouble() * 15);

            student.setTwoFactorEnabled(false);
            student.setCreatedAt(new Date());
            student.setCreatedBy("SYSTEM");

            User saved = userRepo.save(student);
            students.add(saved);
        }

        return students;
    }

    private List<Event> createSampleEvents(EventRepository eventRepo, DepartmentRepository departmentRepo, UserRepository userRepo) {
        List<Event> events = new ArrayList<>();

        // Danh sách sự kiện mẫu thực tế của IUH
        String[][] eventData = {
            // TC3 - Hoạt động chính trị - xã hội - văn hóa - thể thao
            {"Tuyên truyền pháp luật về an toàn giao thông", "SOCIAL", "TC3_ACTIVITIES", "SCHOOL", "Hội trường A"},
            {"Cuộc thi hùng biện tiếng Anh cấp trường", "SOCIAL", "TC3_ACTIVITIES", "SCHOOL", "Phòng họp B"},
            {"Giải bóng đá sinh viên IUH 2024", "SOCIAL", "TC3_ACTIVITIES", "SCHOOL", "Sân vận động"},
            {"Liên hoan văn nghệ chào mừng 20/11", "SOCIAL", "TC3_ACTIVITIES", "SCHOOL", "Hội trường lớn"},
            {"Cuộc thi tìm hiểu lịch sử Đảng", "SOCIAL", "TC3_ACTIVITIES", "DEPARTMENT", "Phòng học A101"},
            {"Hội thảo về khởi nghiệp cho sinh viên", "SOCIAL", "TC3_ACTIVITIES", "DEPARTMENT", "Phòng họp C"},
            {"Cuộc thi thiết kế poster môi trường", "SOCIAL", "TC3_ACTIVITIES", "CLUB", "CLB Môi trường"},

            // TC4 - Công dân và cộng đồng
            {"Tình nguyện dọn dẹp bãi biển Vũng Tàu", "SOCIAL", "TC4_COMMUNITY", "SCHOOL", "Bãi biển Vũng Tàu"},
            {"Hiến máu nhân đạo", "SOCIAL", "TC4_COMMUNITY", "SCHOOL", "Sảnh chính"},
            {"Tình nguyện dạy học cho trẻ em vùng cao", "SOCIAL", "TC4_COMMUNITY", "DEPARTMENT", "Tỉnh Lâm Đồng"},
            {"Chương trình từ thiện ủng hộ đồng bào lũ lụt", "SOCIAL", "TC4_COMMUNITY", "SCHOOL", "Sảnh chính"},
            {"Tình nguyện trồng cây xanh", "SOCIAL", "TC4_COMMUNITY", "DEPARTMENT", "Khuôn viên trường"},
            {"Dự án hỗ trợ người khuyết tật", "SOCIAL", "TC4_COMMUNITY", "EXTERNAL", "Trung tâm hỗ trợ"},
            {"Tình nguyện dọn dẹp khu dân cư", "SOCIAL", "TC4_COMMUNITY", "LOCAL", "Phường 12, Quận 10"},
            {"Chương trình tặng quà Tết cho người nghèo", "SOCIAL", "TC4_COMMUNITY", "SCHOOL", "Hội trường A"}
        };

        List<Department> departments = departmentRepo.findAll();
        List<User> allUsers = userRepo.findAll();
        List<User> organizers = allUsers.stream()
            .filter(u -> u.getRole() == User.UserRole.ORGANIZER)
            .toList();
        if (organizers.isEmpty()) {
            organizers = allUsers.stream()
                .filter(u -> u.getRole() == User.UserRole.ADMIN)
                .toList();
        }
        if (organizers.isEmpty() || departments.isEmpty()) {
            log.warn("No organizers or departments found, skipping event creation");
            return events;
        }

        Random random = new Random();
        Date now = new Date();

        for (int i = 0; i < eventData.length; i++) {
            String[] data = eventData[i];

            Event event = new Event();
            event.setName(data[0]);
            event.setType(Event.EventType.valueOf(data[1]));
            event.setTrainingCriteria(TrainingCriteria.valueOf(data[2]));
            event.setEventLevel(EventLevel.valueOf(data[3]));
            event.setLocation(data[4]);
            event.setDepartment(departments.get(i % departments.size()));
            event.setOrganizer(organizers.get(0));
            event.setScope(Event.EventScope.SCHOOL);

            // Thời gian sự kiện (một số đã qua, một số sắp tới)
            long startTime = now.getTime() - TimeUnit.DAYS.toMillis(30 - i * 2);
            long endTime = startTime + TimeUnit.HOURS.toMillis(2 + random.nextInt(4));

            event.setStartTime(new Date(startTime));
            event.setEndTime(new Date(endTime));

            // Trạng thái sự kiện
            if (endTime < now.getTime()) {
                event.setStatus(Event.EventStatus.COMPLETED);
            } else if (startTime < now.getTime()) {
                event.setStatus(Event.EventStatus.ONGOING);
            } else {
                event.setStatus(Event.EventStatus.APPROVED);
            }

            event.setMaxParticipants(50 + random.nextInt(100));
            event.setNote("Sự kiện mẫu cho dashboard");
            event.setAllowCancelRegistration(true);
            event.setAllowAllCohorts(true);
            event.setUseCustomPoints(false);

            if (event.getType() == Event.EventType.TRAINING) {
                event.setTrainingPointsReward(4);
            } else {
                // Tính điểm theo cấp độ
                int points = 4; // default
                if (event.getEventLevel() != null) {
                    switch (event.getEventLevel()) {
                        case SCHOOL:
                            points = event.getTrainingCriteria() == TrainingCriteria.TC3_ACTIVITIES ? 6 : 5;
                            break;
                        case DEPARTMENT:
                            points = event.getTrainingCriteria() == TrainingCriteria.TC3_ACTIVITIES ? 4 : 3;
                            break;
                        case CLUB:
                        case LOCAL:
                            points = 2;
                            break;
                        case EXTERNAL:
                            points = 3;
                            break;
                        default:
                            points = 4;
                    }
                }
                event.setSocialPointsReward(points);
            }

            event.setCreatedAt(new Date());
            event.setCreatedBy("SYSTEM");
            event.setIsDeleted(false);

            Event saved = eventRepo.save(event);
            events.add(saved);
        }

        return events;
    }

    private void createSampleRegistrations(RegistrationRepository registrationRepo, List<User> students, List<Event> events) {
        if (students.isEmpty() || events.isEmpty()) {
            return;
        }

        Random random = new Random();
        int registrationCount = 0;

        for (Event event : events) {
            // Mỗi sự kiện có 5-15 người đăng ký
            int numRegistrations = 5 + random.nextInt(11);
            Collections.shuffle(students);

            for (int i = 0; i < Math.min(numRegistrations, students.size()); i++) {
                User student = students.get(i);

                // Kiểm tra xem đã đăng ký chưa
                boolean alreadyRegistered = registrationRepo.findAll().stream()
                    .anyMatch(r -> r.getEvent().getId().equals(event.getId()) &&
                                 r.getUser().getId().equals(student.getId()) &&
                                 !r.getIsDeleted());

                if (alreadyRegistered) {
                    continue;
                }

                Registration reg = new Registration();
                reg.setEvent(event);
                reg.setUser(student);

                // Trạng thái đăng ký
                if (event.getStatus() == Event.EventStatus.COMPLETED) {
                    reg.setStatus(Registration.RegistrationStatus.ATTENDED);
                    // Check-in và check-out
                    long eventStart = event.getStartTime().getTime();
                    reg.setCheckInTime(new Date(eventStart + TimeUnit.MINUTES.toMillis(5)));
                    reg.setCheckOutTime(new Date(event.getEndTime().getTime() - TimeUnit.MINUTES.toMillis(10)));

                    // Điểm đã cộng
                    if (event.getType() == Event.EventType.SOCIAL && event.getSocialPointsReward() != null) {
                        reg.setPointsAwarded(event.getSocialPointsReward());
                        reg.setPointsProcessingStatus(Registration.PointsProcessingStatus.AUTO_AWARDED);
                    } else if (event.getType() == Event.EventType.TRAINING && event.getTrainingPointsReward() != null) {
                        reg.setPointsAwarded(event.getTrainingPointsReward());
                        reg.setPointsProcessingStatus(Registration.PointsProcessingStatus.AUTO_AWARDED);
                    }
                } else if (event.getStatus() == Event.EventStatus.ONGOING) {
                    reg.setStatus(Registration.RegistrationStatus.REGISTERED);
                    if (random.nextBoolean()) {
                        reg.setCheckInTime(new Date());
                    }
                } else {
                    reg.setStatus(Registration.RegistrationStatus.REGISTERED);
                }

                reg.setCreatedAt(new Date());
                reg.setCreatedBy("SYSTEM");
                reg.setIsDeleted(false);

                registrationRepo.save(reg);
                registrationCount++;
            }
        }

        log.info("Created {} registrations", registrationCount);
    }

    private void createSampleLeaderships(StudentLeadershipRepository leadershipRepo, List<User> students) {
        if (students.isEmpty()) {
            return;
        }

        String[] classNames = {"12DHTH10", "12DHTH11", "12DHTH12", "12DHCN10", "12DHCN11"};
        String[] clubNames = {"CLB Tiếng Anh", "CLB Môi trường", "CLB Tình nguyện", "CLB Văn nghệ", "CLB Thể thao"};

        int leadershipCount = 0;

        // Tạo 10 chức vụ cán bộ
        for (int i = 0; i < Math.min(10, students.size()); i++) {
            User student = students.get(i);

            StudentLeadership leadership = new StudentLeadership();
            leadership.setStudent(student);

            if (i < 5) {
                // Cán bộ lớp
                leadership.setType(LeadershipType.CLASS);
                leadership.setOrganizationName(classNames[i % classNames.length]);
                leadership.setPosition(i == 0 ? LeadershipPosition.LEADER :
                                     i == 1 ? LeadershipPosition.VICE_LEADER :
                                     i == 2 ? LeadershipPosition.EXECUTIVE_MEMBER :
                                     LeadershipPosition.REGULAR_MEMBER);
            } else {
                // Cán bộ CLB
                leadership.setType(LeadershipType.CLUB);
                leadership.setOrganizationName(clubNames[(i - 5) % clubNames.length]);
                leadership.setPosition(i == 5 ? LeadershipPosition.LEADER :
                                     i == 6 ? LeadershipPosition.VICE_LEADER :
                                     LeadershipPosition.REGULAR_MEMBER);
            }

            leadership.setSemester(Semester.SEMESTER_1);
            leadership.setStartDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)));
            leadership.setEndDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90)));
            leadership.setIsActive(true);
            leadership.calculatePoints(); // Tự động tính điểm
            leadership.setIsVerified(true);
            leadership.setVerifiedBy("SYSTEM");
            leadership.setVerifiedAt(new Date());
            leadership.setCreatedAt(new Date());
            leadership.setCreatedBy("SYSTEM");
            leadership.setIsDeleted(false);

            leadershipRepo.save(leadership);
            leadershipCount++;
        }

        log.info("Created {} leaderships", leadershipCount);
    }

    private void createSampleResearch(ResearchAchievementRepository researchRepo, List<User> students) {
        if (students.isEmpty()) {
            return;
        }

        String[] researchTitles = {
            "Nghiên cứu ứng dụng AI trong quản lý chất thải",
            "Phát triển hệ thống IoT cho nông nghiệp thông minh",
            "Nghiên cứu vật liệu mới từ phế phẩm nông nghiệp",
            "Ứng dụng blockchain trong chuỗi cung ứng",
            "Nghiên cứu tối ưu hóa năng lượng tái tạo",
            "Phát triển ứng dụng di động hỗ trợ người khuyết tật",
            "Nghiên cứu xử lý nước thải bằng công nghệ sinh học"
        };

        ResearchLevel[] levels = {ResearchLevel.DEPARTMENT, ResearchLevel.SCHOOL, ResearchLevel.CITY, ResearchLevel.NATIONAL};
        int researchCount = 0;

        // Tạo 8 thành tích nghiên cứu
        for (int i = 0; i < Math.min(8, students.size()); i++) {
            User student = students.get(i);

            ResearchAchievement research = new ResearchAchievement();
            research.setStudent(student);
            research.setResearchTitle(researchTitles[i % researchTitles.length]);
            research.setResearchDescription("Mô tả về nghiên cứu: " + researchTitles[i % researchTitles.length]);
            research.setLevel(levels[i % levels.length]);
            research.setSemester(Semester.SEMESTER_1);
            research.calculatePoints(); // Tự động tính điểm
            research.setAchievementDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60 - i * 5)));
            research.setCertificateNumber("CERT-" + (2024000 + i));
            research.setIsVerified(true);
            research.setVerifiedBy("SYSTEM");
            research.setVerifiedAt(new Date());
            research.setCreatedAt(new Date());
            research.setCreatedBy("SYSTEM");
            research.setIsDeleted(false);

            researchRepo.save(research);
            researchCount++;
        }

        log.info("Created {} research achievements", researchCount);
    }

    private void createSampleEvaluations(TrainingPointsEvaluationRepository evaluationRepo, List<User> students) {
        if (students.isEmpty()) {
            return;
        }

        Random random = new Random();
        int evaluationCount = 0;

        // Tạo đánh giá cho học kỳ 1
        for (User student : students) {
            TrainingPointsEvaluation eval = new TrainingPointsEvaluation();
            eval.setStudent(student);
            eval.setSemester(Semester.SEMESTER_1);

            // Điểm theo 5 tiêu chí
            eval.setTc1Points(20.0 + random.nextDouble() * 10); // 20-30 (có thể vượt do nghiên cứu)
            eval.setTc2Points(25.0 - random.nextDouble() * 5); // 20-25
            eval.setTc3Points(5.0 + random.nextDouble() * 15); // 5-20
            eval.setTc4Points(10.0 + random.nextDouble() * 15); // 10-25
            eval.setTc5Points(random.nextDouble() < 0.3 ? 10.0 : 0.0); // 30% có chức vụ

            eval.calculateTotalPoints();
            eval.determineGrade();
            eval.setStatus(TrainingPointsEvaluation.EvaluationStatus.APPROVED);
            eval.setEvaluatedBy("SYSTEM");
            eval.setEvaluatedAt(new Date());
            eval.setCreatedAt(new Date());
            eval.setCreatedBy("SYSTEM");
            eval.setIsDeleted(false);

            evaluationRepo.save(eval);
            evaluationCount++;
        }

        log.info("Created {} training points evaluations", evaluationCount);
    }
}
