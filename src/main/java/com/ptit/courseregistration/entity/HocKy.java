package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HOCKY", schema = "dbo")
public class HocKy {

    @Id
    @Column(name = "MaHocKy", length = 20, nullable = false)
    private String maHocKy;

    @Column(name = "TenHocKy", length = 50, nullable = false)
    private String tenHocKy;

    @Column(name = "NamHoc", length = 20, nullable = false)
    private String namHoc;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "NgayMoDangKy", columnDefinition = "datetime2")
    private LocalDateTime ngayMoDangKy;

    @Column(name = "NgayDongDangKy", columnDefinition = "datetime2")
    private LocalDateTime ngayDongDangKy;

    @Column(name = "TinChiToiThieu")
    private Integer tinChiToiThieu;

    @Column(name = "TinChiToiDa")
    private Integer tinChiToiDa;

    public HocKy() {
    }

    public String getMaHocKy() {
        return maHocKy;
    }

    public void setMaHocKy(String maHocKy) {
        this.maHocKy = maHocKy;
    }

    public String getTenHocKy() {
        return tenHocKy;
    }

    public void setTenHocKy(String tenHocKy) {
        this.tenHocKy = tenHocKy;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public LocalDateTime getNgayMoDangKy() {
        return ngayMoDangKy;
    }

    public void setNgayMoDangKy(LocalDateTime ngayMoDangKy) {
        this.ngayMoDangKy = ngayMoDangKy;
    }

    public LocalDateTime getNgayDongDangKy() {
        return ngayDongDangKy;
    }

    public void setNgayDongDangKy(LocalDateTime ngayDongDangKy) {
        this.ngayDongDangKy = ngayDongDangKy;
    }

    public Integer getTinChiToiThieu() {
        return tinChiToiThieu;
    }

    public void setTinChiToiThieu(Integer tinChiToiThieu) {
        this.tinChiToiThieu = tinChiToiThieu;
    }

    public Integer getTinChiToiDa() {
        return tinChiToiDa;
    }

    public void setTinChiToiDa(Integer tinChiToiDa) {
        this.tinChiToiDa = tinChiToiDa;
    }
}
