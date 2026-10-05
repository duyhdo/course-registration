package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.LopHocPhanForm;
import com.ptit.courseregistration.entity.GiangVien;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.GiangVienRepository;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LopHocPhanService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DRAFT = "DRAFT";
    private static final String OPEN = "OPEN";
    private static final String CLOSED = "CLOSED";
    private static final String CANCELLED = "CANCELLED";

    private final LopHocPhanRepository lopHocPhanRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final HocKyRepository hocKyRepository;

    public LopHocPhanService(LopHocPhanRepository lopHocPhanRepository,
                             MonHocRepository monHocRepository,
                             GiangVienRepository giangVienRepository,
                             HocKyRepository hocKyRepository) {
        this.lopHocPhanRepository = lopHocPhanRepository;
        this.monHocRepository = monHocRepository;
        this.giangVienRepository = giangVienRepository;
        this.hocKyRepository = hocKyRepository;
    }

    @Transactional(readOnly = true)
    public List<LopHocPhan> findAll() {
        return initialize(lopHocPhanRepository.findAll());
    }

    @Transactional(readOnly = true)
    public LopHocPhan findById(String maLopHocPhan) {
        String id = maLopHocPhan == null ? "" : maLopHocPhan.trim();
        LopHocPhan lopHocPhan = lopHocPhanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lớp học phần: " + id));
        return initialize(lopHocPhan);
    }

    @Transactional(readOnly = true)
    public List<LopHocPhan> search(String keyword, String maHocKy, String trangThai) {
        return initialize(lopHocPhanRepository.findByFilters(
                blankToNull(keyword), blankToNull(maHocKy), blankToNull(trangThai)));
    }

    @Transactional
    public LopHocPhan create(LopHocPhanForm form) {
        validateSchedule(form);
        validateCapacity(form.getSiSoToiDa());

        String id = requiredTrimmed(form.getMaLopHocPhan(), "Mã lớp học phần không được để trống.");
        if (lopHocPhanRepository.existsById(id)) {
            throw new BusinessException("Mã lớp học phần đã tồn tại: " + id);
        }

        MonHoc monHoc = findMonHoc(form.getMaMonHoc());
        requireActiveSubject(monHoc);
        HocKy hocKy = findHocKy(form.getMaHocKy());
        GiangVien giangVien = findOptionalGiangVien(form.getMaGV());

        LopHocPhan lopHocPhan = new LopHocPhan();
        lopHocPhan.setMaLopHocPhan(id);
        lopHocPhan.setMonHoc(monHoc);
        lopHocPhan.setGiangVien(giangVien);
        lopHocPhan.setHocKy(hocKy);
        applyEditableFields(lopHocPhan, form);
        lopHocPhan.setSiSoHienTai(0);
        lopHocPhan.setTrangThai(DRAFT);
        return lopHocPhanRepository.save(lopHocPhan);
    }

    @Transactional
    public LopHocPhan update(String maLopHocPhan, LopHocPhanForm form) {
        LopHocPhan lopHocPhan = findById(maLopHocPhan);
        if (CLOSED.equals(lopHocPhan.getTrangThai()) || CANCELLED.equals(lopHocPhan.getTrangThai())) {
            throw new BusinessException("Không thể cập nhật lớp học phần đã đóng hoặc đã hủy.");
        }

        validateSchedule(form);
        validateCapacity(form.getSiSoToiDa());
        if (form.getSiSoToiDa() < lopHocPhan.getSiSoHienTai()) {
            throw new BusinessException("Sĩ số tối đa không thể nhỏ hơn sĩ số hiện tại.");
        }

        String maMonHoc = requiredTrimmed(form.getMaMonHoc(), "Mã môn học không được để trống.");
        String maHocKy = requiredTrimmed(form.getMaHocKy(), "Mã học kỳ không được để trống.");
        boolean open = OPEN.equals(lopHocPhan.getTrangThai());
        if (open && !maMonHoc.equals(lopHocPhan.getMonHoc().getMaMonHoc())) {
            throw new BusinessException("Không thể thay đổi môn học khi lớp học phần đã mở.");
        }
        if (open && !maHocKy.equals(lopHocPhan.getHocKy().getMaHocKy())) {
            throw new BusinessException("Không thể thay đổi học kỳ khi lớp học phần đã mở.");
        }

        MonHoc monHoc;
        if (open || maMonHoc.equals(lopHocPhan.getMonHoc().getMaMonHoc())) {
            monHoc = lopHocPhan.getMonHoc();
        } else {
            monHoc = findMonHoc(maMonHoc);
            requireActiveSubject(monHoc);
        }

        HocKy hocKy = open ? lopHocPhan.getHocKy() : findHocKy(maHocKy);
        GiangVien giangVien = findOptionalGiangVien(form.getMaGV());

        lopHocPhan.setMonHoc(monHoc);
        lopHocPhan.setHocKy(hocKy);
        lopHocPhan.setGiangVien(giangVien);
        applyEditableFields(lopHocPhan, form);
        return lopHocPhanRepository.save(lopHocPhan);
    }

    @Transactional
    public void open(String maLopHocPhan) {
        LopHocPhan lopHocPhan = findById(maLopHocPhan);
        requireState(lopHocPhan, DRAFT, "Chỉ lớp học phần ở trạng thái DRAFT mới được mở.");

        MonHoc monHoc = findMonHoc(lopHocPhan.getMonHoc().getMaMonHoc());
        requireActiveSubject(monHoc);
        findHocKy(lopHocPhan.getHocKy().getMaHocKy());

        GiangVien giangVien = lopHocPhan.getGiangVien();
        if (giangVien == null) {
            throw new BusinessException("Cần phân công giảng viên trước khi mở lớp học phần.");
        }
        giangVien = findGiangVien(giangVien.getMaGV());
        requireActiveTeacher(giangVien);

        validateCapacity(lopHocPhan.getSiSoToiDa());
        validateScheduleForOpening(lopHocPhan);

        lopHocPhan.setTrangThai(OPEN);
        lopHocPhanRepository.save(lopHocPhan);
    }

    @Transactional
    public void close(String maLopHocPhan) {
        LopHocPhan lopHocPhan = findById(maLopHocPhan);
        requireState(lopHocPhan, OPEN, "Chỉ lớp học phần ở trạng thái OPEN mới được đóng.");
        lopHocPhan.setTrangThai(CLOSED);
        lopHocPhanRepository.save(lopHocPhan);
    }

    @Transactional
    public void cancel(String maLopHocPhan) {
        LopHocPhan lopHocPhan = findById(maLopHocPhan);
        if (!DRAFT.equals(lopHocPhan.getTrangThai()) && !OPEN.equals(lopHocPhan.getTrangThai())) {
            throw new BusinessException("Chỉ lớp học phần ở trạng thái DRAFT hoặc OPEN mới được hủy.");
        }
        lopHocPhan.setTrangThai(CANCELLED);
        lopHocPhanRepository.save(lopHocPhan);
    }

    private MonHoc findMonHoc(String maMonHoc) {
        String id = requiredTrimmed(maMonHoc, "Mã môn học không được để trống.");
        return monHocRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy môn học: " + id));
    }

    private HocKy findHocKy(String maHocKy) {
        String id = requiredTrimmed(maHocKy, "Mã học kỳ không được để trống.");
        return hocKyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy học kỳ: " + id));
    }

    private GiangVien findGiangVien(String maGV) {
        String id = requiredTrimmed(maGV, "Mã giảng viên không được để trống.");
        return giangVienRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên: " + id));
    }

    private GiangVien findOptionalGiangVien(String maGV) {
        return maGV == null || maGV.isBlank() ? null : findGiangVien(maGV);
    }

    private void requireActiveSubject(MonHoc monHoc) {
        if (!ACTIVE.equals(monHoc.getTrangThai())) {
            throw new BusinessException("Chỉ môn học đang hoạt động mới được sử dụng cho lớp học phần.");
        }
    }

    private void requireActiveTeacher(GiangVien giangVien) {
        if (!ACTIVE.equals(giangVien.getTrangThai())) {
            throw new BusinessException("Giảng viên phải đang hoạt động trước khi mở lớp học phần.");
        }
    }

    private void requireState(LopHocPhan lopHocPhan, String expected, String message) {
        if (!expected.equals(lopHocPhan.getTrangThai())) {
            throw new BusinessException(message);
        }
    }

    private void validateSchedule(LopHocPhanForm form) {
        Integer thuHoc = form.getThuHoc();
        if (thuHoc != null && (thuHoc < 2 || thuHoc > 8)) {
            throw new BusinessException("Thứ học phải nằm trong khoảng từ 2 đến 8.");
        }
        validatePeriods(form.getTietBatDau(), form.getTietKetThuc());
    }

    private void validateScheduleForOpening(LopHocPhan lopHocPhan) {
        Integer thuHoc = lopHocPhan.getThuHoc();
        if (thuHoc == null || thuHoc < 2 || thuHoc > 8) {
            throw new BusinessException("Thứ học phải được nhập và nằm trong khoảng từ 2 đến 8 trước khi mở lớp.");
        }
        Integer tietBatDau = lopHocPhan.getTietBatDau();
        Integer tietKetThuc = lopHocPhan.getTietKetThuc();
        if (tietBatDau == null || tietBatDau < 1) {
            throw new BusinessException("Tiết bắt đầu phải được nhập và lớn hơn hoặc bằng 1 trước khi mở lớp.");
        }
        if (tietKetThuc == null || tietKetThuc < tietBatDau) {
            throw new BusinessException("Tiết kết thúc phải được nhập và không nhỏ hơn tiết bắt đầu.");
        }
    }

    private void validatePeriods(Integer tietBatDau, Integer tietKetThuc) {
        if (tietBatDau != null && tietBatDau < 1) {
            throw new BusinessException("Tiết bắt đầu phải lớn hơn hoặc bằng 1.");
        }
        if (tietKetThuc != null && tietKetThuc < 1) {
            throw new BusinessException("Tiết kết thúc phải lớn hơn hoặc bằng 1.");
        }
        if (tietBatDau != null && tietKetThuc != null && tietKetThuc < tietBatDau) {
            throw new BusinessException("Tiết kết thúc không thể nhỏ hơn tiết bắt đầu.");
        }
    }

    private void validateCapacity(Integer siSoToiDa) {
        if (siSoToiDa == null || siSoToiDa < 1) {
            throw new BusinessException("Sĩ số tối đa phải lớn hơn hoặc bằng 1.");
        }
    }

    private void applyEditableFields(LopHocPhan lopHocPhan, LopHocPhanForm form) {
        lopHocPhan.setPhongHoc(blankToNull(form.getPhongHoc()));
        lopHocPhan.setThuHoc(form.getThuHoc());
        lopHocPhan.setTietBatDau(form.getTietBatDau());
        lopHocPhan.setTietKetThuc(form.getTietKetThuc());
        lopHocPhan.setSiSoToiDa(form.getSiSoToiDa());
    }

    private String requiredTrimmed(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LopHocPhan initialize(LopHocPhan lopHocPhan) {
        MonHoc monHoc = lopHocPhan.getMonHoc();
        Hibernate.initialize(monHoc);
        Hibernate.initialize(monHoc.getKhoa());
        if (lopHocPhan.getGiangVien() != null) {
            Hibernate.initialize(lopHocPhan.getGiangVien());
        }
        Hibernate.initialize(lopHocPhan.getHocKy());
        return lopHocPhan;
    }

    private List<LopHocPhan> initialize(List<LopHocPhan> lopHocPhanList) {
        lopHocPhanList.forEach(this::initialize);
        return lopHocPhanList;
    }
}
