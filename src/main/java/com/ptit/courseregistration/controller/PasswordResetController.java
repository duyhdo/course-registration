package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forgot-password")
public class PasswordResetController {

    private static final String FORGOT_VIEW = "auth/forgot-password";
    private static final String VERIFY_VIEW = "auth/verify-reset-code";
    private static final String RESET_VIEW = "auth/reset-password";
    private static final String FORGOT_REDIRECT = "redirect:/forgot-password";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String forgotForm() {
        return FORGOT_VIEW;
    }

    @PostMapping
    public String requestReset(@RequestParam(required = false) String email, HttpSession session,
                               Model model, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.requestReset(email, session);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã xác nhận đã được gửi đến email của bạn.");
            return "redirect:/forgot-password/verify";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("email", email);
            return FORGOT_VIEW;
        }
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session) {
        if (session.getAttribute(PasswordResetService.SESSION_EMAIL) == null) {
            return FORGOT_REDIRECT;
        }
        return VERIFY_VIEW;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam(required = false) String code, HttpSession session, Model model) {
        try {
            passwordResetService.verifyCode(code, session);
            return "redirect:/forgot-password/reset";
        } catch (BusinessException ex) {
            if (session.getAttribute(PasswordResetService.SESSION_EMAIL) == null) {
                model.addAttribute("errorMessage", ex.getMessage());
                return FORGOT_VIEW;
            }
            model.addAttribute("errorMessage", ex.getMessage());
            return VERIFY_VIEW;
        }
    }

    @GetMapping("/reset")
    public String resetForm(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(PasswordResetService.SESSION_VERIFIED))) {
            return FORGOT_REDIRECT;
        }
        return RESET_VIEW;
    }

    @PostMapping("/reset")
    public String reset(@RequestParam(required = false) String newPassword,
                        @RequestParam(required = false) String confirmPassword,
                        HttpSession session, Model model) {
        try {
            passwordResetService.resetPassword(newPassword, confirmPassword, session);
            return "redirect:/login?resetSuccess";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            if (session.getAttribute(PasswordResetService.SESSION_EMAIL) == null) {
                return FORGOT_VIEW;
            }
            return RESET_VIEW;
        }
    }
}