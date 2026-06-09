package com.helu.internship.api;

import com.helu.internship.entity.ProductEntity;
import com.helu.internship.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepo productRepo;

    @GetMapping
    public List<ProductEntity> getAll() {
        return productRepo.findAll();
    }
}
