package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.dto.MonHocForm;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.service.MonHocService;
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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/subjects")
public class MonHocAdminController {

    private static final String FORM_VIEW = "admin/subjects/form";

    private final MonHocService monHocService;
    private final KhoaRepository khoaRepository;

    public MonHocAdminController(MonHocService monHocService, KhoaRepository khoaRepository) {
        this.monHocService = monHocService;
        this.khoaRepository = khoaRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        boolean blank = keyword == null || keyword.isBlank();
        Sort sort = Sort.by(Sort.Direction.ASC, "maMonHoc");
        Page<MonHoc> subjects = PaginationSupport.fetchPage(page, size, sort,
                pageable -> blank ? monHocService.findAll(pageable) : monHocService.search(keyword, pageable));
        model.addAttribute("subjects", subjects.getContent());
        model.addAttribute("pagination",
                PaginationSupport.view(subjects, "/admin/subjects", PaginationSupport.filters("keyword", keyword)));
        model.addAttribute("keyword", keyword);
        return "admin/subjects/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("monHocForm", new MonHocForm());
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid MonHocForm monHocForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                monHocService.create(monHocForm);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm môn học thành công.");
                return "redirect:/admin/subjects";
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        prepareForm(model, false, null);
        return FORM_VIEW;
    }

    @GetMapping("/{maMonHoc}")
    public String detail(@PathVariable String maMonHoc, Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("subject", monHocService.findById(maMonHoc));
            return "admin/subjects/detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/subjects";
        }
    }

    @GetMapping("/{maMonHoc}/edit")
    public String editForm(@PathVariable String maMonHoc, Model model,
                           RedirectAttributes redirectAttributes) {
        MonHoc monHoc;
        try {
            monHoc = monHocService.findById(maMonHoc);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/subjects";
        }

        MonHocForm form = new MonHocForm();
        form.setMaMonHoc(monHoc.getMaMonHoc());
        form.setMaKhoa(monHoc.getKhoa().getMaKhoa());
        form.setTenMonHoc(monHoc.getTenMonHoc());
        form.setSoTinChi(monHoc.getSoTinChi());
        form.setSoTietLyThuyet(monHoc.getSoTietLyThuyet());
        form.setSoTietThucHanh(monHoc.getSoTietThucHanh());
        form.setMoTa(monHoc.getMoTa());
        model.addAttribute("monHocForm", form);
        prepareForm(model, true, monHoc.getMaMonHoc(), monHoc.getKhoa());
        return FORM_VIEW;
    }

    @PostMapping("/{maMonHoc}/edit")
    public String update(@PathVariable String maMonHoc, @Valid MonHocForm monHocForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        MonHoc currentMonHoc;
        try {
            currentMonHoc = monHocService.findById(maMonHoc);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/subjects";
        }

        monHocForm.setMaMonHoc(maMonHoc);
        if (!result.hasErrors()) {
            try {
                monHocService.update(maMonHoc, monHocForm);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật môn học thành công.");
                return "redirect:/admin/subjects/" + maMonHoc;
            } catch (BusinessException ex) {
                result.reject("business.error", ex.getMessage());
            }
        }
        prepareForm(model, true, maMonHoc, currentMonHoc.getKhoa());
        return FORM_VIEW;
    }

    @PostMapping("/{maMonHoc}/toggle-status")
    public String toggleStatus(@PathVariable String maMonHoc, RedirectAttributes redirectAttributes) {
        try {
            monHocService.toggleStatus(maMonHoc);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái môn học.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    private void prepareForm(Model model, boolean editMode, String maMonHoc) {
        prepareForm(model, editMode, maMonHoc, null);
    }

    private void prepareForm(Model model, boolean editMode, String maMonHoc, Khoa currentKhoa) {
        List<Khoa> khoaList = new ArrayList<>(khoaRepository.findByTrangThai("ACTIVE"));
        if (editMode && currentKhoa != null && "INACTIVE".equals(currentKhoa.getTrangThai())
                && khoaList.stream().noneMatch(khoa -> khoa.getMaKhoa().equals(currentKhoa.getMaKhoa()))) {
            khoaList.add(currentKhoa);
        }
        model.addAttribute("khoaList", khoaList);
        model.addAttribute("editMode", editMode);
        model.addAttribute("maMonHoc", maMonHoc);
    }
}
