package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.service.DangKyHocPhanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentEnrollmentController {

    private final DangKyHocPhanService dangKyHocPhanService;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final HocKyRepository hocKyRepository;

    public StudentEnrollmentController(DangKyHocPhanService dangKyHocPhanService,
                                       LopHocPhanRepository lopHocPhanRepository,
                                       HocKyRepository hocKyRepository) {
        this.dangKyHocPhanService = dangKyHocPhanService;
        this.lopHocPhanRepository = lopHocPhanRepository;
        this.hocKyRepository = hocKyRepository;
    }

    @GetMapping("/course-sections")
    public String available(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String maHocKy,
                            Model model) {
        model.addAttribute("courseSections",
                lopHocPhanRepository.findByFilters(blankToNull(keyword), blankToNull(maHocKy), "OPEN"));
        model.addAttribute("keyword", keyword);
        model.addAttribute("maHocKy", maHocKy);
        model.addAttribute("semesters", hocKyRepository.findAllByOrderByNgayBatDauDesc());
        return "student/enrollment/available";
    }

    @GetMapping("/course-sections/{maLopHocPhan}")
    public String detail(@PathVariable String maLopHocPhan, Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("courseSection",
                    dangKyHocPhanService.findOpenCourseSectionDetail(maLopHocPhan));
            return "student/enrollment/detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/course-sections";
        }
    }

    @PostMapping("/course-sections/{maLopHocPhan}/register")
    public String register(@PathVariable String maLopHocPhan, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            dangKyHocPhanService.register(authentication.getName(), maLopHocPhan);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký học phần thành công.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/course-sections";
    }

    @GetMapping("/registrations")
    public String registrations(Authentication authentication, Model model) {
        model.addAttribute("registrations",
                dangKyHocPhanService.findActiveRegistrations(authentication.getName()));
        model.addAttribute("totalCredits",
                dangKyHocPhanService.calculateTotalRegisteredCredits(authentication.getName()));
        return "student/enrollment/registrations";
    }

    @PostMapping("/registrations/{maLopHocPhan}/cancel")
    public String cancel(@PathVariable String maLopHocPhan, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            dangKyHocPhanService.cancel(authentication.getName(), maLopHocPhan);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đăng ký học phần thành công.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/registrations";
    }

    @GetMapping("/enrollment-history")
    public String history(Authentication authentication, Model model) {
        model.addAttribute("registrations",
                dangKyHocPhanService.findStudentHistory(authentication.getName()));
        return "student/enrollment/history";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
