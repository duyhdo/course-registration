package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "LOPHOCPHAN", schema = "dbo")
public class LopHocPhan {

    @Id
    @Column(name = "MaLopHocPhan", length = 20, nullable = false)
    private String maLopHocPhan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaMonHoc", referencedColumnName = "MaMonHoc", nullable = false)
    private MonHoc monHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "MaGV", referencedColumnName = "MaGV", nullable = true)
    private GiangVien giangVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHocKy", referencedColumnName = "MaHocKy", nullable = false)
    private HocKy hocKy;

    @Column(name = "PhongHoc", length = 50)
    private String phongHoc;

    @Column(name = "ThuHoc")
    private Integer thuHoc;

    @Column(name = "TietBatDau")
    private Integer tietBatDau;

    @Column(name = "TietKetThuc")
    private Integer tietKetThuc;

    @Column(name = "SiSoToiDa", nullable = false)
    private Integer siSoToiDa;

    @Column(name = "SiSoHienTai", nullable = false)
    private Integer siSoHienTai;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    public LopHocPhan() {
    }

    public String getMaLopHocPhan() {
        return maLopHocPhan;
    }

    public void setMaLopHocPhan(String maLopHocPhan) {
        this.maLopHocPhan = maLopHocPhan;
    }

    public MonHoc getMonHoc() {
        return monHoc;
    }

    public void setMonHoc(MonHoc monHoc) {
        this.monHoc = monHoc;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    public HocKy getHocKy() {
        return hocKy;
    }

    public void setHocKy(HocKy hocKy) {
        this.hocKy = hocKy;
    }

    public String getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(String phongHoc) {
        this.phongHoc = phongHoc;
    }

    public Integer getThuHoc() {
        return thuHoc;
    }

    public void setThuHoc(Integer thuHoc) {
        this.thuHoc = thuHoc;
    }

    public Integer getTietBatDau() {
        return tietBatDau;
    }

    public void setTietBatDau(Integer tietBatDau) {
        this.tietBatDau = tietBatDau;
    }

    public Integer getTietKetThuc() {
        return tietKetThuc;
    }

    public void setTietKetThuc(Integer tietKetThuc) {
        this.tietKetThuc = tietKetThuc;
    }

    public Integer getSiSoToiDa() {
        return siSoToiDa;
    }

    public void setSiSoToiDa(Integer siSoToiDa) {
        this.siSoToiDa = siSoToiDa;
    }

    public Integer getSiSoHienTai() {
        return siSoHienTai;
    }

    public void setSiSoHienTai(Integer siSoHienTai) {
        this.siSoHienTai = siSoHienTai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
