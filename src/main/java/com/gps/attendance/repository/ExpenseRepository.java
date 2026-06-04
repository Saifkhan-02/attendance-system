package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByEmployeeId(Long employeeId);
}