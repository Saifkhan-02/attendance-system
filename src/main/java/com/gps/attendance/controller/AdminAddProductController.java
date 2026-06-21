package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.AdminAddProduct;
import com.gps.attendance.repository.AdminAddProductRepository;

@RestController
@CrossOrigin("*")
public class AdminAddProductController {

    @Autowired
    private AdminAddProductRepository repository;

    @PostMapping("/admin/save-product")
    public AdminAddProduct saveProduct(
            @RequestBody AdminAddProduct product) {

        return repository.save(product);
    }

    @GetMapping("/products")
    public List<AdminAddProduct> getProducts() {

        return repository.findAll();
    }
}