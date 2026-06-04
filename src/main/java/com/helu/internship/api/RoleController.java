package com.helu.internship.api;

import com.helu.internship.entity.RoleEntity;
import com.helu.internship.repo.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleRepo roleRepo;

    @GetMapping
    public List<RoleEntity> getAllRoles() {
        return roleRepo.findAll();
    }
}
