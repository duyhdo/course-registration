package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.dto.LopHocPhanForm;
import com.ptit.courseregistration.entity.GiangVien;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.GiangVienRepository;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import com.ptit.courseregistration.service.LopHocPhanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/course-sections")
public class LopHocPhanAdminController {

    private static final String FORM_VIEW = "admin/course-sections/form";
    private static final String DRAFT = "DRAFT";
    private static final String OPEN = "OPEN";
    private static final String CLOSED = "CLOSED";
    private static final String CANCELLED = "CANCELLED";

    private final LopHocPhanService lopHocPhanService;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final HocKyRepository hocKyRepository;

    public LopHocPhanAdminController(LopHocPhanService lopHocPhanService,
                                     MonHocRepository monHocRepository,
                                     GiangVienRepository giangVienRepository,
                                     HocKyRepository hocKyRepository) {
        this.lopHocPhanService = lopHocPhanService;
        this.monHocRepository = monHocRepository;
        this.giangVienRepository = giangVienRepository;
        this.hocKyRepository = hocKyRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String maHocKy,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("courseSections", lopHocPhanService.search(keyword, maHocKy, trangThai));
        model.addAttribute("keyword", keyword);
        model.addAttribute("maHocKy", maHocKy);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("semesters", hocKyRepository.findAllByOrderByNgayBatDauDesc());
        return "admin/course-sections/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("courseSectionForm", new LopHocPhanForm());
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid LopHocPhanForm courseSectionForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                lopHocPhanService.create(courseSectionForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm lớp học phần thành công.");
                return "redirect:/admin/course-sections";
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @GetMapping("/{maLopHocPhan}")
    public String detail(@PathVariable String maLopHocPhan, Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("courseSection", lopHocPhanService.findById(maLopHocPhan));
            return "admin/course-sections/detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/course-sections";
        }
    }

    @GetMapping("/{maLopHocPhan}/edit")
    public String editForm(@PathVariable String maLopHocPhan, Model model,
                           RedirectAttributes redirectAttributes) {
        LopHocPhan courseSection;
        try {
            courseSection = lopHocPhanService.findById(maLopHocPhan);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/course-sections";
        }
        if (isTerminal(courseSection.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể cập nhật lớp học phần đã đóng hoặc đã hủy.");
            return "redirect:/admin/course-sections/" + maLopHocPhan;
        }

        model.addAttribute("courseSectionForm", toForm(courseSection));
        prepareForm(model, true, courseSection);
        return FORM_VIEW;
    }

    @PostMapping("/{maLopHocPhan}/edit")
    public String update(@PathVariable String maLopHocPhan, @Valid LopHocPhanForm courseSectionForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        LopHocPhan current;
        try {
            current = lopHocPhanService.findById(maLopHocPhan);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/course-sections";
        }
        if (isTerminal(current.getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể cập nhật lớp học phần đã đóng hoặc đã hủy.");
            return "redirect:/admin/course-sections/" + maLopHocPhan;
        }

        courseSectionForm.setMaLopHocPhan(maLopHocPhan);
        if (!result.hasErrors()) {
            try {
                lopHocPhanService.update(maLopHocPhan, courseSectionForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lớp học phần thành công.");
                return "redirect:/admin/course-sections/" + maLopHocPhan;
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        if (OPEN.equals(current.getTrangThai())) {
            courseSectionForm.setMaMonHoc(current.getMonHoc().getMaMonHoc());
            courseSectionForm.setMaHocKy(current.getHocKy().getMaHocKy());
        }
        prepareForm(model, true, current);
        return FORM_VIEW;
    }

    @PostMapping("/{maLopHocPhan}/open")
    public String open(@PathVariable String maLopHocPhan, RedirectAttributes redirectAttributes) {
        return lifecycleAction(maLopHocPhan, redirectAttributes,
                () -> lopHocPhanService.open(maLopHocPhan), "Đã mở lớp học phần.");
    }

    @PostMapping("/{maLopHocPhan}/close")
    public String close(@PathVariable String maLopHocPhan, RedirectAttributes redirectAttributes) {
        return lifecycleAction(maLopHocPhan, redirectAttributes,
                () -> lopHocPhanService.close(maLopHocPhan), "Đã đóng lớp học phần.");
    }

    @PostMapping("/{maLopHocPhan}/cancel")
    public String cancel(@PathVariable String maLopHocPhan, RedirectAttributes redirectAttributes) {
        return lifecycleAction(maLopHocPhan, redirectAttributes,
                () -> lopHocPhanService.cancel(maLopHocPhan), "Đã hủy lớp học phần.");
    }

    private String lifecycleAction(String maLopHocPhan, RedirectAttributes redirectAttributes,
                                  Runnable action, String successMessage) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/course-sections/" + maLopHocPhan;
    }

    private void prepareForm(Model model, boolean editMode, LopHocPhan current) {
        List<MonHoc> subjects = new ArrayList<>(monHocRepository.findByTrangThai("ACTIVE"));
        List<GiangVien> teachers = new ArrayList<>(giangVienRepository.findByTrangThai("ACTIVE"));
        if (current != null) {
            addCurrentSubjectIfInactive(subjects, current.getMonHoc());
            addCurrentTeacherIfInactive(teachers, current.getGiangVien());
        }
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("semesters", hocKyRepository.findAllByOrderByNgayBatDauDesc());
        model.addAttribute("editMode", editMode);
        model.addAttribute("courseSectionId", current == null ? null : current.getMaLopHocPhan());
        model.addAttribute("sectionStatus", current == null ? DRAFT : current.getTrangThai());
        model.addAttribute("currentSubject", current == null ? null : current.getMonHoc());
        model.addAttribute("currentSemester", current == null ? null : current.getHocKy());
    }

    private void addCurrentSubjectIfInactive(List<MonHoc> subjects, MonHoc currentSubject) {
        if (currentSubject != null && !"ACTIVE".equals(currentSubject.getTrangThai())
                && subjects.stream().noneMatch(subject ->
                subject.getMaMonHoc().equals(currentSubject.getMaMonHoc()))) {
            subjects.add(currentSubject);
        }
    }

    private void addCurrentTeacherIfInactive(List<GiangVien> teachers, GiangVien currentTeacher) {
        if (currentTeacher != null && !"ACTIVE".equals(currentTeacher.getTrangThai())
                && teachers.stream().noneMatch(teacher ->
                teacher.getMaGV().equals(currentTeacher.getMaGV()))) {
            teachers.add(currentTeacher);
        }
    }

    private LopHocPhanForm toForm(LopHocPhan courseSection) {
        LopHocPhanForm form = new LopHocPhanForm();
        form.setMaLopHocPhan(courseSection.getMaLopHocPhan());
        form.setMaMonHoc(courseSection.getMonHoc().getMaMonHoc());
        form.setMaGV(courseSection.getGiangVien() == null ? null : courseSection.getGiangVien().getMaGV());
        form.setMaHocKy(courseSection.getHocKy().getMaHocKy());
        form.setPhongHoc(courseSection.getPhongHoc());
        form.setThuHoc(courseSection.getThuHoc());
        form.setTietBatDau(courseSection.getTietBatDau());
        form.setTietKetThuc(courseSection.getTietKetThuc());
        form.setSiSoToiDa(courseSection.getSiSoToiDa());
        return form;
    }

    private boolean isTerminal(String status) {
        return CLOSED.equals(status) || CANCELLED.equals(status);
    }
}
