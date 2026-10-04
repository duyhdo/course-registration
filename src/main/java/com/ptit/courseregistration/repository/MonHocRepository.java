package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonHocRepository extends JpaRepository<MonHoc, String> {

    List<MonHoc> findByMaMonHocContainingIgnoreCaseOrTenMonHocContainingIgnoreCase(String maMonHoc, String tenMonHoc);

    List<MonHoc> findByTrangThai(String trangThai);

    List<MonHoc> findByKhoa_MaKhoa(String maKhoa);
}
