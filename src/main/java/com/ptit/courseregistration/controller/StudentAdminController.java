package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.dto.StudentForm;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.service.StudentService;
import com.ptit.courseregistration.web.PaginationSupport;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/students")
public class StudentAdminController {

    private static final String FORM_VIEW = "admin/students/form";

    private final StudentService studentService;
    private final KhoaRepository khoaRepository;

    public StudentAdminController(StudentService studentService, KhoaRepository khoaRepository) {
        this.studentService = studentService;
        this.khoaRepository = khoaRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        boolean blank = keyword == null || keyword.isBlank();
        Sort sort = Sort.by(Sort.Direction.ASC, "maSV");
        Page<SinhVien> students = PaginationSupport.fetchPage(page, size, sort,
                pageable -> blank ? studentService.findAll(pageable) : studentService.search(keyword, pageable));
        model.addAttribute("students", students.getContent());
        model.addAttribute("pagination",
                PaginationSupport.view(students, "/admin/students", PaginationSupport.filters("keyword", keyword)));
        model.addAttribute("keyword", keyword);
        return "admin/students/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("studentForm", new StudentForm());
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid StudentForm studentForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                studentService.create(studentForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm sinh viên thành công.");
                return "redirect:/admin/students";
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @GetMapping("/{maSV}")
    public String detail(@PathVariable String maSV, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("student", studentService.findById(maSV));
            return "admin/students/detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/students";
        }
    }

    @GetMapping("/{maSV}/edit")
    public String editForm(@PathVariable String maSV, Model model, RedirectAttributes redirectAttributes) {
        SinhVien sv;
        try {
            sv = studentService.findById(maSV);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/students";
        }
        StudentForm form = new StudentForm();
        form.setMaSV(sv.getMaSV());
        form.setMaKhoa(sv.getKhoa().getMaKhoa());
        form.setHoTen(sv.getHoTen());
        form.setNgaySinh(sv.getNgaySinh());
        form.setGioiTinh(sv.getGioiTinh());
        form.setEmail(sv.getEmail());
        form.setSoDienThoai(sv.getSoDienThoai());
        form.setDiaChi(sv.getDiaChi());
        form.setKhoaHoc(sv.getKhoaHoc());
        model.addAttribute("studentForm", form);
        prepareForm(model, true, sv.getMaSV());
        return FORM_VIEW;
    }

    @PostMapping("/{maSV}/edit")
    public String update(@PathVariable String maSV, @Valid StudentForm studentForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // The identifier always comes from the path, never from the submitted form
        studentForm.setMaSV(maSV);
        if (!result.hasErrors()) {
            try {
                studentService.update(maSV, studentForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sinh viên thành công.");
                return "redirect:/admin/students/" + maSV;
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        prepareForm(model, true, maSV);
        return FORM_VIEW;
    }

    @PostMapping("/{maSV}/toggle-status")
    public String toggleStatus(@PathVariable String maSV, RedirectAttributes redirectAttributes) {
        try {
            studentService.toggleStatus(maSV);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái sinh viên " + maSV + ".");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/students";
    }

    private void prepareForm(Model model, boolean editMode, String maSV) {
        model.addAttribute("khoaList", khoaRepository.findByTrangThai("ACTIVE"));
        model.addAttribute("editMode", editMode);
        model.addAttribute("maSV", maSV);
    }
}
