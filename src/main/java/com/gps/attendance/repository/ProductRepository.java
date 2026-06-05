package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.Product;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long> {
}