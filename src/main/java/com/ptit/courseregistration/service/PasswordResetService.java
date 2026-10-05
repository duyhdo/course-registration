package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.entity.TaiKhoan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.SinhVienRepository;
import com.ptit.courseregistration.repository.TaiKhoanRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    public static final String SESSION_EMAIL = "resetEmail";
    public static final String SESSION_CODE_HASH = "resetCodeHash";
    public static final String SESSION_EXPIRES_AT = "resetExpiresAt";
    public static final String SESSION_VERIFIED = "resetVerified";

    private static final long CODE_LIFETIME_MINUTES = 10;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SinhVienRepository sinhVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailService mailService;

    public PasswordResetService(SinhVienRepository sinhVienRepository,
                                TaiKhoanRepository taiKhoanRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetMailService mailService) {
        this.sinhVienRepository = sinhVienRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public void requestReset(String email, HttpSession session) {
        String normalizedEmail = email == null ? "" : email.trim();
        findAccountByEmail(normalizedEmail);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        mailService.sendResetCode(normalizedEmail, code);

        session.setAttribute(SESSION_EMAIL, normalizedEmail);
        session.setAttribute(SESSION_CODE_HASH, hash(code));
        session.setAttribute(SESSION_EXPIRES_AT, LocalDateTime.now().plusMinutes(CODE_LIFETIME_MINUTES));
        session.setAttribute(SESSION_VERIFIED, Boolean.FALSE);
    }

    public void verifyCode(String code, HttpSession session) {
        String storedHash = (String) session.getAttribute(SESSION_CODE_HASH);
        LocalDateTime expiresAt = (LocalDateTime) session.getAttribute(SESSION_EXPIRES_AT);
        if (session.getAttribute(SESSION_EMAIL) == null || storedHash == null || expiresAt == null) {
            throw new BusinessException("Yêu cầu khôi phục mật khẩu không hợp lệ hoặc đã hết hạn.");
        }
        if (LocalDateTime.now().isAfter(expiresAt)) {
            clearResetSession(session);
            throw new BusinessException("Mã xác nhận đã hết hạn.");
        }

        String submitted = code == null ? "" : code.trim();
        boolean matches = MessageDigest.isEqual(
                hash(submitted).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            throw new BusinessException("Mã xác nhận không đúng.");
        }
        session.setAttribute(SESSION_VERIFIED, Boolean.TRUE);
    }

    @Transactional
    public void resetPassword(String newPassword, String confirmPassword, HttpSession session) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null || !Boolean.TRUE.equals(session.getAttribute(SESSION_VERIFIED))) {
            throw new BusinessException("Bạn cần xác nhận mã trước khi đặt lại mật khẩu.");
        }
        LocalDateTime expiresAt = (LocalDateTime) session.getAttribute(SESSION_EXPIRES_AT);
        if (expiresAt == null || LocalDateTime.now().isAfter(expiresAt)) {
            clearResetSession(session);
            throw new BusinessException("Mã xác nhận đã hết hạn.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new BusinessException("Mật khẩu mới không được để trống.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("Mật khẩu xác nhận không khớp.");
        }

        TaiKhoan taiKhoan = findAccountByEmail(email);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(newPassword));
        taiKhoan.setNgayCapNhat(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        clearResetSession(session);
    }

    public void clearResetSession(HttpSession session) {
        session.removeAttribute(SESSION_EMAIL);
        session.removeAttribute(SESSION_CODE_HASH);
        session.removeAttribute(SESSION_EXPIRES_AT);
        session.removeAttribute(SESSION_VERIFIED);
    }

    private TaiKhoan findAccountByEmail(String email) {
        SinhVien sinhVien = sinhVienRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email không khớp với tài khoản nào."));
        TaiKhoan taiKhoan = sinhVien.getTaiKhoan();
        if (taiKhoan == null) {
            throw new BusinessException("Email không khớp với tài khoản nào.");
        }
        return taiKhoan;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
