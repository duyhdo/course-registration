# Course Registration System

Hệ thống quản lý và đăng ký học phần được xây dựng bằng Java Spring Boot, Thymeleaf và SQL Server.

Ứng dụng phục vụ mô hình một trường/cơ sở đào tạo, với hai nhóm người dùng chính:

- `ADMIN`: quản lý dữ liệu đào tạo và theo dõi đăng ký học phần.
- `STUDENT`: tra cứu lớp học phần, đăng ký, hủy đăng ký và xem lịch sử.

Hệ thống không bao gồm chức năng đăng nhập dành cho giảng viên.

---

## 1. Chức năng chính

### Quản trị viên

- Đăng nhập / đăng xuất.
- Quản lý sinh viên.
- Quản lý môn học.
- Quản lý lớp học phần.
- Quản lý vòng đời lớp học phần:
  - `DRAFT → OPEN`
  - `OPEN → CLOSED`
  - `DRAFT / OPEN → CANCELLED`
- Theo dõi dữ liệu đăng ký học phần.
- Xem danh sách sinh viên theo lớp học phần.
- Cấu hình học kỳ:
  - thời gian mở đăng ký;
  - thời gian đóng đăng ký;
  - tín chỉ tối thiểu;
  - tín chỉ tối đa.

### Sinh viên

- Đăng nhập / đăng xuất.
- Xem thông tin cá nhân.
- Tra cứu các lớp học phần đang `OPEN`.
- Xem chi tiết lớp học phần.
- Đăng ký học phần.
- Hủy đăng ký học phần.
- Xem các học phần đang đăng ký.
- Xem tổng số tín chỉ hiện tại.
- Xem lịch sử `REGISTERED / CANCELLED`.

### Khôi phục mật khẩu

- Yêu cầu mã xác nhận qua email.
- Xác minh mã.
- Kiểm tra thời hạn mã.
- Đặt lại mật khẩu.
- Mật khẩu được lưu bằng BCrypt.

---

## 2. Công nghệ sử dụng

- Java 17
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- Bootstrap
- Microsoft SQL Server
- Flyway Database Migration
- Maven / Maven Wrapper

---

## 3. Kiến trúc tổng thể

Ứng dụng sử dụng kiến trúc phân lớp:

```text
Browser
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
JPA / Hibernate
   ↓
SQL Server
```

Mô hình dữ liệu chính gồm 8 entity:

```text
TAIKHOAN
KHOA
SINHVIEN
GIANGVIEN
MONHOC
HOCKY
LOPHOCPHAN
DANGKYHOCPHAN
```

Tài liệu kiến trúc chi tiết:

```text
docs/ARCHITECTURE.md
```

---

## 4. Yêu cầu môi trường

Để chạy project ở local cần:

- JDK 17
- Microsoft SQL Server
- Git
- PowerShell hoặc terminal tương đương
- Trình duyệt web

Project đã bao gồm Maven Wrapper nên không bắt buộc cài Maven riêng.

Kiểm tra Java:

```powershell
java -version
```

---

## 5. Clone project

```powershell
git clone <repository-url>
cd course-registration
```

---

## 6. Database Migration

Project sử dụng Flyway để quản lý schema SQL Server.

Cấu trúc:

```text
db/
└── bootstrap/
    └── 00_create_database.sql

src/main/resources/
└── db/
    ├── migration/
    │   ├── V1__create_tables.sql
    │   ├── V2__create_constraints_and_indexes.sql
    │   └── V3__reference_data.sql
    │
    └── seed/
        └── local/
            └── V1000__demo_data.sql
```

### Core migration

Các migration trong:

```text
classpath:db/migration
```

chịu trách nhiệm tạo:

- bảng;
- primary key;
- foreign key;
- index;
- constraint;
- dữ liệu reference cần thiết.

### Local demo seed

Profile `local` chạy thêm:

```text
classpath:db/seed/local
```

để tạo dữ liệu demo phục vụ phát triển và kiểm thử.

Demo seed không được sử dụng cho production.

---

## 7. Tạo database mới

Script bootstrap:

