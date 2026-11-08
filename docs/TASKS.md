# Project Tasks

Ký hiệu: `[x] DONE` — hoàn tất; `[-] IN PROGRESS` — đang thực hiện; `[ ] TODO` — chưa thực hiện; `[~] DEFERRED / OUT OF SCOPE` — hoãn hoặc ngoài phạm vi.

## 1. Core Functionality

- [x] DONE — Đăng nhập, đăng xuất và phân quyền `ADMIN` / `STUDENT`.
- [x] DONE — Quản lý sinh viên.
- [x] DONE — Quản lý môn học.
- [x] DONE — Quản lý lớp học phần.
- [x] DONE — Cấu hình học kỳ.
- [x] DONE — Đăng ký học phần.
- [x] DONE — Hủy đăng ký học phần.
- [x] DONE — Xem học phần đã đăng ký và lịch sử đăng ký.
- [x] DONE — Admin theo dõi đăng ký.
- [x] DONE — Quên mật khẩu, xác nhận mã email và đặt lại mật khẩu.

## 2. Concurrency / Data Integrity

- [x] DONE — **F-01:** căn chỉnh metadata đăng ký lại; chỉ cho phép một bản ghi `REGISTERED` cho cùng sinh viên và lớp.
- [x] DONE — **F-03:** kiểm thử hủy đồng thời trên SQL Server; không tái hiện double-cancellation hoặc double-decrement trong kịch bản kiểm thử.
- [x] DONE — **F-04:** khóa lớp học phần trước khi Admin kiểm tra `REGISTERED` và hủy lớp.
- [x] DONE — **F-05:** tuần tự hóa đăng ký đồng thời của cùng sinh viên theo thứ tự khóa `SinhVien → LopHocPhan`.

## 3. UI

- [x] DONE — Shared shell cho các trang Admin và Student đã xác thực.
- [x] DONE — Căn chỉnh giao diện Admin.
- [x] DONE — Căn chỉnh giao diện Student.
- [x] DONE — Modal xác nhận hủy đăng ký.
- [x] DONE — Căn chỉnh nội dung theo wireframe trong phạm vi giao diện.

## 4. Testing

- [x] DONE — Compile: `.\mvnw.cmd -q -DskipTests compile`.
- [x] DONE — Regression suite: `.\mvnw.cmd test "-Dtest=*,!CourseRegistrationApplicationTests"` — 264 test.
- [x] DONE — Local full suite: `.\mvnw.cmd clean test "-Dspring.profiles.active=local"` — 265 test.
- [x] DONE — Kiểm thử đồng thời SQL Server cho F-03.

## 5. Documentation

- [x] DONE — `README.md`.
- [x] DONE — `docs/ARCHITECTURE.md`.
- [x] DONE — `docs/TASKS.md`.

## 6. Deployment

- [ ] TODO — Triển khai EC2.
- [ ] TODO — Deployment smoke test.
- [ ] TODO — Tạo release/tag.

## 7. Deferred / Out of Scope

- [~] DEFERRED / OUT OF SCOPE — Đăng nhập giảng viên.
- [~] DEFERRED / OUT OF SCOPE — Multi-school.
- [~] DEFERRED / OUT OF SCOPE — Kiểm tra xung đột thời khóa biểu.
- [~] DEFERRED / OUT OF SCOPE — Waitlist.
- [~] DEFERRED / OUT OF SCOPE — Học phí/thanh toán.
- [~] DEFERRED / OUT OF SCOPE — Điểm số.
- [~] DEFERRED / OUT OF SCOPE — Audit log.
- [~] DEFERRED / OUT OF SCOPE — Analytics.
- [~] DEFERRED / OUT OF SCOPE — Notification center.
- [~] DEFERRED / OUT OF SCOPE — Rate limiting.
