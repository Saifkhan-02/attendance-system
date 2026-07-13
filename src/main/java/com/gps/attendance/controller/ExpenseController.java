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

        if (expense.getEmployeeId() == null) {
            throw new RuntimeException("Employee ID is required");
        }

        if (expense.getAmount() == null || expense.getAmount() <= 0) {
            throw new RuntimeException("Expense amount must be greater than 0");
        }

        if (expense.getExpenseDate() == null
                || expense.getExpenseDate().trim().isEmpty()) {
            throw new RuntimeException("Expense date is required");
        }

        if (expense.getExpenseType() == null
                || expense.getExpenseType().trim().isEmpty()) {
            throw new RuntimeException("Expense type is required");
        }

        if (repository.existsByEmployeeIdAndExpenseDateAndExpenseType(
                expense.getEmployeeId(),
                expense.getExpenseDate(),
                expense.getExpenseType())) {

            throw new RuntimeException(
                    "Expense already submitted for this date and type");
        }

        expense.setStatus("Pending");

        return repository.save(expense);
    }

    @GetMapping("/expense/history/{employeeId}")
    public List<Expense> getExpenseHistory(@PathVariable Long employeeId) {

        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    @GetMapping("/expenses/chart-data")
    public List<Object[]> getExpenseChartData() {

        List<Object[]> data
                = expenseRepository.getLast7DaysExpenseStats();

        java.util.Collections.reverse(data);

        return data;
    }

    @GetMapping("/admin/expenses")
    public List<Expense> getAllExpenses() {
        return repository.findAllByOrderByIdDesc();
    }
}
