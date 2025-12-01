# CHI TIẾT ADMIN DASHBOARD (WEB APPLICATION)

> **Cập nhật:** Tài liệu này được cập nhật dựa trên code thực tế của dự án Event Management Backend Java Spring

## 📋 MỤC LỤC

1. [Tổng quan](#tổng-quan)
2. [Layout chung](#layout-chung)
3. [Các trang chính](#các-trang-chính)
4. [API Reference](#api-reference)
5. [Phân quyền theo role](#phân-quyền-theo-role)
6. [Chi tiết từng trang](#chi-tiết-từng-trang)

---

## 📊 TỔNG QUAN

**Admin Dashboard** là ứng dụng Web được sử dụng bởi các vai trò:
- **ADMIN** - Quản trị viên hệ thống (đầy đủ quyền)
- **FACULTY_ADMIN** - Quản lý khoa (quyền hạn giới hạn trong phạm vi khoa)
- **ORGANIZER** - Người tổ chức sự kiện (quyền hạn giới hạn cho sự kiện của mình)
- **STUDENT** - Sinh viên (chỉ xem và đăng ký sự kiện)
- **FACULTY_SCANNER / SCHOOL_SCANNER** - Người quét QR code check-in/check-out

**Công nghệ đề xuất:**
- Frontend: React.js / Vue.js / Angular
- UI Framework: Material-UI / Ant Design / Tailwind CSS
- Charts: Chart.js / Recharts / ApexCharts
- State Management: Redux / Zustand / Pinia
- API Client: Axios / Fetch API

**Base URL:** `https://your-backend-url.com/api`

---

## 🎨 LAYOUT CHUNG

### **Header (Top Navigation Bar)**

#### **Hiển thị cho tất cả roles:**
- ✅ Logo hệ thống (bên trái)
- ✅ Tên người dùng hiện tại
- ✅ Avatar người dùng
- ✅ Dropdown menu:
  - Xem thông tin cá nhân (`GET /api/users/me`)
  - Đổi mật khẩu (`PUT /api/users/change-password`)
  - Cài đặt 2FA (nếu có quyền)
  - Đăng xuất (`POST /api/auth/logout`)
- ✅ Thông báo (Notification Bell) - hiển thị số lượng thông báo chưa đọc (`GET /api/notifications/user/{userId}`)
- ✅ Tìm kiếm nhanh (Quick Search) - tìm kiếm sự kiện, người dùng

---

### **Sidebar (Left Navigation Menu)**

#### **Menu Items:**

##### **1. Dashboard** (🏠) - `/dashboard`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị
- **ORGANIZER:** ❌ **ẨN** (không có quyền xem dashboard)
- **API:** `GET /api/dashboard`

##### **2. Quản lý Sự kiện** (📅) - `/events`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị
- **ORGANIZER:** ✅ Hiển thị
- **API:** `GET /api/events`, `POST /api/events`, `PUT /api/events/{id}`

##### **3. Quản lý Người dùng** (👥) - `/users`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị (chỉ xem, không tạo/sửa/xóa)
- **ORGANIZER:** ❌ **ẨN**
- **API:** `GET /api/users/paginated`, `GET /api/users/search`

##### **4. Quản lý Khoa** (🏛️) - `/departments`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ❌ **ẨN** (chỉ có thể cập nhật điểm phạt khoa của mình qua API)
- **ORGANIZER:** ❌ **ẨN**
- **API:** `GET /api/departments`, `POST /api/departments`, `PUT /api/departments/{id}/penalty-points`

##### **5. Quản lý Điểm** (⭐) - `/points`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị (chỉ trong phạm vi khoa)
- **ORGANIZER:** ✅ Hiển thị (chỉ xem)
- **API:** `GET /api/points/user/{userId}`, `PUT /api/points/training`, `PUT /api/points/social`

##### **6. Quản lý Đăng ký** (📝) - `/registrations`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị (chỉ trong phạm vi khoa)
- **ORGANIZER:** ✅ Hiển thị (chỉ sự kiện của mình)
- **STUDENT:** ✅ Hiển thị (chỉ đăng ký của mình)
- **API:** `GET /api/registrations/my-registrations`

##### **7. Báo cáo** (📊) - `/reports`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ❌ **ẨN**
- **ORGANIZER:** ❌ **ẨN**
- **API:** `GET /api/reports/events`, `GET /api/reports/students/top`, `GET /api/reports/departments`

##### **8. Audit Log** (📋) - `/audit`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ❌ **ẨN**
- **ORGANIZER:** ❌ **ẨN**
- **API:** `GET /api/audit/logs`, `GET /api/audit/logs/paginated`

##### **9. Quản lý Học vụ** (🎓) - `/academic`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị (chỉ trong phạm vi khoa)
- **ORGANIZER:** ✅ Hiển thị (chỉ xem)
- **API:** `GET /api/academic/current-semester/{userId}`, `PUT /api/academic/update-academic-info/{userId}`

##### **10. Quản lý Hệ thống** (⚙️) - `/system`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ❌ **ẨN**
- **ORGANIZER:** ❌ **ẨN**
- **API:** `GET /api/system/health`, `GET /api/admin/user-sessions/active`, `GET /api/admin/token-blacklist`

##### **11. Thông báo** (🔔) - `/notifications`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị
- **ORGANIZER:** ✅ Hiển thị
- **API:** `GET /api/notifications/user/{userId}`, `POST /api/notifications`

##### **12. Phân tích** (📈) - `/analytics`
- **ADMIN:** ✅ Hiển thị
- **FACULTY_ADMIN:** ✅ Hiển thị (có thể giới hạn)
- **ORGANIZER:** ❌ **ẨN**
- **API:** `POST /api/analytics/summary`, `POST /api/analytics/participation-trends`

##### **13. Phản hồi** (💬) - `/feedbacks`
- **Tất cả roles:** ✅ Hiển thị
- **API:** `GET /api/feedbacks/event/{eventId}`, `POST /api/feedbacks`

---

## 📄 CÁC TRANG CHÍNH

### **1. Trang Dashboard** (`/dashboard`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ✅ Có quyền
- **ORGANIZER:** ❌ **KHÔNG có quyền** (trang này bị ẩn)

#### **API Endpoints:**

**Main Dashboard:**
```
GET /api/dashboard?startDate={date}&endDate={date}&preset={preset}
```

**Sub-endpoints:**
```
GET /api/dashboard/overview
GET /api/dashboard/time-series
GET /api/dashboard/departments
GET /api/dashboard/top-students
GET /api/dashboard/top-organizers
GET /api/dashboard/events
GET /api/dashboard/points
GET /api/dashboard/security
GET /api/dashboard/recent-activities
```

**Date Presets:** `TODAY`, `LAST_7_DAYS`, `LAST_30_DAYS`, `LAST_90_DAYS`, `THIS_WEEK`, `LAST_WEEK`, `THIS_MONTH`, `LAST_MONTH`, `THIS_QUARTER`, `LAST_QUARTER`, `THIS_YEAR`, `LAST_YEAR`, `ALL_TIME`

#### **1.1. ADMIN - Dashboard tổng hợp**

##### **Overview Stats Cards:**
1. **Tổng số Người dùng** (`overviewStats.totalUsers`)
2. **Tổng số Sự kiện** (`overviewStats.totalEvents`)
3. **Tổng số Đăng ký** (`overviewStats.totalRegistrations`)
4. **Tổng số Khoa** (`overviewStats.totalDepartments`)
5. **Sự kiện đang diễn ra** (`overviewStats.activeEvents`)
6. **Sự kiện đã hoàn thành** (`overviewStats.completedEvents`)

##### **Charts Section:**

**Time Series Stats:**
- `timeSeriesStats.eventsCreated` - Sự kiện được tạo theo thời gian
- `timeSeriesStats.registrations` - Đăng ký theo thời gian
- `timeSeriesStats.pointsAwarded` - Điểm được cấp theo thời gian
- `timeSeriesStats.userLogins` - Đăng nhập theo thời gian

**Department Stats:**
- Bảng thống kê theo từng khoa (`departmentStats[]`)
- Mỗi khoa có: `totalUsers`, `totalEvents`, `totalRegistrations`, `totalPoints`, `averagePointsPerUser`, `participationRate`

**Top Performers:**
- `topStudents[]` - Top sinh viên có điểm cao nhất
- `topOrganizers[]` - Top organizers

**Event Stats:**
- `eventStats.totalEvents`, `eventStats.upcomingEvents`, `eventStats.ongoingEvents`, `eventStats.completedEvents`, `eventStats.cancelledEvents`
- `eventStats.eventsByType` - Map theo loại (TRAINING/SOCIAL)
- `eventStats.eventsByStatus` - Map theo trạng thái

**Points Stats:**
- `pointsStats.totalTrainingPoints`, `pointsStats.totalSocialPoints`
- `pointsStats.pointsBySemester` - Điểm theo kỳ học

**Security Stats:**
- `securityStats.activeSessions` - Số lượng session đang hoạt động
- `securityStats.todayLogins` - Đăng nhập hôm nay
- `securityStats.auditLogsToday` - Audit logs hôm nay

**Recent Activities:**
- `recentActivities[]` - Danh sách hoạt động gần đây

#### **1.2. FACULTY_ADMIN - Dashboard khoa**

- Tương tự ADMIN nhưng chỉ hiển thị dữ liệu trong khoa của mình
- Không có Security Stats
- Không có nút "Xuất báo cáo"

---

### **2. Trang Quản lý Sự kiện** (`/events`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền (xem tất cả sự kiện)
- **FACULTY_ADMIN:** ✅ Có quyền (chỉ xem sự kiện trong khoa)
- **ORGANIZER:** ✅ Có quyền (chỉ xem sự kiện của mình)

#### **API Endpoints:**

```
GET    /api/events                    - Lấy tất cả sự kiện
POST   /api/events                    - Tạo sự kiện mới (ADMIN, FACULTY_ADMIN, ORGANIZER)
PUT    /api/events/{id}               - Cập nhật sự kiện (ADMIN, FACULTY_ADMIN, ORGANIZER)
PUT    /api/events/{id}/approve        - Duyệt sự kiện
PUT    /api/events/{id}/cancel        - Hủy sự kiện (ADMIN, FACULTY_ADMIN, ORGANIZER)
```

#### **2.1. ADMIN - Quản lý Sự kiện**

##### **Layout:**
- **Header Section:**
  - Tiêu đề: "Quản lý Sự kiện"
  - Nút "Tạo sự kiện mới" → `POST /api/events`
  - Bộ lọc: Trạng thái, Khoa, Loại, Tìm kiếm

##### **Table Columns:**
1. Checkbox
2. Tên sự kiện
3. Khoa
4. Loại (TRAINING/SOCIAL)
5. Thời gian bắt đầu
6. Thời gian kết thúc
7. Trạng thái (Badge màu)
8. Số lượng đăng ký
9. Số lượng check-in
10. Organizer
11. Actions:
    - Xem chi tiết (👁️)
    - Sửa (✏️) → `PUT /api/events/{id}`
    - Duyệt (✅) → `PUT /api/events/{id}/approve`
    - Hủy (❌) → `PUT /api/events/{id}/cancel`

#### **2.2. FACULTY_ADMIN - Quản lý Sự kiện**

- Chỉ hiển thị sự kiện trong khoa của mình
- Không có bộ lọc "Khoa"
- Không có nút "Xuất Excel"

#### **2.3. ORGANIZER - Quản lý Sự kiện**

- Chỉ hiển thị sự kiện mà mình là organizer
- Không có quyền duyệt sự kiện
- Không có Bulk Actions

---

### **3. Trang Quản lý Người dùng** (`/users`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền (xem tất cả người dùng)
- **FACULTY_ADMIN:** ✅ Có quyền (chỉ xem người dùng trong khoa)
- **ORGANIZER:** ❌ **KHÔNG có quyền** (trang này bị ẩn)

#### **API Endpoints:**

```
GET    /api/users/me                          - Lấy thông tin cá nhân
PUT    /api/users/change-password              - Đổi mật khẩu
PUT    /api/users/points                      - Cập nhật điểm cá nhân
POST   /api/users/admin-create                - Tạo người dùng (ADMIN only)
POST   /api/users/bulk-create                 - Import từ Excel (ADMIN only)
GET    /api/users/paginated                   - Lấy danh sách có phân trang (ADMIN, FACULTY_ADMIN)
GET    /api/users/search                      - Tìm kiếm người dùng (ADMIN, FACULTY_ADMIN)
GET    /api/users/role/{role}                 - Lấy theo role (ADMIN, FACULTY_ADMIN)
GET    /api/users/department/{departmentId}   - Lấy theo khoa (ADMIN, FACULTY_ADMIN)
GET    /api/users/top-students                - Top sinh viên (ADMIN, FACULTY_ADMIN)
GET    /api/users/active                      - Người dùng hoạt động (ADMIN, FACULTY_ADMIN)
POST   /api/users/forgot-password             - Quên mật khẩu
```

#### **3.1. ADMIN - Quản lý Người dùng**

##### **Tabs:**
- Tất cả người dùng → `GET /api/users/paginated`
- Top sinh viên → `GET /api/users/top-students`
- Người dùng hoạt động → `GET /api/users/active?daysAgo=7`

##### **Actions:**
- Tạo người dùng mới → `POST /api/users/admin-create`
- Import từ Excel → `POST /api/users/bulk-create` (multipart/form-data)

#### **3.2. FACULTY_ADMIN - Quản lý Người dùng**

- Chỉ có quyền xem, không thể tạo/sửa/xóa
- Chỉ hiển thị người dùng trong khoa của mình
- Không có nút "Tạo người dùng mới"
- Không có nút "Import từ Excel"

---

### **4. Trang Quản lý Khoa** (`/departments`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ❌ **KHÔNG có quyền** (trang này bị ẩn, chỉ có thể cập nhật điểm phạt qua API)
- **ORGANIZER:** ❌ **KHÔNG có quyền**

#### **API Endpoints:**

```
GET    /api/departments                      - Lấy tất cả khoa
GET    /api/departments/{id}                 - Lấy khoa theo ID
GET    /api/departments/search?name={name}   - Tìm kiếm khoa
POST   /api/departments                      - Tạo khoa mới (ADMIN only)
PUT    /api/departments/{id}                 - Cập nhật khoa (ADMIN only)
PUT    /api/departments/{id}/penalty-points  - Cập nhật điểm phạt (ADMIN, FACULTY_ADMIN)
DELETE /api/departments/{id}                 - Xóa khoa (ADMIN only)
```

#### **4.1. ADMIN - Quản lý Khoa**

- Có đầy đủ quyền: Tạo, Sửa, Xóa, Cập nhật điểm phạt

#### **4.2. FACULTY_ADMIN**

- Không có trang quản lý khoa
- Chỉ có thể cập nhật điểm phạt khoa của mình qua API: `PUT /api/departments/{id}/penalty-points`

---

### **5. Trang Quản lý Điểm** (`/points`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền (xem tất cả điểm)
- **FACULTY_ADMIN:** ✅ Có quyền (chỉ xem điểm trong khoa)
- **ORGANIZER:** ✅ Có quyền (chỉ xem điểm)

#### **API Endpoints:**

```
GET    /api/points/user/{userId}                    - Lấy điểm của user (ADMIN, FACULTY_ADMIN, ORGANIZER, hoặc chính user đó)
GET    /api/points/user/{userId}/history           - Lịch sử điểm (ADMIN, FACULTY_ADMIN, ORGANIZER, hoặc chính user đó)
GET    /api/points/my-points?userId={id}           - Điểm của tôi
GET    /api/points/my-history?userId={id}           - Lịch sử điểm của tôi
GET    /api/points/semesters                        - Danh sách kỳ học
PUT    /api/points/training                        - Cập nhật điểm rèn luyện (ADMIN, FACULTY_ADMIN)
PUT    /api/points/social                          - Cập nhật điểm xã hội (ADMIN, FACULTY_ADMIN)
POST   /api/points/events/{eventId}/manual-update  - Cập nhật điểm thủ công cho sự kiện (ADMIN, FACULTY_ADMIN)
GET    /api/points/events/{eventId}/report         - Báo cáo điểm sự kiện (ADMIN, FACULTY_ADMIN)
POST   /api/points/events/{eventId}/manual-process - Xử lý điểm thủ công (ADMIN, FACULTY_ADMIN)
GET    /api/points/events/{eventId}/pending        - Đăng ký chưa xử lý điểm (ADMIN, FACULTY_ADMIN)
GET    /api/points/dashboard                       - Dashboard xử lý điểm (ADMIN, FACULTY_ADMIN)
POST   /api/points/update                          - Cập nhật điểm (generic)
POST   /api/points/manual                          - Xử lý điểm thủ công (generic)
POST   /api/points/bulk                            - Cập nhật điểm hàng loạt
```

#### **5.1. ADMIN - Quản lý Điểm**

##### **Tabs:**
- Tất cả điểm
- Điểm rèn luyện
- Điểm xã hội
- Xử lý điểm thủ công → `GET /api/points/dashboard`
- Báo cáo điểm sự kiện

##### **Actions:**
- Cập nhật điểm rèn luyện → `PUT /api/points/training`
- Cập nhật điểm xã hội → `PUT /api/points/social`
- Xử lý điểm thủ công → `POST /api/points/events/{eventId}/manual-process`

#### **5.2. FACULTY_ADMIN - Quản lý Điểm**

- Chỉ hiển thị điểm của sinh viên trong khoa
- Có thể cập nhật điểm trong khoa

#### **5.3. ORGANIZER - Quản lý Điểm**

- Chỉ có thể xem điểm, không thể cập nhật
- Không có tab "Xử lý điểm thủ công"
- Không có tab "Báo cáo điểm sự kiện"

---

### **6. Trang Quản lý Đăng ký** (`/registrations`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền (xem tất cả đăng ký)
- **FACULTY_ADMIN:** ✅ Có quyền (chỉ xem đăng ký trong khoa)
- **ORGANIZER:** ✅ Có quyền (chỉ xem đăng ký sự kiện của mình)
- **STUDENT:** ✅ Có quyền (chỉ xem đăng ký của mình)

#### **API Endpoints:**

```
POST   /api/registrations                      - Đăng ký sự kiện (STUDENT)
POST   /api/registrations/{eventId}/check-in   - Check-in (FACULTY_SCANNER, SCHOOL_SCANNER)
POST   /api/registrations/{eventId}/check-out  - Check-out (FACULTY_SCANNER, SCHOOL_SCANNER)
PUT    /api/registrations/{eventId}/cancel     - Hủy đăng ký (STUDENT)
GET    /api/registrations/my-registrations     - Đăng ký của tôi (STUDENT)
GET    /api/registrations/my-registrations/{status} - Đăng ký theo trạng thái (STUDENT)
```

#### **6.1. ADMIN - Quản lý Đăng ký**

- Xem tất cả đăng ký
- Có thể xử lý điểm thủ công

#### **6.2. FACULTY_ADMIN - Quản lý Đăng ký**

- Chỉ hiển thị đăng ký của sinh viên trong khoa

#### **6.3. ORGANIZER - Quản lý Đăng ký**

- Chỉ hiển thị đăng ký của sự kiện mà mình là organizer
- Không thể xử lý điểm thủ công

---

### **7. Trang Báo cáo** (`/reports`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ❌ **KHÔNG có quyền** (trang này bị ẩn)
- **ORGANIZER:** ❌ **KHÔNG có quyền**

#### **API Endpoints:**

```
GET    /api/reports/events              - Báo cáo sự kiện theo trạng thái (ADMIN)
GET    /api/reports/students/top?limit=10 - Top sinh viên (ADMIN)
GET    /api/reports/departments         - Báo cáo sự kiện theo khoa (ADMIN)
```

#### **7.1. ADMIN - Báo cáo**

##### **Tabs:**
- Báo cáo sự kiện → `GET /api/reports/events`
- Báo cáo sinh viên → `GET /api/reports/students/top`
- Báo cáo khoa → `GET /api/reports/departments`

---

### **8. Trang Audit Log** (`/audit`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ❌ **KHÔNG có quyền** (trang này bị ẩn)
- **ORGANIZER:** ❌ **KHÔNG có quyền**

#### **API Endpoints:**

```
GET    /api/audit/logs                                    - Tìm kiếm audit logs (ADMIN)
GET    /api/audit/logs/paginated                           - Tìm kiếm có phân trang (ADMIN)
GET    /api/audit/users/{userId}/logs                      - Logs theo user (ADMIN)
GET    /api/audit/users/{userId}/logs/paginated           - Logs theo user có phân trang (ADMIN)
GET    /api/audit/actions/{action}/logs                    - Logs theo action (ADMIN)
GET    /api/audit/actions/{action}/logs/paginated         - Logs theo action có phân trang (ADMIN)
GET    /api/audit/resources/{resourceType}/{resourceId}/logs - Logs theo resource (ADMIN)
GET    /api/audit/departments/{departmentId}/logs         - Logs theo khoa (ADMIN)
GET    /api/audit/statistics?startDate={date}&endDate={date} - Thống kê audit (ADMIN)
GET    /api/audit/suspicious?since={date}                 - Hoạt động đáng ngờ (ADMIN)
```

#### **8.1. ADMIN - Audit Log**

##### **Tabs:**
- Tất cả logs → `GET /api/audit/logs/paginated`
- Logs theo user → `GET /api/audit/users/{userId}/logs/paginated`
- Logs theo action → `GET /api/audit/actions/{action}/logs/paginated`
- Logs theo resource → `GET /api/audit/resources/{resourceType}/{resourceId}/logs/paginated`
- Logs theo khoa → `GET /api/audit/departments/{departmentId}/logs/paginated`
- Hoạt động đáng ngờ → `GET /api/audit/suspicious?since={date}`

##### **Filters:**
- User ID
- Action
- Resource Type
- Resource ID
- Status (Success/Failed)
- Start Date - End Date
- User Role
- Department ID

---

### **9. Trang Quản lý Học vụ** (`/academic`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền (xem tất cả)
- **FACULTY_ADMIN:** ✅ Có quyền (chỉ trong khoa)
- **ORGANIZER:** ✅ Có quyền (chỉ xem)

#### **API Endpoints:**

```
GET    /api/academic/current-semester/{userId}                    - Học kỳ hiện tại (ADMIN, FACULTY_ADMIN, ORGANIZER, hoặc chính user đó)
GET    /api/academic/semester-info/{userId}                       - Thông tin học kỳ (ADMIN, FACULTY_ADMIN, ORGANIZER, hoặc chính user đó)
GET    /api/academic/semesters                                    - Danh sách kỳ học
PUT    /api/academic/update-academic-info/{userId}                - Cập nhật thông tin học vụ (ADMIN, FACULTY_ADMIN)
POST   /api/academic/calculate-semester-by-date/{userId}?targetDate={date} - Tính học kỳ theo ngày (ADMIN, FACULTY_ADMIN, ORGANIZER, hoặc chính user đó)
GET    /api/academic/all-users-semester-info                     - Tất cả users với thông tin học kỳ (ADMIN, FACULTY_ADMIN)
```

#### **9.1. ADMIN - Quản lý Học vụ**

- Có thể cập nhật thông tin học vụ cho tất cả users
- Có thể xem tất cả users với thông tin học kỳ

#### **9.2. FACULTY_ADMIN - Quản lý Học vụ**

- Chỉ hiển thị sinh viên trong khoa
- Có thể cập nhật thông tin học vụ trong khoa

#### **9.3. ORGANIZER - Quản lý Học vụ**

- Chỉ có thể xem, không thể cập nhật

---

### **10. Trang Quản lý Hệ thống** (`/system`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ❌ **KHÔNG có quyền** (trang này bị ẩn)
- **ORGANIZER:** ❌ **KHÔNG có quyền**

#### **API Endpoints:**

**System Monitoring (Public - không cần auth):**
```
GET    /api/system/health      - Health check
GET    /api/system/status      - System status
GET    /api/system/memory      - Memory info
GET    /api/system/cpu         - CPU info
GET    /api/system/runtime     - Runtime info
GET    /api/system/disk        - Disk info
GET    /api/system/threads     - Thread info
GET    /api/system/properties  - System properties
POST   /api/system/gc          - Force garbage collection
```

**User Sessions (ADMIN only):**
```
GET    /api/admin/user-sessions/active                    - Active sessions (ADMIN)
GET    /api/admin/user-sessions/user/{username}          - Sessions của user (ADMIN)
GET    /api/admin/user-sessions/user/{username}/active   - Active sessions của user (ADMIN)
GET    /api/admin/user-sessions/user/{username}/count    - Số lượng active sessions (ADMIN)
POST   /api/admin/user-sessions/user/{username}/force-logout - Force logout user (ADMIN)
DELETE /api/admin/user-sessions/session/{token}           - Đóng session cụ thể (ADMIN)
GET    /api/admin/user-sessions/session/{token}           - Thông tin session (ADMIN)
```

**Token Blacklist (ADMIN only):**
```
GET    /api/admin/token-blacklist                         - Tất cả blacklisted tokens (ADMIN)
GET    /api/admin/token-blacklist/user/{username}         - Tokens của user (ADMIN)
GET    /api/admin/token-blacklist/reason/{reason}        - Tokens theo lý do (ADMIN)
GET    /api/admin/token-blacklist/check/{token}          - Kiểm tra token (ADMIN)
DELETE /api/admin/token-blacklist/{token}                - Xóa khỏi blacklist (ADMIN)
DELETE /api/admin/token-blacklist/user/{username}        - Xóa tất cả tokens của user (ADMIN)
POST   /api/admin/token-blacklist/revoke/{token}         - Revoke token (ADMIN)
POST   /api/admin/token-blacklist/compromise/{token}      - Mark as compromised (ADMIN)
```

**Async Operations (ADMIN, FACULTY_ADMIN):**
```
POST   /api/async/bulk-email                    - Gửi email hàng loạt (ADMIN, FACULTY_ADMIN)
POST   /api/async/event-notification           - Gửi thông báo sự kiện (ADMIN, FACULTY_ADMIN)
POST   /api/async/bulk-notifications           - Gửi thông báo hàng loạt (ADMIN, FACULTY_ADMIN)
POST   /api/async/department-notification      - Gửi thông báo khoa (ADMIN, FACULTY_ADMIN)
POST   /api/async/auto-process-points          - Xử lý điểm tự động (ADMIN, FACULTY_ADMIN)
POST   /api/async/export-event-report          - Xuất báo cáo sự kiện (ADMIN, FACULTY_ADMIN)
POST   /api/async/export-user-report          - Xuất báo cáo user (ADMIN, FACULTY_ADMIN)
POST   /api/async/export-points-report        - Xuất báo cáo điểm (ADMIN, FACULTY_ADMIN)
```

#### **10.1. ADMIN - Quản lý Hệ thống**

##### **Tabs:**
- Quản lý Session → `GET /api/admin/user-sessions/active`
- Token Blacklist → `GET /api/admin/token-blacklist`
- Gửi Email hàng loạt → `POST /api/async/bulk-email`
- Gửi Thông báo → `POST /api/async/bulk-notifications`
- Monitoring → `GET /api/system/status`

---

### **11. Trang Thông báo** (`/notifications`)

#### **Quyền truy cập:**
- **Tất cả roles:** ✅ Có quyền

#### **API Endpoints:**

```
POST   /api/notifications                    - Gửi thông báo
GET    /api/notifications/user/{userId}      - Thông báo của user
```

---

### **12. Trang Phân tích** (`/analytics`)

#### **Quyền truy cập:**
- **ADMIN:** ✅ Có quyền
- **FACULTY_ADMIN:** ✅ Có quyền (có thể giới hạn)
- **ORGANIZER:** ❌ **KHÔNG có quyền**

#### **API Endpoints:**

```
POST   /api/analytics/summary                        - Tổng quan phân tích
POST   /api/analytics/participation-trends?startDate={date}&endDate={date} - Xu hướng tham gia
GET    /api/analytics/event-roi                     - Phân tích ROI sự kiện
GET    /api/analytics/user-behavior                  - Phân tích hành vi người dùng
```

---

### **13. Trang Phản hồi** (`/feedbacks`)

#### **Quyền truy cập:**
- **Tất cả roles:** ✅ Có quyền

#### **API Endpoints:**

```
POST   /api/feedbacks                    - Gửi phản hồi
GET    /api/feedbacks/event/{eventId}    - Phản hồi theo sự kiện
GET    /api/feedbacks/user/{userId}      - Phản hồi theo user
```

---

## 🔐 PHÂN QUYỀN THEO ROLE - TỔNG HỢP

### **ADMIN (Quản trị viên hệ thống)**
- ✅ **Có quyền đầy đủ** trên tất cả các trang
- ✅ Có thể xem, tạo, sửa, xóa tất cả dữ liệu
- ✅ Có quyền truy cập tất cả các tính năng
- ✅ Có thể xuất báo cáo
- ✅ Có thể xem audit logs
- ✅ Có thể quản lý hệ thống (sessions, tokens, async operations)

### **FACULTY_ADMIN (Quản lý khoa)**
- ⚠️ **Quyền hạn giới hạn** trong phạm vi khoa của mình
- ✅ Có thể xem dashboard (chỉ khoa của mình)
- ✅ Có thể quản lý sự kiện (chỉ trong khoa)
- ✅ Có thể xem người dùng (chỉ trong khoa, không tạo/sửa/xóa)
- ❌ Không thể quản lý khoa (chỉ có thể cập nhật điểm phạt khoa của mình)
- ✅ Có thể quản lý điểm (chỉ trong khoa)
- ✅ Có thể quản lý đăng ký (chỉ trong khoa)
- ❌ Không thể xem báo cáo tổng hợp
- ❌ Không thể xem audit logs
- ❌ Không thể quản lý hệ thống (sessions, tokens)
- ✅ Có thể sử dụng async operations (gửi email, thông báo)

### **ORGANIZER (Người tổ chức sự kiện)**
- ⚠️ **Quyền hạn giới hạn** cho sự kiện của mình
- ❌ Không thể xem dashboard
- ✅ Có thể quản lý sự kiện (chỉ sự kiện của mình)
- ❌ Không thể quản lý người dùng
- ❌ Không thể quản lý khoa
- ✅ Có thể xem điểm (chỉ xem, không cập nhật)
- ✅ Có thể xem đăng ký (chỉ sự kiện của mình)
- ❌ Không thể xem báo cáo
- ❌ Không thể xem audit logs
- ❌ Không thể quản lý hệ thống

### **STUDENT (Sinh viên)**
- ⚠️ **Quyền hạn rất hạn chế**
- ✅ Có thể đăng ký sự kiện
- ✅ Có thể xem đăng ký của mình
- ✅ Có thể hủy đăng ký
- ✅ Có thể xem điểm của mình
- ✅ Có thể gửi phản hồi

### **FACULTY_SCANNER / SCHOOL_SCANNER**
- ✅ Có thể check-in/check-out sự kiện
- ❌ Không có quyền truy cập dashboard

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Phân quyền phải được kiểm tra ở cả Frontend và Backend:**
   - Frontend: Ẩn/hiện các nút, menu, trang dựa trên role
   - Backend: API phải kiểm tra quyền trước khi trả về dữ liệu (đã được implement bằng @PreAuthorize)

2. **2FA bắt buộc:**
   - ADMIN và FACULTY_ADMIN phải bật 2FA (không thể tắt)
   - ORGANIZER có thể bật/tắt 2FA tùy chọn

3. **Dữ liệu được lọc tự động:**
   - FACULTY_ADMIN: Tất cả API chỉ trả về dữ liệu trong khoa của mình (cần implement ở service layer)
   - ORGANIZER: Tất cả API chỉ trả về dữ liệu của sự kiện mình tổ chức (cần implement ở service layer)

4. **UI/UX:**
   - Các nút/chức năng không có quyền nên bị ẩn hoàn toàn, không chỉ disable
   - Hiển thị thông báo rõ ràng khi người dùng cố gắng truy cập trang không có quyền
   - Badge hiển thị role và khoa hiện tại ở header

5. **API Authentication:**
   - Tất cả API (trừ `/api/auth/**` và `/api/system/health`) đều yêu cầu JWT token
   - Token được gửi qua Cookie hoặc Authorization header
   - Refresh token được sử dụng để lấy token mới

6. **Error Handling:**
   - Tất cả API trả về format: `{ success: boolean, message: string, data: any }`
   - HTTP status codes: 200 (success), 400 (bad request), 401 (unauthorized), 403 (forbidden), 404 (not found), 500 (server error)

---

## 📚 API REFERENCE

### **Authentication APIs**

```
POST   /api/auth/register              - Đăng ký
POST   /api/auth/login                 - Đăng nhập
POST   /api/auth/logout                - Đăng xuất
POST   /api/auth/refresh               - Refresh token
POST   /api/auth/verify-2fa            - Xác thực 2FA
POST   /api/auth/resend-otp            - Gửi lại OTP
```

### **Swagger Documentation**

- Swagger UI: `http://your-backend-url/swagger-ui.html`
- OpenAPI JSON: `http://your-backend-url/v3/api-docs`

---

**Tài liệu này cung cấp chi tiết đầy đủ về ADMIN DASHBOARD dựa trên code thực tế để hỗ trợ việc phát triển Frontend.**

**Cập nhật lần cuối:** Dựa trên code commit mới nhất
