package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiangVienRepository extends JpaRepository<GiangVien, String> {

    Optional<GiangVien> findByEmail(String email);

    List<GiangVien> findByTrangThai(String trangThai);

    List<GiangVien> findByKhoa_MaKhoa(String maKhoa);
}
