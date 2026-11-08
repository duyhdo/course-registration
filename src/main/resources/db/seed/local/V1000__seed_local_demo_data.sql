INSERT INTO dbo.TAIKHOAN (TenDangNhap, MatKhauHash, VaiTro, TrangThai, NgayTao)
VALUES
    ('admin', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'ADMIN', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN001', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN002', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN003', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN004', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN005', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN006', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN007', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN008', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN009', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00'),
    ('B24DCCN010', '$2a$10$Uo0yold6fcEPZ68yTEnroeUK3G4IdaCF.ZndbMm.SR6Rgb9OkjD2q', 'STUDENT', 'ACTIVE', '2025-01-01T00:00:00');

INSERT INTO dbo.SINHVIEN (
    MaSV, TaiKhoanId, MaKhoa, HoTen, NgaySinh, GioiTinh, Email,
    SoDienThoai, DiaChi, KhoaHoc, TrangThai
)
SELECT v.MaSV, tk.TaiKhoanId, v.MaKhoa, v.HoTen, v.NgaySinh, v.GioiTinh,
       v.Email, v.SoDienThoai, v.DiaChi, v.KhoaHoc, 'ACTIVE'
FROM (VALUES
    ('B24DCCN001', 'CNTT', N'Nguyễn Minh Anh', CAST('2006-02-14' AS DATE), N'Nữ', 'b24dccn001@example.edu.vn', '0900000001', N'Hà Nội', 'K24'),
    ('B24DCCN002', 'CNTT', N'Trần Quốc Bảo', CAST('2006-05-21' AS DATE), N'Nam', 'b24dccn002@example.edu.vn', '0900000002', N'Hà Nội', 'K24'),
    ('B24DCCN003', 'CNTT', N'Lê Thu Hà', CAST('2006-08-09' AS DATE), N'Nữ', 'b24dccn003@example.edu.vn', '0900000003', N'Hải Phòng', 'K24'),
    ('B24DCCN004', 'DTVT', N'Phạm Đức Long', CAST('2006-01-30' AS DATE), N'Nam', 'b24dccn004@example.edu.vn', '0900000004', N'Nam Định', 'K24'),
    ('B24DCCN005', 'DTVT', N'Vũ Ngọc Mai', CAST('2006-11-17' AS DATE), N'Nữ', 'b24dccn005@example.edu.vn', '0900000005', N'Hà Nội', 'K24'),
    ('B24DCCN006', 'QTKD', N'Đặng Hoàng Nam', CAST('2006-03-06' AS DATE), N'Nam', 'b24dccn006@example.edu.vn', '0900000006', N'Thái Bình', 'K24'),
    ('B24DCCN007', 'QTKD', N'Bùi Khánh Ngọc', CAST('2006-07-12' AS DATE), N'Nữ', 'b24dccn007@example.edu.vn', '0900000007', N'Hà Nội', 'K24'),
    ('B24DCCN008', 'CNTT', N'Đỗ Gia Phúc', CAST('2006-04-25' AS DATE), N'Nam', 'b24dccn008@example.edu.vn', '0900000008', N'Bắc Ninh', 'K24'),
    ('B24DCCN009', 'DTVT', N'Ngô Hải Yến', CAST('2006-09-03' AS DATE), N'Nữ', 'b24dccn009@example.edu.vn', '0900000009', N'Hà Nội', 'K24'),
    ('B24DCCN010', 'QTKD', N'Hoàng Anh Tuấn', CAST('2006-12-28' AS DATE), N'Nam', 'b24dccn010@example.edu.vn', '0900000010', N'Ninh Bình', 'K24')
) AS v(MaSV, MaKhoa, HoTen, NgaySinh, GioiTinh, Email, SoDienThoai, DiaChi, KhoaHoc)
JOIN dbo.TAIKHOAN tk ON tk.TenDangNhap = v.MaSV;

INSERT INTO dbo.GIANGVIEN (MaGV, MaKhoa, HoTen, Email, SoDienThoai, HocVi, TrangThai)
VALUES
    ('GV-CNTT-01', 'CNTT', N'Nguyễn Văn Bình', 'binh.nguyen@example.edu.vn', '0911000001', N'Thạc sĩ', 'ACTIVE'),
    ('GV-CNTT-02', 'CNTT', N'Trần Thị Hương', 'huong.tran@example.edu.vn', '0911000002', N'Tiến sĩ', 'ACTIVE'),
    ('GV-DTVT-01', 'DTVT', N'Lê Quang Huy', 'huy.le@example.edu.vn', '0911000003', N'Thạc sĩ', 'ACTIVE'),
    ('GV-DTVT-02', 'DTVT', N'Phạm Thu Trang', 'trang.pham@example.edu.vn', '0911000004', N'Tiến sĩ', 'ACTIVE'),
    ('GV-QTKD-01', 'QTKD', N'Vũ Minh Đức', 'duc.vu@example.edu.vn', '0911000005', N'Thạc sĩ', 'ACTIVE'),
    ('GV-QTKD-02', 'QTKD', N'Đặng Thanh Tâm', 'tam.dang@example.edu.vn', '0911000006', N'Tiến sĩ', 'ACTIVE');

