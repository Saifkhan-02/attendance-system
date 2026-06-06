package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Expense;
import com.gps.attendance.repository.ExpenseRepository;

@RestController
@CrossOrigin("*")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseRepository repository;

    @PostMapping("/expense/submit")
    public Expense submitExpense(@RequestBody Expense expense) {

        expense.setStatus("Pending");

        return repository.save(expense);
    }

    @GetMapping("/expense/history/{employeeId}")
    public List<Expense> getExpenseHistory(@PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }

    @GetMapping("/expenses/chart-data")
    public List<Object[]> getExpenseChartData() {

        return expenseRepository
                .getMonthlyExpenseStats();
    }
}
