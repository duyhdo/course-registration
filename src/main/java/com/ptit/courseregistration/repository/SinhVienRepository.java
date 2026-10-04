package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {

    Optional<SinhVien> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndMaSVNot(String email, String maSV);

    List<SinhVien> findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(String maSV, String hoTen);

    List<SinhVien> findByTrangThai(String trangThai);

    List<SinhVien> findByKhoa_MaKhoa(String maKhoa);
}
