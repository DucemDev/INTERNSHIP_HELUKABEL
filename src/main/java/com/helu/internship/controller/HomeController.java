package com.helu.internship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String adminPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "admin");
        return "admin-page";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "dashboard");
        return "dashboard/dashboard-home";
    }

    @GetMapping("/dashboard/underserved-segments")
    public String underServedSegmentsPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "underserved-segments");
        return "dashboard/Dashboard-underserved-segments";
    }

    @GetMapping("/staff")
    public String staffPage() {
        return "staff-page";
    }
}
