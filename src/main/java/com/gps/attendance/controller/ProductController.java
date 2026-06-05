package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Product;
import com.gps.attendance.repository.ProductRepository;

@RestController
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @PostMapping("/admin/save-product")
    public Product saveProduct(
            @RequestBody Product product) {

        return repository.save(product);
    }

    @GetMapping("/products")
    public List<Product> getProducts() {

        return repository.findAll();
    }
}