```text
db/bootstrap/00_create_database.sql
```

chỉ có nhiệm vụ tạo database vật lý ban đầu.

Script này không tạo table nghiệp vụ. Toàn bộ schema được quản lý bởi Flyway.

Sau khi database rỗng tồn tại, khi application khởi động:

```text
Flyway
   ↓
V1
   ↓
V2
   ↓
V3
   ↓
V1000 (local only)
   ↓
Hibernate validate
   ↓
Application start
```

Hibernate sử dụng:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

để phát hiện sớm trường hợp Entity và database schema không đồng nhất.

---

## 8. Cấu hình local

Project cung cấp file mẫu:

```text
src/main/resources/application-local.example.properties
```

Tạo file local:

```powershell
Copy-Item `
  .\src\main\resources\application-local.example.properties `
  .\src\main\resources\application-local.properties
```

Sau đó cấu hình SQL Server và các giá trị cần thiết.

`application-local.properties` là cấu hình riêng của máy developer và không nên commit lên Git.

Các giá trị nhạy cảm như:

- DB password;
- SMTP password;
- token;
- secret;

nên được cung cấp thông qua environment variable.

Ví dụ:

```powershell
$env:DB_PASSWORD="<your-local-db-password>"
```

Không commit secret thật vào repository.

---

## 9. Chạy ứng dụng local

