package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "DANGKYHOCPHAN", schema = "dbo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MaSV", "MaLopHocPhan"}))
public class DangKyHocPhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDangKy", nullable = false)
    private Long maDangKy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaSV", referencedColumnName = "MaSV", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaLopHocPhan", referencedColumnName = "MaLopHocPhan", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "NgayDangKy", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime ngayDangKy;

    @Column(name = "NgayHuy", columnDefinition = "datetime2")
    private LocalDateTime ngayHuy;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    public DangKyHocPhan() {
    }

    public Long getMaDangKy() {
        return maDangKy;
    }

    public void setMaDangKy(Long maDangKy) {
        this.maDangKy = maDangKy;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public LopHocPhan getLopHocPhan() {
        return lopHocPhan;
    }

    public void setLopHocPhan(LopHocPhan lopHocPhan) {
        this.lopHocPhan = lopHocPhan;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public LocalDateTime getNgayHuy() {
        return ngayHuy;
    }

    public void setNgayHuy(LocalDateTime ngayHuy) {
        this.ngayHuy = ngayHuy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