INSERT INTO dbo.MONHOC (
    MaMonHoc, MaKhoa, TenMonHoc, SoTinChi, SoTietLyThuyet, SoTietThucHanh, MoTa, TrangThai
)
VALUES
    ('MH-CNTT-01', 'CNTT', N'Nhập môn lập trình', 3, 30, 30, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-CNTT-02', 'CNTT', N'Cơ sở dữ liệu', 3, 30, 15, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-CNTT-03', 'CNTT', N'Phát triển ứng dụng web', 3, 30, 30, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-CNTT-04', 'CNTT', N'An toàn thông tin', 3, 30, 15, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-DTVT-01', 'DTVT', N'Mạng máy tính', 3, 30, 30, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-DTVT-02', 'DTVT', N'Kỹ thuật truyền thông', 2, 30, 0, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-QTKD-01', 'QTKD', N'Nguyên lý kế toán', 3, 30, 15, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-QTKD-02', 'QTKD', N'Marketing căn bản', 3, 30, 15, N'Môn học minh họa local.', 'ACTIVE'),
    ('MH-QTKD-03', 'QTKD', N'Kinh tế vi mô', 2, 30, 0, N'Môn học minh họa local.', 'ACTIVE');

INSERT INTO dbo.HOCKY (
    MaHocKy, TenHocKy, NamHoc, NgayBatDau, NgayKetThuc,
    NgayMoDangKy, NgayDongDangKy, TinChiToiThieu, TinChiToiDa
)
VALUES (
    'HK2025-1', N'Học kỳ 1', '2025-2026', '2025-08-01', '2099-12-31',
    '2020-01-01T00:00:00', '2099-12-31T23:59:59', 12, 30
);

INSERT INTO dbo.LOPHOCPHAN (
    MaLopHocPhan, MaMonHoc, MaGV, MaHocKy, PhongHoc, ThuHoc,
    TietBatDau, TietKetThuc, SiSoToiDa, SiSoHienTai, TrangThai
)
VALUES
    ('LHP-CNTT-01', 'MH-CNTT-01', 'GV-CNTT-01', 'HK2025-1', N'A101', 2, 1, 3, 30, 2, 'OPEN'),
    ('LHP-CNTT-02', 'MH-CNTT-02', 'GV-CNTT-02', 'HK2025-1', N'A102', 3, 4, 6, 30, 1, 'OPEN'),
    ('LHP-CNTT-03', 'MH-CNTT-03', 'GV-CNTT-01', 'HK2025-1', N'A103', 4, 7, 9, 25, 2, 'OPEN'),
    ('LHP-CNTT-04', 'MH-CNTT-04', 'GV-CNTT-02', 'HK2025-1', N'A104', 5, 1, 3, 25, 0, 'DRAFT'),
    ('LHP-DTVT-01', 'MH-DTVT-01', 'GV-DTVT-01', 'HK2025-1', N'B201', 2, 4, 6, 30, 1, 'OPEN'),
    ('LHP-DTVT-02', 'MH-DTVT-02', NULL, 'HK2025-1', N'B202', 3, 7, 9, 30, 1, 'OPEN'),
    ('LHP-DTVT-03', 'MH-DTVT-02', 'GV-DTVT-02', 'HK2025-1', N'B203', 4, 1, 3, 25, 0, 'CLOSED'),
    ('LHP-QTKD-01', 'MH-QTKD-01', 'GV-QTKD-01', 'HK2025-1', N'C301', 2, 7, 9, 30, 1, 'OPEN'),
    ('LHP-QTKD-02', 'MH-QTKD-02', 'GV-QTKD-02', 'HK2025-1', N'C302', 5, 4, 6, 30, 0, 'OPEN'),
    ('LHP-QTKD-03', 'MH-QTKD-03', 'GV-QTKD-01', 'HK2025-1', N'C303', 6, 1, 3, 25, 0, 'CANCELLED');

INSERT INTO dbo.DANGKYHOCPHAN (MaSV, MaLopHocPhan, NgayDangKy, TrangThai)
VALUES
    ('B24DCCN001', 'LHP-CNTT-01', '2025-08-15T08:00:00', 'REGISTERED'),
    ('B24DCCN002', 'LHP-CNTT-01', '2025-08-15T08:05:00', 'REGISTERED'),
    ('B24DCCN002', 'LHP-CNTT-02', '2025-08-15T08:10:00', 'REGISTERED'),
    ('B24DCCN001', 'LHP-CNTT-03', '2025-08-15T08:15:00', 'REGISTERED'),
    ('B24DCCN003', 'LHP-CNTT-03', '2025-08-15T08:20:00', 'REGISTERED'),
    ('B24DCCN004', 'LHP-DTVT-01', '2025-08-15T08:25:00', 'REGISTERED'),
    ('B24DCCN005', 'LHP-DTVT-02', '2025-08-15T08:30:00', 'REGISTERED'),
    ('B24DCCN006', 'LHP-QTKD-01', '2025-08-15T08:35:00', 'REGISTERED');

INSERT INTO dbo.DANGKYHOCPHAN (MaSV, MaLopHocPhan, NgayDangKy, NgayHuy, TrangThai)
VALUES (
    'B24DCCN001', 'LHP-DTVT-01', '2025-08-15T08:40:00',
    '2025-08-16T09:00:00', 'CANCELLED'
);
