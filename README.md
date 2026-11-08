# Course Registration System

## 1. Giới thiệu

Ứng dụng đăng ký học phần cho một trường/cơ sở đào tạo, với hai vai trò `ADMIN` và `STUDENT`. Phạm vi chính gồm quản lý danh mục và lớp học phần, đăng ký/hủy học phần, cấu hình học kỳ và khôi phục mật khẩu.

## 2. Chức năng chính

**ADMIN**
- Quản lý sinh viên và môn học.
- Quản lý lớp học phần và vòng đời lớp.
- Theo dõi đăng ký học phần.
- Cấu hình thời gian đăng ký và giới hạn tín chỉ theo học kỳ.

**STUDENT**
- Xem thông tin cá nhân.
- Xem lớp học phần `OPEN`.
- Đăng ký và hủy đăng ký học phần.
- Xem học phần hiện đã đăng ký và lịch sử đăng ký.

**AUTH**
- Đăng nhập, đăng xuất.
- Quên mật khẩu, xác nhận mã qua email và đặt lại mật khẩu.

## 3. Technology Stack

- Java 17
- Spring Boot 4.0.8: Spring MVC, Spring Security, Spring Data JPA
- Hibernate
- Thymeleaf
- Bootstrap 5.3.3
- Microsoft SQL Server
- Maven Wrapper

## 4. Yêu cầu môi trường

- JDK 17
- SQL Server có schema của ứng dụng
- Maven Wrapper (`mvnw.cmd`); không cần cài Maven riêng
- Trình duyệt web hiện đại

## 5. Cấu hình Local

1. Sao chép `src/main/resources/application-local.example.properties` thành `src/main/resources/application-local.properties`.
2. Cấu hình datasource và SMTP cho môi trường local. Không commit file `application-local.properties`; file này đã được Git ignore.
3. Cấp các giá trị nhạy cảm qua biến môi trường hoặc cấu hình local không được commit. Các biến SMTP được dùng trong cấu hình gồm `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME` và `MAIL_PASSWORD`.
4. Chọn profile `local` bằng `SPRING_PROFILES_ACTIVE`.

Không ghi mật khẩu, token hoặc credential vào tài liệu hay Git.

## 6. Cấu hình Database

Ứng dụng dùng datasource SQL Server. Cấu hình mẫu trỏ tới database `QL_DangKyHocPhan`. Hibernate được cấu hình `ddl-auto=none`; schema phải được chuẩn bị trước khi chạy ứng dụng. Repository hiện không có SQL script khởi tạo database/schema.

## 7. Chạy ứng dụng

Trong Windows PowerShell, từ thư mục gốc:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

## 8. Chạy test

```powershell
.\mvnw.cmd test "-Dtest=*,!CourseRegistrationApplicationTests"
.\mvnw.cmd clean test "-Dspring.profiles.active=local"
```

Baseline đã xác nhận: 264 test trong lệnh regression không chạy `CourseRegistrationApplicationTests`; 265 test trong clean local-profile suite.

## 9. Quyền truy cập

- Anonymous: trang đăng nhập (`/login`), quên mật khẩu (`/forgot-password`), xác nhận mã (`/forgot-password/verify`) và đặt lại mật khẩu (`/forgot-password/reset`).
- `ADMIN`: `/admin/**`.
- `STUDENT`: `/student/**`.
- Các dashboard yêu cầu đăng nhập; truy cập đường dẫn theo vai trò khác bị Spring Security từ chối.
- Không có đăng nhập giảng viên.

## 10. Cấu trúc thư mục

```text
src/main/java/                 Mã nguồn Java
src/main/resources/templates/  Thymeleaf templates
src/main/resources/static/     CSS và tài nguyên tĩnh
src/test/                      Tests
docs/                          Tài liệu và wireframes
```

## 11. Tài liệu liên quan

- [Kiến trúc](docs/ARCHITECTURE.md)
- [Theo dõi công việc](docs/TASKS.md)
- [Functional completion matrix](docs/FUNCTIONAL_COMPLETION_MATRIX.md)
- [F-03 verification](docs/F03_VERIFICATION.md)

F-01, F-04 và F-05 được tóm tắt trong tài liệu kiến trúc và theo dõi công việc; không có tài liệu riêng cho các mã này trong `docs/`.
