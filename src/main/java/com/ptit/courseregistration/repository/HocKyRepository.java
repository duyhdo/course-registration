package com.ptit.courseregistration.repository;

import com.ptit.courseregistration.entity.HocKy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HocKyRepository extends JpaRepository<HocKy, String> {

    List<HocKy> findAllByOrderByNgayBatDauDesc();
}
