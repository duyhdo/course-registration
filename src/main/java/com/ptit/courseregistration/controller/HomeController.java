package com.ptit.courseregistration.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    return "redirect:/admin";
                }
                if ("ROLE_STUDENT".equals(authority.getAuthority())) {
                    return "redirect:/student";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "admin/home";
    }

    @GetMapping("/student")
    public String studentHome() {
        return "student/home";
    }
}
