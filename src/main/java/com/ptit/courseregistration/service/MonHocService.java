package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.MonHocForm;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MonHocService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";

    private final MonHocRepository monHocRepository;
    private final KhoaRepository khoaRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public MonHocService(MonHocRepository monHocRepository,
                         KhoaRepository khoaRepository,
                         LopHocPhanRepository lopHocPhanRepository) {
        this.monHocRepository = monHocRepository;
        this.khoaRepository = khoaRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
    }

    @Transactional(readOnly = true)
    public List<MonHoc> findAll() {
        return initialize(monHocRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MonHoc findById(String maMonHoc) {
        String id = maMonHoc == null ? "" : maMonHoc.trim();
        MonHoc monHoc = monHocRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy môn học: " + id));
        return initialize(monHoc);
    }

    @Transactional(readOnly = true)
    public List<MonHoc> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String value = keyword.trim();
        return initialize(monHocRepository.findByMaMonHocContainingIgnoreCaseOrTenMonHocContainingIgnoreCase(
                value, value));
    }

    @Transactional
    public MonHoc create(MonHocForm form) {
        String maMonHoc = form.getMaMonHoc().trim();
        if (monHocRepository.existsById(maMonHoc)) {
            throw new BusinessException("Mã môn học đã tồn tại: " + maMonHoc);
        }

        Khoa khoa = findKhoa(form.getMaKhoa());
        MonHoc monHoc = new MonHoc();
        monHoc.setMaMonHoc(maMonHoc);
        monHoc.setTrangThai(ACTIVE);
        applyEditableFields(monHoc, form, khoa);
        return monHocRepository.save(monHoc);
    }

    @Transactional
    public MonHoc update(String maMonHoc, MonHocForm form) {
        MonHoc monHoc = findById(maMonHoc);
        String maKhoa = form.getMaKhoa().trim();
        Khoa currentKhoa = monHoc.getKhoa();
        Khoa khoa;
        if (maKhoa.equals(currentKhoa.getMaKhoa())) {
            khoa = currentKhoa;
        } else {
            khoa = findKhoa(maKhoa);
            if (!ACTIVE.equals(khoa.getTrangThai())) {
                throw new BusinessException("Không thể chuyển môn học sang khoa đang ngừng hoạt động.");
            }
        }

        if (!form.getSoTinChi().equals(monHoc.getSoTinChi())
                && !lopHocPhanRepository.findByMonHoc_MaMonHoc(monHoc.getMaMonHoc()).isEmpty()) {
            throw new BusinessException(
                    "Không thể thay đổi số tín chỉ vì môn học đã được sử dụng để mở lớp học phần.");
        }

        applyEditableFields(monHoc, form, khoa);
        return monHocRepository.save(monHoc);
    }

    @Transactional
    public void toggleStatus(String maMonHoc) {
        MonHoc monHoc = findById(maMonHoc);
        String currentStatus = monHoc.getTrangThai();
        if (ACTIVE.equals(currentStatus)) {
            monHoc.setTrangThai(INACTIVE);
        } else if (INACTIVE.equals(currentStatus)) {
            monHoc.setTrangThai(ACTIVE);
        } else {
            throw new BusinessException("Trạng thái môn học không hợp lệ: " + currentStatus);
        }
        monHocRepository.save(monHoc);
    }

    private Khoa findKhoa(String maKhoa) {
        String id = maKhoa.trim();
        return khoaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khoa: " + id));
    }

    private void applyEditableFields(MonHoc monHoc, MonHocForm form, Khoa khoa) {
        monHoc.setKhoa(khoa);
        monHoc.setTenMonHoc(form.getTenMonHoc().trim());
        monHoc.setSoTinChi(form.getSoTinChi());
        monHoc.setSoTietLyThuyet(form.getSoTietLyThuyet());
        monHoc.setSoTietThucHanh(form.getSoTietThucHanh());
        monHoc.setMoTa(blankToNull(form.getMoTa()));
    }

    private MonHoc initialize(MonHoc monHoc) {
        Hibernate.initialize(monHoc.getKhoa());
        return monHoc;
    }

    private List<MonHoc> initialize(List<MonHoc> monHocList) {
        monHocList.forEach(this::initialize);
        return monHocList;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
