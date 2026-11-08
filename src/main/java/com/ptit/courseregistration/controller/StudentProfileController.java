package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentProfileController {

    private final StudentService studentService;

    public StudentProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("student", studentService.findById(authentication.getName()));
            return "student/profile";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student";
        }
    }
}
