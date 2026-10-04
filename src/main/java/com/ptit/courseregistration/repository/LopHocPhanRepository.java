package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.LopHocPhan;
import jakarta.persistence.LockModeType;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LopHocPhan l WHERE l.maLopHocPhan = :id")
    Optional<LopHocPhan> findByIdForUpdate(@Param("id") String id);
}
