package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.AdminAddProduct;

@Repository
public interface AdminAddProductRepository
        extends JpaRepository<AdminAddProduct, Long> {

    AdminAddProduct findByProductNameIgnoreCase(String productName);

    List<AdminAddProduct> findAllByOrderByIdDesc();
}