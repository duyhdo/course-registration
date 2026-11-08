package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.dto.HocKyConfigForm;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.service.HocKyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/semesters")
public class HocKyAdminController {

    private static final String FORM_VIEW = "admin/semesters/form";
    private static final String LIST_REDIRECT = "redirect:/admin/semesters";

    private final HocKyService hocKyService;

    public HocKyAdminController(HocKyService hocKyService) {
        this.hocKyService = hocKyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("semesters", hocKyService.findAll());
        return "admin/semesters/list";
    }

    @GetMapping("/{maHocKy}/edit")
    public String editForm(@PathVariable String maHocKy, Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            HocKy hocKy = hocKyService.findById(maHocKy);
            model.addAttribute("semester", hocKy);
            model.addAttribute("configForm", new HocKyConfigForm(
                    hocKy.getNgayMoDangKy(), hocKy.getNgayDongDangKy(),
                    hocKy.getTinChiToiThieu(), hocKy.getTinChiToiDa()));
            return FORM_VIEW;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return LIST_REDIRECT;
        }
    }

    @PostMapping("/{maHocKy}/edit")
    public String update(@PathVariable String maHocKy, @Valid HocKyConfigForm configForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!result.hasErrors()) {
                hocKyService.updateEnrollmentConfig(maHocKy, configForm);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật cấu hình học kỳ thành công.");
                return LIST_REDIRECT;
            }
            model.addAttribute("semester", hocKyService.findById(maHocKy));
        } catch (BusinessException ex) {
            if (!hocKyExists(maHocKy)) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return LIST_REDIRECT;
            }
            result.reject("hocKyConfig.invalid", ex.getMessage());
            model.addAttribute("semester", hocKyService.findById(maHocKy));
        }
        return FORM_VIEW;
    }

    private boolean hocKyExists(String maHocKy) {
        try {
            hocKyService.findById(maHocKy);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }
}
