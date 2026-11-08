package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.LopHocPhan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LopHocPhanRepository extends JpaRepository<LopHocPhan, String> {

    List<LopHocPhan> findByTrangThai(String trangThai);

    List<LopHocPhan> findByHocKy_MaHocKy(String maHocKy);

    List<LopHocPhan> findByHocKy_MaHocKyAndTrangThai(String maHocKy, String trangThai);

    List<LopHocPhan> findByMonHoc_MaMonHoc(String maMonHoc);

    @EntityGraph(attributePaths = {"monHoc", "hocKy", "giangVien"})
    @Query("SELECT l FROM LopHocPhan l WHERE l.maLopHocPhan = :id")
    Optional<LopHocPhan> findWithDetailsById(@Param("id") String id);

    @EntityGraph(attributePaths = {"monHoc", "hocKy", "giangVien"})
    @Query("""
            SELECT DISTINCT l FROM LopHocPhan l
            JOIN l.monHoc m
            WHERE (:keyword IS NULL
                   OR LOWER(l.maLopHocPhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.maMonHoc) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.tenMonHoc) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:maHocKy IS NULL OR l.hocKy.maHocKy = :maHocKy)
              AND (:trangThai IS NULL OR l.trangThai = :trangThai)
            """)
    List<LopHocPhan> findByFilters(@Param("keyword") String keyword,
                                   @Param("maHocKy") String maHocKy,
                                   @Param("trangThai") String trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LopHocPhan l WHERE l.maLopHocPhan = :id")
    Optional<LopHocPhan> findByIdForUpdate(@Param("id") String id);
}
