package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.service.DangKyHocPhanService;
import com.ptit.courseregistration.web.PaginationSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
                            @RequestParam(required = false) Integer page,
                            @RequestParam(required = false) Integer size,
                            Model model) {
        Sort sort = Sort.by(Sort.Direction.ASC, "maLopHocPhan");
        Page<LopHocPhan> courseSections = PaginationSupport.fetchPage(page, size, sort,
                pageable -> lopHocPhanRepository.findByFilters(
                        blankToNull(keyword), blankToNull(maHocKy), "OPEN", pageable));
        model.addAttribute("courseSections", courseSections.getContent());
        model.addAttribute("pagination", PaginationSupport.view(courseSections, "/student/course-sections",
                PaginationSupport.filters("keyword", keyword, "maHocKy", maHocKy)));
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
    public String history(Authentication authentication,
                           @RequestParam(required = false) Integer page,
                           @RequestParam(required = false) Integer size,
                           Model model) {
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayDangKy")
                .and(Sort.by(Sort.Direction.DESC, "maDangKy"));
        Page<DangKyHocPhan> registrations = PaginationSupport.fetchPage(page, size, sort,
                pageable -> dangKyHocPhanService.findStudentHistory(authentication.getName(), pageable));
        model.addAttribute("registrations", registrations.getContent());
        model.addAttribute("pagination", PaginationSupport.view(registrations, "/student/enrollment-history",
                PaginationSupport.filters()));
        return "student/enrollment/history";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
