package com.helu.internship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LeadViewController {

    @GetMapping("/leads")
    public String leadManagementPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "leads");
        return "lead/lead-management";
    }

    @GetMapping("/leads/{id}")
    public String leadDetailPage(@org.springframework.web.bind.annotation.PathVariable String id, org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "leads");
        model.addAttribute("leadId", id);
        return "lead/lead-detail";
    }
}
