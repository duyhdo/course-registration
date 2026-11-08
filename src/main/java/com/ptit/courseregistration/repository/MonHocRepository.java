package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.MonHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonHocRepository extends JpaRepository<MonHoc, String> {

    Page<MonHoc> findByMaMonHocContainingIgnoreCaseOrTenMonHocContainingIgnoreCase(
            String maMonHoc, String tenMonHoc, Pageable pageable);

    List<MonHoc> findByTrangThai(String trangThai);

    List<MonHoc> findByKhoa_MaKhoa(String maKhoa);
}
