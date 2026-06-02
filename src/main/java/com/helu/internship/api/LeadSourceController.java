package com.helu.internship.api;

import com.helu.internship.entity.LeadSourceEntity;
import com.helu.internship.repo.LeadSourceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lead-sources")
@RequiredArgsConstructor
public class LeadSourceController {
    private final LeadSourceRepo leadSourceRepo;

    @GetMapping
    public List<LeadSourceEntity> getAll() {
        return leadSourceRepo.findAll();
    }
}
