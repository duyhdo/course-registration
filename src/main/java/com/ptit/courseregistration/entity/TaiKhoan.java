package com.ptit.courseregistration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "TAIKHOAN", schema = "dbo")
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TaiKhoanId", nullable = false)
    private Long taiKhoanId;

    @Column(name = "TenDangNhap", length = 50, nullable = false, unique = true)
    private String tenDangNhap;

    @Column(name = "MatKhauHash", length = 255, nullable = false)
    private String matKhauHash;

    @Column(name = "VaiTro", length = 20, nullable = false)
    private String vaiTro;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    @Column(name = "NgayTao", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat", columnDefinition = "datetime2")
    private LocalDateTime ngayCapNhat;

    public TaiKhoan() {
    }

    public Long getTaiKhoanId() {
        return taiKhoanId;
    }

    public void setTaiKhoanId(Long taiKhoanId) {
        this.taiKhoanId = taiKhoanId;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
