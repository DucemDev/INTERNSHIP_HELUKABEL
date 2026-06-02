package com.helu.internship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LeadViewController {

    @GetMapping("/leads")
    public String leadManagementPage() {
        return "lead/lead-management";
    }
}
