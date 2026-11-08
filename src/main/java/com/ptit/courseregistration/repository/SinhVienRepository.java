package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.SinhVien;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {

    Optional<SinhVien> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndMaSVNot(String email, String maSV);

    Page<SinhVien> findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(
            String maSV, String hoTen, Pageable pageable);

    List<SinhVien> findByTrangThai(String trangThai);

    List<SinhVien> findByKhoa_MaKhoa(String maKhoa);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SinhVien s WHERE s.maSV = :maSV")
    Optional<SinhVien> findByMaSVForUpdate(@Param("maSV") String maSV);
}
