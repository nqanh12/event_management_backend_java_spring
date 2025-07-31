# Event Management Backend Java Spring

## 📝 Giới thiệu

**Event Management Backend Java Spring** là hệ thống quản lý sự kiện, điểm rèn luyện, hoạt động sinh viên, quản trị trường/khoa, phân quyền đa vai trò, bảo mật JWT, xác thực 2FA, gửi email tự động, báo cáo, dashboard, và nhiều tính năng khác. Dự án sử dụng Spring Boot 3, MongoDB, JWT, Docker, và tích hợp email, phân tích, audit log, notification, v.v.

---

## 🚀 Tính năng chính
- Quản lý sự kiện, đăng ký, check-in/check-out, điểm danh bằng QR code
- Quản lý user, phân quyền theo vai trò (Admin, Manager, Organizer, Student, Guest, Scanner)
- Quản lý điểm rèn luyện, điểm xã hội, lịch sử điểm, dashboard thống kê
- Báo cáo, xuất file, phân tích dữ liệu, audit log
- Xác thực JWT, refresh token, 2FA qua email OTP, bảo mật nâng cao
- Gửi email tự động: thông báo sự kiện, điểm, bảo mật, đăng ký thành công, v.v.
- Gửi notification qua API, dashboard tổng hợp
- Quản lý trường, khoa, phòng ban, phân quyền theo cấp
- Hệ thống API RESTful, tài liệu Swagger/OpenAPI
- Hỗ trợ Docker Compose, dễ dàng triển khai

---

## 🏗️ Kiến trúc & Công nghệ
- **Spring Boot 3.x** (REST API, Security, Async, Scheduling)
- **MongoDB** (NoSQL, cloud/local)
- **JWT** (Authentication, Refresh Token, Cookie)
- **Spring Security** (Role-based, @PreAuthorize)
- **Lombok** (Boilerplate code)
- **Swagger/OpenAPI** (API docs)
- **Docker, Docker Compose** (Triển khai nhanh)
- **Mail Service** (Gmail SMTP, gửi OTP, thông báo)
- **Bucket4j** (Rate Limiting)
- **Apache POI** (Xuất file Excel)
- **Junit, Spring Test** (Unit test)

---

## 🔐 Phân quyền & Vai trò
| Vai trò             | Quyền hạn chính |
|---------------------|-----------------|
| GLOBAL_ADMIN        | Quản trị toàn hệ thống, tạo user, phân quyền, quản lý trường/khoa, báo cáo tổng hợp |
| SCHOOL_MANAGER      | Quản lý cấp trường: user, điểm, sự kiện, báo cáo trong trường |
| FACULTY_ADMIN       | Quản lý cấp khoa: user, điểm, sự kiện, báo cáo trong khoa |
| ORGANIZER           | Tổ chức sự kiện: tạo, cập nhật, quản lý sự kiện mình phụ trách |
| STUDENT             | Sinh viên: đăng ký sự kiện, xem điểm, lịch sử tham gia |
| GUEST               | Khách: đăng ký sự kiện với quyền hạn hạn chế |
| FACULTY_SCANNER     | Quét check-in/check-out sự kiện cho khoa |
| SCHOOL_SCANNER      | Quét check-in/check-out sự kiện cho trường |

> **Xem chi tiết phân quyền trong các annotation @PreAuthorize ở các controller.**

---

## ⚙️ Cài đặt & Chạy thử

### 1. Yêu cầu
- Java 17+
- Maven 3.8+
- Docker (khuyến nghị)

### 2. Clone & Build
```bash
git clone https://github.com/your-org/event_management_backend_java_spring.git
cd event_management_backend_java_spring
mvn clean install
```

### 3. Chạy bằng Docker Compose
```bash
docker-compose up --build
```
- Truy cập API tại: `http://localhost:8080`
- MongoDB chạy tại: `mongodb://localhost:27017/event_management`

### 4. Chạy local (không Docker)
- Cài MongoDB local hoặc dùng cloud URI trong `src/main/resources/application.yaml`
- Chỉnh sửa thông tin SMTP/email nếu cần
- Chạy:
```bash
mvn spring-boot:run
```

---

## 🛠️ Cấu hình môi trường (`application.yaml`)
- MongoDB URI, database
- SMTP Gmail (gửi email OTP, thông báo)
- JWT signerKey, thời hạn token, cookie
- Cấu hình Swagger, CORS, logging


---

## 📚 API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📦 Các module & endpoint chính
- `/api/auth` : Đăng ký, đăng nhập, 2FA, refresh token, quên mật khẩu, logout
- `/api/users` : Quản lý user, phân quyền, tìm kiếm, đổi mật khẩu, điểm
- `/api/events` : Quản lý sự kiện, tạo/cập nhật/hủy, duyệt sự kiện
- `/api/registrations` : Đăng ký, check-in/check-out, hủy đăng ký, lịch sử
- `/api/points` : Quản lý điểm, dashboard điểm, lịch sử điểm
- `/api/analytics` : Phân tích dữ liệu, xu hướng, ROI, hành vi
- `/api/audit` : Audit log, thống kê, phát hiện bất thường
- `/api/dashboard` : Dashboard tổng hợp, thống kê
- `/api/notifications` : Gửi/lấy thông báo
- `/api/feedbacks` : Gửi/lấy phản hồi sự kiện
- `/api/reports` : Báo cáo, xuất file
- `/api/departments` : Quản lý khoa/phòng ban
- `/api/schools` : Quản lý trường học
- `/api/system` : Monitoring, health, metrics
- `/api/async` : Gửi email, notification, xuất báo cáo bất đồng bộ

---

## ✉️ Email & Notification
- Gửi OTP, xác thực 2FA, thông báo điểm, sự kiện, bảo mật, đăng ký thành công, v.v.
- Tùy biến template HTML trong `src/main/resources/templates/email/`
- Hỗ trợ gửi bulk email, notification async

---

## 🛡️ Bảo mật
- JWT, refresh token, cookie bảo mật
- Xác thực 2FA qua email OTP (bắt buộc cho admin/manager)
- Rate limiting,Anh**
