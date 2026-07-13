package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gps.attendance.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByEmployeeId(Long employeeId);

    List<Expense> findByEmployeeIdOrderByIdDesc(Long employeeId);

    boolean existsByEmployeeIdAndExpenseDateAndExpenseType(
            Long employeeId,
            String expenseDate,
            String expenseType
    );

    List<Expense> findAllByOrderByIdDesc();

    @Query(value = """
SELECT
SUBSTRING(expense_date,1,7) as month,
SUM(amount)
FROM expenses
GROUP BY month
ORDER BY month
""", nativeQuery = true)
    List<Object[]> getMonthlyExpenseStats();

    @Query(value = """
SELECT
expense_date,
SUM(amount)
FROM expenses
GROUP BY expense_date
ORDER BY expense_date DESC
LIMIT 7
""", nativeQuery = true)
    List<Object[]> getLast7DaysExpenseStats();

    @Query("""
SELECT COALESCE(SUM(e.amount),0)
FROM Expense e
WHERE e.employeeId = :employeeId
""")
    Double getTotalExpenseByEmployee(Long employeeId);

@Query("""
SELECT e.employeeId, COALESCE(SUM(e.amount),0)
FROM Expense e
WHERE e.employeeId IN :employeeIds
GROUP BY e.employeeId
""")
List<Object[]> getExpenseSummaryByEmployees(
        @Param("employeeIds") List<Long> employeeIds
);

List<Expense> findByEmployeeIdAndExpenseDateOrderByIdDesc(
        Long employeeId,
        String expenseDate
);

List<Expense> findByEmployeeIdAndExpenseDateStartingWithOrderByIdDesc(
        Long employeeId,
        String month
);

@Query("""
SELECT COALESCE(SUM(e.amount),0)
FROM Expense e
WHERE e.employeeId = :employeeId
AND e.expenseDate = :today
""")
Double getTodayExpenseByEmployee(
        @Param("employeeId") Long employeeId,
        @Param("today") String today
);
List<Expense> findByEmployeeIdAndExpenseDateBetweenOrderByIdDesc(
        Long employeeId,
        String fromDate,
        String toDate
);

}

