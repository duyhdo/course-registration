package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.Khoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhoaRepository extends JpaRepository<Khoa, String> {

    List<Khoa> findByTrangThai(String trangThai);
}
