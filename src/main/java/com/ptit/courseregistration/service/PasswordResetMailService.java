package com.ptit.courseregistration.service;

import com.ptit.courseregistration.exception.BusinessException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetMailService {

    private final JavaMailSender mailSender;

    public PasswordResetMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetCode(String recipientEmail, String verificationCode) {
        SimpleMailMessage message = buildMessage(recipientEmail, verificationCode);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    private SimpleMailMessage buildMessage(String recipientEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Khôi phục mật khẩu - Course Registration");
        message.setText("Bạn đang yêu cầu khôi phục mật khẩu.\n"
                + "Mã xác nhận của bạn là: " + verificationCode + "\n"
                + "Mã có hiệu lực trong 10 phút.");
        return message;
    }
}