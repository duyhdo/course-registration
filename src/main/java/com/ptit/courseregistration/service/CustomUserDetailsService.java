package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.TaiKhoan;
import com.ptit.courseregistration.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final TaiKhoanRepository taiKhoanRepository;

    public CustomUserDetailsService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(taiKhoan.getTenDangNhap())
                .password(taiKhoan.getMatKhauHash())
                .authorities("ROLE_" + taiKhoan.getVaiTro())
                .disabled(!STATUS_ACTIVE.equals(taiKhoan.getTrangThai()))
                .build();
    }
}