Kích hoạt profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
```

Chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

Sau khi application khởi động:

```text
http://localhost:8080/login
```

---

## 10. Dữ liệu demo local

Local seed cung cấp đủ dữ liệu để kiểm thử các chức năng chính:

- nhiều khoa;
- môn học;
- giảng viên;
- học kỳ;
- sinh viên;
- lớp học phần;
- đăng ký `REGISTERED`;
- lịch sử `CANCELLED`.

Dữ liệu bao gồm các trường hợp:

```text
ACTIVE / LOCKED
ACTIVE / INACTIVE
DRAFT / OPEN / CLOSED / CANCELLED
REGISTERED / CANCELLED
```

Ít nhất một học kỳ demo được cấu hình với khoảng thời gian đăng ký tương đối theo thời điểm seed để người mới clone project có thể thử đăng ký ngay.

Demo data chỉ sử dụng dữ liệu giả, không chứa dữ liệu cá nhân thực tế.

---

## 11. Tài khoản demo

Các tài khoản demo được tạo bởi local seed.
ADMIN
- Username: `admin`
- Password: `Demo@123`

STUDENT
- Username: `B24DCCN001`
- Password: `Demo@123`

Thông tin đăng nhập cụ thể cần khớp với:

```text
src/main/resources/db/seed/local/V1000__demo_data.sql
```

Mật khẩu trong database được lưu dưới dạng BCrypt hash, không lưu plaintext.

Các credential demo chỉ dùng cho môi trường local/development.

---

## 12. Quyền truy cập

### Chưa đăng nhập

Chỉ được truy cập các màn hình:

- Login
- Quên mật khẩu
- Xác nhận mã
- Đặt lại mật khẩu

### ADMIN

Truy cập các chức năng:

```text
/admin/**
```

gồm:

- Trang chủ quản trị
- Sinh viên
- Môn học
- Lớp học phần
- Đăng ký
- Học kỳ

### STUDENT

Truy cập:

```text
/student/**
```

gồm:

- Trang chủ sinh viên
- Thông tin cá nhân
- Đăng ký học phần
- Học phần đã đăng ký
- Lịch sử đăng ký

Dashboard không được hiển thị khi chưa xác thực.

---

## 13. Quy tắc đăng ký học phần

Luồng chính:

```text
Lock SinhVien
   ↓
Kiểm tra SinhVien ACTIVE
   ↓
Lock LopHocPhan
   ↓
Kiểm tra OPEN
   ↓
Kiểm tra thời gian đăng ký
   ↓
Kiểm tra đăng ký trùng
   ↓
Kiểm tra cùng môn / cùng học kỳ
   ↓
Kiểm tra sĩ số
   ↓
Kiểm tra TinChiToiDa
   ↓
Tạo REGISTERED
   ↓
Tăng SiSoHienTai
   ↓
Commit
```

---

## 14. Hủy đăng ký

Luồng hủy:

```text
Lock LopHocPhan
   ↓
Kiểm tra trạng thái / thời gian
   ↓
Tìm REGISTERED
   ↓
REGISTERED → CANCELLED
   ↓
NgayHuy = current time
   ↓
Giảm SiSoHienTai
   ↓
Commit
```

Lịch sử đăng ký không bị xóa.

---

## 15. Đăng ký lại sau khi hủy

Hệ thống hỗ trợ:

```text
REGISTERED
   ↓
CANCELLED
   ↓
Đăng ký lại cùng LopHocPhan
   ↓
Tạo REGISTERED mới
```

Bản ghi `CANCELLED` cũ vẫn được giữ làm lịch sử.

Database sử dụng filtered unique index:

```sql
(MaSV, MaLopHocPhan)
WHERE TrangThai = 'REGISTERED'
```

nhằm đảm bảo tại một thời điểm chỉ tồn tại một đăng ký đang hoạt động cho cùng sinh viên và lớp học phần.

---

## 16. Chạy test

### Test không cần full application context

```powershell
.\mvnw.cmd test "-Dtest=*,!CourseRegistrationApplicationTests"
```

### Full local test

Yêu cầu SQL Server local được cấu hình.

```powershell
.\mvnw.cmd clean test "-Dspring.profiles.active=local"
```

Baseline gần nhất:

```text
265 tests
0 failures
0 errors
```

Project cũng có integration test sử dụng SQL Server thật để xác minh một số tình huống concurrency.

---

## 17. Kiểm tra migration từ database rỗng

Migration đã được kiểm thử trên một database rỗng độc lập.

Quy trình kiểm chứng:

```text
Empty SQL Server database
        ↓
Flyway V1
        ↓
Flyway V2
        ↓
Flyway V3
        ↓
Local V1000 seed
        ↓
Hibernate validate
        ↓
Application start
```

Đã xác minh:

- tất cả migration chạy thành công;
- schema đúng với JPA model;
- filtered unique index tồn tại;
- dữ liệu demo được tạo;
- số lượng `REGISTERED` phù hợp;
- `SiSoHienTai` khớp với số đăng ký active;
- không vượt `SiSoToiDa`;
- tài khoản demo đăng nhập được.

---

## 18. Cấu trúc project

```text
course-registration/
├── db/
│   └── bootstrap/
│
├── docs/
│   ├── wireframes/
│   ├── ARCHITECTURE.md
│   ├── TASKS.md
│   └── FUNCTIONAL_COMPLETION_MATRIX.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       ├── db/
│   │       │   ├── migration/
│   │       │   └── seed/local/
│   │       ├── templates/
│   │       └── static/
│   │
│   └── test/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 19. Tài liệu liên quan

Chi tiết kiến trúc:

```text
docs/ARCHITECTURE.md
```

Theo dõi trạng thái công việc:

```text
docs/TASKS.md
```

Functional baseline:

```text
docs/FUNCTIONAL_COMPLETION_MATRIX.md
```

Wireframe:

```text
docs/wireframes/
```

---

## 20. Phạm vi và giới hạn hiện tại

Project hiện không bao gồm:

- đăng nhập giảng viên;
- dashboard giảng viên;
- multi-school / multi-campus;
- kiểm tra xung đột thời khóa biểu;
- waitlist;
- học phí / thanh toán;
- điểm;
- quản lý chương trình đào tạo đầy đủ;
- notification center;
- analytics/dashboard chart;
- audit log;
- rate limiting cho login/password reset.

Đây là các hạng mục ngoài phạm vi phiên bản hiện tại.

---

## 21. Deployment

Deployment EC2 chưa phải một phần của baseline local hiện tại.

Sau khi deployment hoàn tất, tài liệu này sẽ được cập nhật thêm:

- cấu hình EC2;
- systemd;
- Nginx;
- environment variables;
- deployment smoke test;
- release/tag.