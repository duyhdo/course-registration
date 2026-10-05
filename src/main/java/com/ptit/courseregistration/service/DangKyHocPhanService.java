package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.SinhVienRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DangKyHocPhanService {

    private static final String ACTIVE = "ACTIVE";
    private static final String OPEN = "OPEN";
    private static final String REGISTERED = "REGISTERED";
    private static final String CANCELLED = "CANCELLED";

    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final SinhVienRepository sinhVienRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public DangKyHocPhanService(DangKyHocPhanRepository dangKyHocPhanRepository,
                                SinhVienRepository sinhVienRepository,
                                LopHocPhanRepository lopHocPhanRepository) {
        this.dangKyHocPhanRepository = dangKyHocPhanRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
    }

    @Transactional
    public DangKyHocPhan register(String maSV, String maLopHocPhan) {
        SinhVien sinhVien = findSinhVien(maSV);
        if (!ACTIVE.equals(sinhVien.getTrangThai())) {
            throw new BusinessException("Sinh viên đang không hoạt động.");
        }

        LopHocPhan lopHocPhan = findCourseSectionForUpdate(maLopHocPhan);
        if (!OPEN.equals(lopHocPhan.getTrangThai())) {
            throw new BusinessException("Lớp học phần hiện không mở đăng ký.");
        }

        MonHoc monHoc = lopHocPhan.getMonHoc();
        HocKy hocKy = lopHocPhan.getHocKy();
        LocalDateTime now = LocalDateTime.now();
        validateRegistrationWindow(hocKy, now);

        Integer maxCredits = hocKy.getTinChiToiDa();
        if (maxCredits == null) {
            throw new BusinessException("Học kỳ chưa cấu hình giới hạn tín chỉ.");
        }
        Integer subjectCredits = monHoc.getSoTinChi();
        if (subjectCredits == null || subjectCredits <= 0) {
            throw new BusinessException("Dữ liệu môn học không hợp lệ: số tín chỉ phải lớn hơn 0.");
        }

        String studentId = sinhVien.getMaSV();
        String sectionId = lopHocPhan.getMaLopHocPhan();
        String semesterId = hocKy.getMaHocKy();
        String subjectId = monHoc.getMaMonHoc();
        if (dangKyHocPhanRepository.existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(
                studentId, sectionId, REGISTERED)) {
            throw new BusinessException("Sinh viên đã đăng ký lớp học phần này.");
        }

        if (dangKyHocPhanRepository.countRegisteredForSubjectInSemester(
                studentId, subjectId, semesterId) > 0) {
            throw new BusinessException(
                    "Sinh viên đã đăng ký một lớp khác của môn học này trong cùng học kỳ.");
        }

        Integer currentCapacity = lopHocPhan.getSiSoHienTai();
        Integer maxCapacity = lopHocPhan.getSiSoToiDa();
        if (currentCapacity == null || maxCapacity == null
                || currentCapacity < 0 || currentCapacity > maxCapacity) {
            throw new BusinessException("Sĩ số lớp học phần không hợp lệ.");
        }
        if (currentCapacity >= maxCapacity) {
            throw new BusinessException("Lớp học phần đã đủ sĩ số.");
        }

        long currentCredits = dangKyHocPhanRepository.sumRegisteredCredits(studentId, semesterId);
        if (currentCredits + subjectCredits > maxCredits) {
            throw new BusinessException("Đăng ký vượt quá số tín chỉ tối đa của học kỳ.");
        }

        DangKyHocPhan dangKy = new DangKyHocPhan();
        dangKy.setSinhVien(sinhVien);
        dangKy.setLopHocPhan(lopHocPhan);
        dangKy.setNgayDangKy(now);
        dangKy.setNgayHuy(null);
        dangKy.setTrangThai(REGISTERED);

        lopHocPhan.setSiSoHienTai(currentCapacity + 1);
        return dangKyHocPhanRepository.save(dangKy);
    }

    @Transactional
    public DangKyHocPhan cancel(String maSV, String maLopHocPhan) {
        SinhVien sinhVien = findSinhVien(maSV);
        LopHocPhan lopHocPhan = findCourseSectionForUpdate(maLopHocPhan);
        if (!OPEN.equals(lopHocPhan.getTrangThai())) {
            throw new BusinessException("Lớp học phần hiện không mở đăng ký.");
        }

        LocalDateTime now = LocalDateTime.now();
        validateRegistrationWindow(lopHocPhan.getHocKy(), now);

        DangKyHocPhan dangKy = dangKyHocPhanRepository
                .findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
                        sinhVien.getMaSV(), lopHocPhan.getMaLopHocPhan(), REGISTERED)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đăng ký đang hoạt động để hủy."));

        Integer currentCapacity = lopHocPhan.getSiSoHienTai();
        Integer maxCapacity = lopHocPhan.getSiSoToiDa();
        if (currentCapacity == null || maxCapacity == null
                || currentCapacity <= 0 || currentCapacity > maxCapacity) {
            throw new BusinessException("Sĩ số lớp học phần không hợp lệ, không thể hủy đăng ký.");
        }

        dangKy.setTrangThai(CANCELLED);
        dangKy.setNgayHuy(now);
        lopHocPhan.setSiSoHienTai(currentCapacity - 1);
        return dangKy;
    }

    @Transactional(readOnly = true)
    public List<DangKyHocPhan> findStudentHistory(String maSV) {
        SinhVien sinhVien = findSinhVien(maSV);
        return initialize(dangKyHocPhanRepository
                .findBySinhVien_MaSVOrderByNgayDangKyDesc(sinhVien.getMaSV()));
    }

    @Transactional(readOnly = true)
    public List<DangKyHocPhan> findActiveRegistrations(String maSV) {
        SinhVien sinhVien = findSinhVien(maSV);
        return initialize(dangKyHocPhanRepository
                .findBySinhVien_MaSVAndTrangThaiIn(sinhVien.getMaSV(), List.of(REGISTERED)));
    }

    private SinhVien findSinhVien(String maSV) {
        String id = maSV == null ? "" : maSV.trim();
        return sinhVienRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sinh viên không tồn tại."));
    }

    private LopHocPhan findCourseSectionForUpdate(String maLopHocPhan) {
        String id = maLopHocPhan == null ? "" : maLopHocPhan.trim();
        return lopHocPhanRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Lớp học phần không tồn tại."));
    }

    private void validateRegistrationWindow(HocKy hocKy, LocalDateTime now) {
        LocalDateTime opensAt = hocKy.getNgayMoDangKy();
        LocalDateTime closesAt = hocKy.getNgayDongDangKy();
        if (opensAt == null || closesAt == null) {
            throw new BusinessException("Học kỳ chưa cấu hình thời gian đăng ký.");
        }
        if (now.isBefore(opensAt) || now.isAfter(closesAt)) {
            throw new BusinessException("Hiện không nằm trong thời gian đăng ký học phần.");
        }
    }

    private DangKyHocPhan initialize(DangKyHocPhan dangKy) {
        Hibernate.initialize(dangKy.getSinhVien());
        LopHocPhan lopHocPhan = dangKy.getLopHocPhan();
        Hibernate.initialize(lopHocPhan);
        MonHoc monHoc = lopHocPhan.getMonHoc();
        Hibernate.initialize(monHoc);
        Hibernate.initialize(monHoc.getKhoa());
        Hibernate.initialize(lopHocPhan.getHocKy());
        if (lopHocPhan.getGiangVien() != null) {
            Hibernate.initialize(lopHocPhan.getGiangVien());
        }
        return dangKy;
    }

    private List<DangKyHocPhan> initialize(List<DangKyHocPhan> registrations) {
        registrations.forEach(this::initialize);
        return registrations;
    }
}
