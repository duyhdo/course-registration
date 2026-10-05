package com.ptit.courseregistration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LopHocPhanForm {

    @NotBlank
    @Size(max = 20)
    private String maLopHocPhan;

    @NotBlank
    private String maMonHoc;

    private String maGV;

    @NotBlank
    private String maHocKy;

    @Size(max = 50)
    private String phongHoc;

    @Min(2)
    @Max(8)
    private Integer thuHoc;

    @Min(1)
    private Integer tietBatDau;

    @Min(1)
    private Integer tietKetThuc;

    @NotNull
    @Min(1)
    private Integer siSoToiDa;

    public LopHocPhanForm() {
    }

    public String getMaLopHocPhan() {
        return maLopHocPhan;
    }

    public void setMaLopHocPhan(String maLopHocPhan) {
        this.maLopHocPhan = maLopHocPhan;
    }

    public String getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(String maMonHoc) {
        this.maMonHoc = maMonHoc;
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getMaHocKy() {
        return maHocKy;
    }

    public void setMaHocKy(String maHocKy) {
        this.maHocKy = maHocKy;
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
}
