package com.ptit.courseregistration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class HocKyConfigForm {

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayMoDangKy;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayDongDangKy;

    @Min(0)
    private Integer tinChiToiThieu;

    @NotNull
    @Min(1)
    private Integer tinChiToiDa;

    public HocKyConfigForm() {
    }

    public HocKyConfigForm(LocalDateTime ngayMoDangKy, LocalDateTime ngayDongDangKy,
                           Integer tinChiToiThieu, Integer tinChiToiDa) {
        this.ngayMoDangKy = ngayMoDangKy;
        this.ngayDongDangKy = ngayDongDangKy;
        this.tinChiToiThieu = tinChiToiThieu;
        this.tinChiToiDa = tinChiToiDa;
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
