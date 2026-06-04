package com.helu.internship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/users")
    public String userManagementPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "users");
        return "user/user-management";
    }

    @GetMapping("/users/{id}")
    public String userDetailPage(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id, org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "users");
        model.addAttribute("userId", id);
        return "user/user-detail";
    }
}
