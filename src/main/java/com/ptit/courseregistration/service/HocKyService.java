package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.HocKyConfigForm;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.HocKyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HocKyService {

    private final HocKyRepository hocKyRepository;

    public HocKyService(HocKyRepository hocKyRepository) {
        this.hocKyRepository = hocKyRepository;
    }

    @Transactional(readOnly = true)
    public List<HocKy> findAll() {
        return hocKyRepository.findAllByOrderByNgayBatDauDesc();
    }

    @Transactional(readOnly = true)
    public HocKy findById(String maHocKy) {
        return hocKyRepository.findById(maHocKy)
                .orElseThrow(() -> new BusinessException("Học kỳ không tồn tại."));
    }

    @Transactional
    public HocKy updateEnrollmentConfig(String maHocKy, HocKyConfigForm form) {
        HocKy hocKy = findById(maHocKy);

        if (form.getNgayMoDangKy() == null) {
            throw new BusinessException("Thời gian mở đăng ký là bắt buộc.");
        }
        if (form.getNgayDongDangKy() == null) {
            throw new BusinessException("Thời gian đóng đăng ký là bắt buộc.");
        }
        if (form.getNgayMoDangKy().isAfter(form.getNgayDongDangKy())) {
            throw new BusinessException("Thời gian mở đăng ký không được sau thời gian đóng đăng ký.");
        }

        Integer toiDa = form.getTinChiToiDa();
        if (toiDa == null || toiDa < 1) {
            throw new BusinessException("Tín chỉ tối đa phải lớn hơn hoặc bằng 1.");
        }

        Integer toiThieu = form.getTinChiToiThieu();
        if (toiThieu != null) {
            if (toiThieu < 0) {
                throw new BusinessException("Tín chỉ tối thiểu không được âm.");
            }
            if (toiThieu > toiDa) {
                throw new BusinessException("Tín chỉ tối thiểu không được lớn hơn tín chỉ tối đa.");
            }
        }

        hocKy.setNgayMoDangKy(form.getNgayMoDangKy());
        hocKy.setNgayDongDangKy(form.getNgayDongDangKy());
        hocKy.setTinChiToiThieu(toiThieu);
        hocKy.setTinChiToiDa(toiDa);
        return hocKyRepository.save(hocKy);
    }
}
