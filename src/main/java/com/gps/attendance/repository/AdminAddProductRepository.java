package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.AdminAddProduct;

@Repository
public interface AdminAddProductRepository
        extends JpaRepository<AdminAddProduct, Long> {
}