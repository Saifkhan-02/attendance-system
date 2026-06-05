package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByEmployeeId(Long employeeId);
}