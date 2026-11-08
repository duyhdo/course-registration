package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminEnrollmentService {

    private static final String REGISTERED = "REGISTERED";
    private static final String CANCELLED = "CANCELLED";
    private static final String ALL = "ALL";

    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public AdminEnrollmentService(DangKyHocPhanRepository dangKyHocPhanRepository,
                                  LopHocPhanRepository lopHocPhanRepository) {
        this.dangKyHocPhanRepository = dangKyHocPhanRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
    }

    @Transactional(readOnly = true)
    public Page<DangKyHocPhan> search(String keyword, String maHocKy,
                                      String maLopHocPhan, String trangThai, Pageable pageable) {
        return dangKyHocPhanRepository.searchForAdmin(
                blankToNull(keyword),
                blankToNull(maHocKy),
                blankToNull(maLopHocPhan),
                blankToNull(trangThai),
                pageable);
    }

    @Transactional(readOnly = true)
    public List<DangKyHocPhan> findByCourseSection(String maLopHocPhan, String trangThai) {
        String sectionId = requiredTrimmed(maLopHocPhan);
        requireCourseSection(sectionId);

        String status = blankToNull(trangThai);
        if (status == null) {
            status = REGISTERED;
        } else if (!ALL.equals(status) && !REGISTERED.equals(status) && !CANCELLED.equals(status)) {
            throw new BusinessException("Trạng thái đăng ký không được hỗ trợ.");
        }

        return dangKyHocPhanRepository.findForAdminByCourseSection(
                sectionId, ALL.equals(status) ? null : status);
    }

    @Transactional(readOnly = true)
    public long countRegisteredByCourseSection(String maLopHocPhan) {
        String sectionId = requiredTrimmed(maLopHocPhan);
        requireCourseSection(sectionId);
        return dangKyHocPhanRepository
                .countByLopHocPhan_MaLopHocPhanAndTrangThai(sectionId, REGISTERED);
    }

    private void requireCourseSection(String maLopHocPhan) {
        if (!lopHocPhanRepository.existsById(maLopHocPhan)) {
            throw new BusinessException("Lớp học phần không tồn tại.");
        }
    }

    private String requiredTrimmed(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Mã lớp học phần không được để trống.");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
