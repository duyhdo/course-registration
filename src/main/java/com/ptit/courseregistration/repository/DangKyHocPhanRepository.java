package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DangKyHocPhanRepository extends JpaRepository<DangKyHocPhan, Long> {

    boolean existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhan(String maSV, String maLopHocPhan);

    List<DangKyHocPhan> findBySinhVien_MaSVOrderByNgayDangKyDesc(String maSV);

    List<DangKyHocPhan> findBySinhVien_MaSVAndTrangThaiIn(String maSV, Collection<String> trangThai);

    List<DangKyHocPhan> findByLopHocPhan_MaLopHocPhanAndTrangThai(String maLopHocPhan, String trangThai);

    long countByLopHocPhan_MaLopHocPhanAndTrangThai(String maLopHocPhan, String trangThai);

    @Query("""
            SELECT COALESCE(SUM(m.soTinChi), 0)
            FROM DangKyHocPhan d
            JOIN d.lopHocPhan l
            JOIN l.monHoc m
            WHERE d.sinhVien.maSV = :maSV
              AND l.hocKy.maHocKy = :maHocKy
              AND d.trangThai = 'REGISTERED'
            """)
    long sumRegisteredCredits(@Param("maSV") String maSV, @Param("maHocKy") String maHocKy);
}
