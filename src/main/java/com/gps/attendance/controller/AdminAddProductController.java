package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.AdminAddProduct;
import com.gps.attendance.repository.AdminAddProductRepository;

@RestController
@CrossOrigin("*")
public class AdminAddProductController {

    @Autowired
    private AdminAddProductRepository repository;

    @PostMapping("/admin/product/save")
    public AdminAddProduct saveProduct(
            @RequestBody AdminAddProduct product) {

        if (product.getProductName() == null
                || product.getProductName().trim().isEmpty()) {
            throw new RuntimeException("Product name is required");
        }
        product.setProductName(
                product.getProductName().trim());

        if (repository.findByProductNameIgnoreCase(
                product.getProductName()) != null) {

            throw new RuntimeException(
                    "Product already exists");
        }

        return repository.save(product);
    }

    @GetMapping("/admin/products")
    public List<AdminAddProduct> getProducts() {

        return repository.findAllByOrderByIdDesc();
    }

    @PutMapping("/admin/update-product/{id}")
    public AdminAddProduct updateProduct(
            @PathVariable Long id,
            @RequestBody AdminAddProduct request) {

        AdminAddProduct product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getProductName() == null
                || request.getProductName().trim().isEmpty()) {
            throw new RuntimeException("Product name is required");
        }

        product.setProductName(
                request.getProductName().trim());

        product.setStatus(request.getStatus());

        return repository.save(product);
    }

    @DeleteMapping("/admin/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id) {

        repository.deleteById(id);

        return "Product Deleted Successfully";
    }
}
