# Architecture

## 1. System Context

```text
Browser → Spring MVC → Service → Repository/JPA → SQL Server
```

Trình duyệt gửi yêu cầu HTTP; Spring MVC định tuyến yêu cầu và trả trang Thymeleaf hoặc kết quả chuyển hướng. Dịch vụ thực thi quy tắc ứng dụng, còn repository/JPA đọc và ghi dữ liệu trong SQL Server.

## 2. Application Architecture

```text
Controller → Service → Repository → Entity / SQL Server
```

- **Controller:** nhận request, kiểm tra đầu vào web, gọi service và chuẩn bị model/view.
- **Service:** thực thi quy tắc nghiệp vụ và ranh giới transaction.
- **Repository:** truy vấn dữ liệu, bao gồm các truy vấn khóa pessimistic dùng cho luồng đồng thời.
- **Entity/JPA:** ánh xạ tám bảng; Flyway quản lý schema và Hibernate kiểm tra tương thích khi khởi động (`ddl-auto=validate`).
- **Flyway:** các migration lõi trong `src/main/resources/db/migration/`; profile `local` bổ sung seed riêng tại `src/main/resources/db/seed/local/`.

## 3. Domain Model

Mô hình gồm đúng tám entity:

| Entity | Vai trò và liên kết được ánh xạ |
|---|---|
| `TaiKhoan` | Thông tin đăng nhập, hash mật khẩu, vai trò và trạng thái. Entity không khai báo quan hệ JPA tới `GiangVien` hoặc `SinhVien`. |
| `Khoa` | Đơn vị khoa; được tham chiếu bởi `SinhVien`, `GiangVien` và `MonHoc`. |
| `SinhVien` | Hồ sơ sinh viên; liên kết bắt buộc tới `Khoa`, liên kết tùy chọn một-một tới `TaiKhoan`. |
| `GiangVien` | Hồ sơ giảng viên; liên kết bắt buộc tới `Khoa`. Không có quan hệ tới `TaiKhoan`. |
| `MonHoc` | Môn học; liên kết bắt buộc tới `Khoa`. |
| `HocKy` | Thông tin học kỳ, thời gian đăng ký và giới hạn tín chỉ. Không có trường `TrangThai`. |
| `LopHocPhan` | Lớp học phần; liên kết bắt buộc tới `MonHoc` và `HocKy`, tùy chọn tới `GiangVien`. |
| `DangKyHocPhan` | Bản ghi đăng ký; liên kết bắt buộc tới `SinhVien` và `LopHocPhan`, lưu trạng thái cùng thời điểm đăng ký/hủy. |

Entity chỉ khai báo các quan hệ đơn hướng như trong ánh xạ hiện tại; không suy diễn inverse collections.

## 4. Entity Relationships / Cardinality

```text
Khoa 1 -- 0..* SinhVien
Khoa 1 -- 0..* GiangVien
Khoa 1 -- 0..* MonHoc
TaiKhoan 0..1 -- 0..1 SinhVien
MonHoc 1 -- 0..* LopHocPhan
HocKy 1 -- 0..* LopHocPhan
GiangVien 0..1 -- 0..* LopHocPhan
SinhVien 1 -- 0..* DangKyHocPhan
LopHocPhan 1 -- 0..* DangKyHocPhan
```

## 5. Authentication & Authorization

Spring Security xử lý đăng nhập form và đăng xuất. Vai trò được giới hạn như sau:

- `ADMIN` truy cập `/admin/**`.
- `STUDENT` truy cập `/student/**`.
- Anonymous có thể mở các trang đăng nhập và khôi phục mật khẩu; yêu cầu còn lại cần xác thực.

Không có tài khoản hoặc luồng đăng nhập giảng viên.

## 6. Registration Flow

`DangKyHocPhanService.register` chạy trong `@Transactional`. Thứ tự hiện tại:

1. Khóa `SinhVien`, kiểm tra trạng thái `ACTIVE`.
2. Khóa `LopHocPhan`, kiểm tra trạng thái `OPEN`.
3. Kiểm tra cửa sổ đăng ký và cấu hình tín chỉ của `HocKy`; kiểm tra dữ liệu tín chỉ môn học hợp lệ.
4. Từ chối nếu đã có bản ghi `REGISTERED` cho lớp, hoặc đã đăng ký lớp khác của cùng môn trong cùng học kỳ.
5. Kiểm tra sức chứa và tổng tín chỉ không vượt giới hạn tối đa.
6. Tạo bản ghi `REGISTERED`, tăng `SiSoHienTai`; các thay đổi được commit khi transaction thành công.

## 7. Cancellation Flow

`DangKyHocPhanService.cancel` chạy trong `@Transactional`:

1. Khóa `LopHocPhan`.
2. Kiểm tra lớp đang `OPEN` và còn trong cửa sổ đăng ký.
3. Tìm bản ghi đang `REGISTERED`, kiểm tra sĩ số hợp lệ.
4. Chuyển bản ghi sang `CANCELLED`, ghi `NgayHuy`, giảm `SiSoHienTai`.
5. Commit các thay đổi khi transaction thành công.

## 8. Course Section Lifecycle

