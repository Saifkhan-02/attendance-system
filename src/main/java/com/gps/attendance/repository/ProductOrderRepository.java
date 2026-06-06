package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.ProductOrder;

@Repository
public interface ProductOrderRepository
        extends JpaRepository<ProductOrder, Long> {

    List<ProductOrder> findByEmployeeId(Long employeeId);
}