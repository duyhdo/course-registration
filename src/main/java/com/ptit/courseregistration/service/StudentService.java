package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.StudentForm;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.entity.TaiKhoan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.SinhVienRepository;
import com.ptit.courseregistration.repository.TaiKhoanRepository;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    private static final String ACTIVE = "ACTIVE";
    private static final String LOCKED = "LOCKED";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String DEFAULT_PASSWORD = "Student@123";

    private final SinhVienRepository sinhVienRepository;
    private final KhoaRepository khoaRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(SinhVienRepository sinhVienRepository,
                          KhoaRepository khoaRepository,
                          TaiKhoanRepository taiKhoanRepository,
                          PasswordEncoder passwordEncoder) {
        this.sinhVienRepository = sinhVienRepository;
        this.khoaRepository = khoaRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<SinhVien> findAll() {
        return initialize(sinhVienRepository.findAll());
    }

    @Transactional(readOnly = true)
    public SinhVien findById(String maSV) {
        SinhVien sv = sinhVienRepository.findById(maSV == null ? "" : maSV.trim())
                .orElseThrow(() -> new BusinessException("Student not found: " + maSV));
        return initialize(sv);
    }

    @Transactional(readOnly = true)
    public List<SinhVien> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String k = keyword.trim();
        return initialize(sinhVienRepository.findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(k, k));
    }

    @Transactional
    public SinhVien create(StudentForm form) {
        String maSV = form.getMaSV().trim();
        String email = form.getEmail().trim();

        if (sinhVienRepository.existsById(maSV)) {
            throw new BusinessException("Student ID already exists: " + maSV);
        }
        if (sinhVienRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists: " + email);
        }
        Khoa khoa = findKhoa(form.getMaKhoa());
        if (taiKhoanRepository.existsByTenDangNhap(maSV)) {
            throw new BusinessException("Username already exists: " + maSV);
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(maSV);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        taiKhoan.setVaiTro(ROLE_STUDENT);
        taiKhoan.setTrangThai(ACTIVE);
        taiKhoan.setNgayTao(LocalDateTime.now());
        taiKhoan.setNgayCapNhat(null);
        taiKhoan = taiKhoanRepository.save(taiKhoan);

        SinhVien sv = new SinhVien();
        sv.setMaSV(maSV);
        sv.setTrangThai(ACTIVE);
        sv.setTaiKhoan(taiKhoan);
        applyEditableFields(sv, form, khoa);
        return sinhVienRepository.save(sv);
    }

    @Transactional
    public SinhVien update(String maSV, StudentForm form) {
        SinhVien sv = findById(maSV);
        String email = form.getEmail().trim();

        if (sinhVienRepository.existsByEmailAndMaSVNot(email, sv.getMaSV())) {
            throw new BusinessException("Email already exists: " + email);
        }
        Khoa khoa = findKhoa(form.getMaKhoa());

        applyEditableFields(sv, form, khoa);
        return sinhVienRepository.save(sv);
    }

    @Transactional
    public void toggleStatus(String maSV) {
        SinhVien sv = findById(maSV);
        String current = sv.getTrangThai();
        String next;
        if (ACTIVE.equals(current)) {
            next = LOCKED;
        } else if (LOCKED.equals(current)) {
            next = ACTIVE;
        } else {
            throw new BusinessException("Unsupported student status: " + current);
        }

        sv.setTrangThai(next);
        sinhVienRepository.save(sv);

        TaiKhoan taiKhoan = sv.getTaiKhoan();
        if (taiKhoan != null) {
            taiKhoan.setTrangThai(next);
            taiKhoan.setNgayCapNhat(LocalDateTime.now());
            taiKhoanRepository.save(taiKhoan);
        }
    }

    // open-in-view is disabled, so lazy associations used by views must be loaded here
    private SinhVien initialize(SinhVien sv) {
        Hibernate.initialize(sv.getKhoa());
        Hibernate.initialize(sv.getTaiKhoan());
        return sv;
    }

    private List<SinhVien> initialize(List<SinhVien> list) {
        list.forEach(this::initialize);
        return list;
    }

    private Khoa findKhoa(String maKhoa) {
        String id = maKhoa.trim();
        return khoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Faculty not found: " + id));
    }

    private void applyEditableFields(SinhVien sv, StudentForm form, Khoa khoa) {
        sv.setKhoa(khoa);
        sv.setHoTen(form.getHoTen().trim());
        sv.setNgaySinh(form.getNgaySinh());
        sv.setGioiTinh(blankToNull(form.getGioiTinh()));
        sv.setEmail(form.getEmail().trim());
        sv.setSoDienThoai(blankToNull(form.getSoDienThoai()));
        sv.setDiaChi(blankToNull(form.getDiaChi()));
        sv.setKhoaHoc(blankToNull(form.getKhoaHoc()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