Vòng đời được hỗ trợ:

```text
DRAFT → OPEN → CLOSED
DRAFT / OPEN → CANCELLED
```

Không thể hủy lớp nếu còn enrollment `REGISTERED`. Hiện không hỗ trợ hủy hàng loạt hoặc chuyển sinh viên sang lớp khác.

## 9. Re-registration Design

Sau khi hủy, bản ghi cũ giữ `CANCELLED`. Đăng ký lại cùng lớp tạo bản ghi mới `REGISTERED`, không ghi đè lịch sử. Migration `V2` tạo filtered unique index SQL Server `UX_DANGKYHOCPHAN_ACTIVE` trên `(MaSV, MaLopHocPhan)` với điều kiện `TrangThai = 'REGISTERED'`; đây không phải unique constraint không điều kiện. Cấu trúc index đã được đối chiếu với metadata của SQL Server đang cấu hình.

## 10. Concurrency Strategy

- **F-05:** đăng ký khóa theo thứ tự `SinhVien` rồi `LopHocPhan` để tuần tự hóa đăng ký đồng thời của cùng sinh viên ở các lớp khác nhau.
- **F-04:** hủy lớp từ Admin khóa `LopHocPhan` trước khi kiểm tra enrollment `REGISTERED`.
- **F-03:** kiểm thử đồng thời trên SQL Server không tái hiện double-cancellation/double-decrement; khóa `LopHocPhan` hiện có tuần tự hóa luồng hủy trong kịch bản kiểm thử.
- Không tìm thấy thứ tự khóa ngược `LopHocPhan → SinhVien` trong các luồng nghiệp vụ đã xem xét. Điều này không khẳng định deadlock là bất khả thi.

## 11. Transaction Boundaries

Các thao tác ghi đăng ký, hủy đăng ký, thay đổi vòng đời lớp và cập nhật nghiệp vụ được bao trong các phương thức `@Transactional` ở service. Các phương thức truy vấn được đánh dấu `@Transactional(readOnly = true)` tại những nơi phù hợp. Ranh giới transaction cụ thể được khai báo tại từng phương thức service.

## 12. Password Reset

Khôi phục mật khẩu dùng mã gửi qua email và trạng thái xác nhận được lưu trong `HttpSession`. Mã có thời hạn 10 phút; mật khẩu mới được mã hóa bằng BCrypt. Lưu trạng thái mã trong session là thiết kế hiện tại được chấp nhận; không có lưu trữ mã khôi phục bền vững trong database.

## 13. UI Architecture

UI dùng Thymeleaf, Bootstrap và stylesheet dùng chung `static/css/app.css`. Các màn hình đã xác thực dùng chung hệ thống trình bày gồm header, sidebar trái và vùng nội dung chính cho cả Admin và Student. Các trang xác thực công khai không dùng dashboard shell.

## 14. Testing Strategy

- Unit/service tests cho nghiệp vụ và DTO validation.
- `CourseRegistrationApplicationTests` kiểm tra khởi tạo ứng dụng.
- `DangKyHocPhanConcurrencyIntegrationTest` kiểm tra đồng thời trên SQL Server local.
- Baseline đã xác nhận: 264 test trong regression suite loại trừ `CourseRegistrationApplicationTests`; 265 test trong clean local-profile suite. Không có số liệu coverage được báo cáo.

## 15. Deployment View

```text
Browser → Spring Boot application → SQL Server
```

Triển khai EC2 chưa được xác nhận là hoàn tất; cần thực hiện triển khai và smoke test riêng.

## 16. Database Bootstrap and Migration

- `db/bootstrap/00_create_database.sql` tạo `QL_DangKyHocPhan` nếu database chưa tồn tại; script không xóa database hiện có.
- Flyway chạy các migration lõi `V1`–`V3` trong mọi profile.
- Profile `local` bổ sung `V1000` với dữ liệu demo giả lập; migration này không nằm trong đường dẫn mặc định.
- Hibernate xác thực schema sau khi Flyway chạy; schema mismatch làm ứng dụng khởi động thất bại thay vì âm thầm cập nhật database.
- Dữ liệu demo gồm tài khoản `admin`, sinh viên `B24DCCN001`–`B24DCCN010`, dữ liệu môn/lớp và bản ghi đăng ký. Mật khẩu demo `Demo@123` chỉ dành cho local.
- Lần khởi tạo Flyway yêu cầu database rỗng. Database đã có schema nhưng chưa có Flyway history cần được baseline/migrate riêng; không tự động áp dụng V1 lên schema hiện có.
- Đã kiểm tra khởi tạo từ database rỗng với cả bốn migration, login seed, sĩ số đăng ký và filtered index; hai test suite lần lượt có 264 và 265 test pass.

## 17. Known Limitations

Phạm vi hiện tại:

- Một trường/cơ sở; không có hỗ trợ multi-school.
- Không có đăng nhập giảng viên.
- Không có kiểm tra xung đột thời khóa biểu hoặc waitlist.
- Không có học phí/thanh toán hoặc điểm số.
- Không có analytics, audit log hoặc notification center.
- Chưa có rate limiting cho đăng nhập/khôi phục mật khẩu.
