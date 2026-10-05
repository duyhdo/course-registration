package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DangKyHocPhanRepository extends JpaRepository<DangKyHocPhan, Long> {

    boolean existsByLopHocPhan_MaLopHocPhanAndTrangThai(String maLopHocPhan, String trangThai);

    boolean existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhan(String maSV, String maLopHocPhan);

    boolean existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(
            String maSV, String maLopHocPhan, String trangThai);

    Optional<DangKyHocPhan> findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
            String maSV, String maLopHocPhan, String trangThai);

    List<DangKyHocPhan> findBySinhVien_MaSVOrderByNgayDangKyDesc(String maSV);

    List<DangKyHocPhan> findBySinhVien_MaSVAndTrangThaiIn(String maSV, Collection<String> trangThai);

    List<DangKyHocPhan> findByLopHocPhan_MaLopHocPhanAndTrangThai(String maLopHocPhan, String trangThai);

    long countByLopHocPhan_MaLopHocPhanAndTrangThai(String maLopHocPhan, String trangThai);

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan.monHoc", "lopHocPhan.hocKy",
            "lopHocPhan.giangVien"})
    @Query("""
            SELECT d
            FROM DangKyHocPhan d
            WHERE (:keyword IS NULL
                   OR LOWER(d.sinhVien.maSV) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.sinhVien.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.lopHocPhan.maLopHocPhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.lopHocPhan.monHoc.tenMonHoc) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:maHocKy IS NULL OR d.lopHocPhan.hocKy.maHocKy = :maHocKy)
              AND (:maLopHocPhan IS NULL OR d.lopHocPhan.maLopHocPhan = :maLopHocPhan)
              AND (:trangThai IS NULL OR d.trangThai = :trangThai)
            ORDER BY d.ngayDangKy DESC
            """)
    List<DangKyHocPhan> searchForAdmin(@Param("keyword") String keyword,
                                       @Param("maHocKy") String maHocKy,
                                       @Param("maLopHocPhan") String maLopHocPhan,
                                       @Param("trangThai") String trangThai);

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan.monHoc", "lopHocPhan.hocKy",
            "lopHocPhan.giangVien"})
    @Query("""
            SELECT d
            FROM DangKyHocPhan d
            WHERE d.lopHocPhan.maLopHocPhan = :maLopHocPhan
              AND (:trangThai IS NULL OR d.trangThai = :trangThai)
            ORDER BY d.sinhVien.maSV ASC
            """)
    List<DangKyHocPhan> findForAdminByCourseSection(@Param("maLopHocPhan") String maLopHocPhan,
                                                     @Param("trangThai") String trangThai);

    @Query("""
            SELECT COUNT(d)
            FROM DangKyHocPhan d
            WHERE d.sinhVien.maSV = :maSV
              AND d.lopHocPhan.monHoc.maMonHoc = :maMonHoc
              AND d.lopHocPhan.hocKy.maHocKy = :maHocKy
              AND d.trangThai = 'REGISTERED'
            """)
    long countRegisteredForSubjectInSemester(@Param("maSV") String maSV,
                                             @Param("maMonHoc") String maMonHoc,
                                             @Param("maHocKy") String maHocKy);

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
