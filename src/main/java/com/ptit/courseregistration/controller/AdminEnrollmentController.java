package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.service.AdminEnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminEnrollmentController {

    private final AdminEnrollmentService adminEnrollmentService;
    private final HocKyRepository hocKyRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public AdminEnrollmentController(AdminEnrollmentService adminEnrollmentService,
                                     HocKyRepository hocKyRepository,
                                     LopHocPhanRepository lopHocPhanRepository) {
        this.adminEnrollmentService = adminEnrollmentService;
        this.hocKyRepository = hocKyRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
    }

    @GetMapping("/enrollments")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String maHocKy,
                       @RequestParam(required = false) String maLopHocPhan,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("enrollments",
                adminEnrollmentService.search(keyword, maHocKy, maLopHocPhan, trangThai));
        model.addAttribute("keyword", keyword);
        model.addAttribute("maHocKy", maHocKy);
        model.addAttribute("maLopHocPhan", maLopHocPhan);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("semesters", hocKyRepository.findAllByOrderByNgayBatDauDesc());
        model.addAttribute("courseSections", lopHocPhanRepository.findAll());
        return "admin/enrollments/list";
    }

    @GetMapping("/course-sections/{maLopHocPhan}/students")
    public String students(@PathVariable String maLopHocPhan,
                           @RequestParam(required = false) String trangThai,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            LopHocPhan courseSection = lopHocPhanRepository.findWithDetailsById(maLopHocPhan)
                    .orElseThrow(() -> new BusinessException("Lớp học phần không tồn tại."));
            model.addAttribute("courseSection", courseSection);
            model.addAttribute("enrollments",
                    adminEnrollmentService.findByCourseSection(maLopHocPhan, trangThai));
            model.addAttribute("trangThai",
                    trangThai == null || trangThai.isBlank() ? "REGISTERED" : trangThai.trim());
            model.addAttribute("registeredCount",
                    adminEnrollmentService.countRegisteredByCourseSection(maLopHocPhan));
            return "admin/course-sections/students";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/course-sections";
        }
    }
}
