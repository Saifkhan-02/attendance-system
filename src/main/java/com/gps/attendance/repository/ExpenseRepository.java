package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByEmployeeId(Long employeeId);

    @Query(value = """
SELECT
SUBSTRING(expense_date,1,7) as month,
SUM(amount)
FROM expenses
GROUP BY month
ORDER BY month
""", nativeQuery = true)
    List<Object[]> getMonthlyExpenseStats();
}
