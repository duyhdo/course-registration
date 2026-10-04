package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "KHOA", schema = "dbo")
public class Khoa {

    @Id
    @Column(name = "MaKhoa", length = 10, nullable = false)
    private String maKhoa;

    @Column(name = "TenKhoa", length = 100, nullable = false)
    private String tenKhoa;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    public Khoa() {
    }

    public String getMaKhoa() {
        return maKhoa;
    }

    public void setMaKhoa(String maKhoa) {
        this.maKhoa = maKhoa;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
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
