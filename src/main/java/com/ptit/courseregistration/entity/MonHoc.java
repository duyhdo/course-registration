package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "MONHOC", schema = "dbo")
public class MonHoc {

    @Id
    @Column(name = "MaMonHoc", length = 20, nullable = false)
    private String maMonHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", referencedColumnName = "MaKhoa", nullable = false)
    private Khoa khoa;

    @Column(name = "TenMonHoc", length = 100, nullable = false)
    private String tenMonHoc;

    @Column(name = "SoTinChi", nullable = false)
    private Integer soTinChi;

    @Column(name = "SoTietLyThuyet")
    private Integer soTietLyThuyet;

    @Column(name = "SoTietThucHanh")
    private Integer soTietThucHanh;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    public MonHoc() {
    }

    public String getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(String maMonHoc) {
        this.maMonHoc = maMonHoc;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }

    public Integer getSoTietLyThuyet() {
        return soTietLyThuyet;
    }

    public void setSoTietLyThuyet(Integer soTietLyThuyet) {
        this.soTietLyThuyet = soTietLyThuyet;
    }

    public Integer getSoTietThucHanh() {
        return soTietThucHanh;
    }

    public void setSoTietThucHanh(Integer soTietThucHanh) {
        this.soTietThucHanh = soTietThucHanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
