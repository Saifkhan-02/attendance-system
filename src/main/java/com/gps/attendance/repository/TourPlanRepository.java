package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gps.attendance.entity.TourPlan;

public interface TourPlanRepository
        extends JpaRepository<TourPlan, Long> {

    List<TourPlan> findByEmployeeId(Long employeeId);

    List<TourPlan> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<TourPlan> findByStatus(String status);

    List<TourPlan> findByEmployeeIdAndMonth(Long employeeId, String month);

    List<TourPlan> findByEmployeeIdAndMonthOrderByIdDesc(
            Long employeeId,
            String month
    );

    List<TourPlan> findAllByOrderByIdDesc();

    long countByStatus(String status);

    boolean existsByEmployeeIdAndMonth(Long employeeId, String month);

    boolean existsByEmployeeIdAndTravelDate(
            Long employeeId,
            java.time.LocalDate travelDate
    );

    List<TourPlan> findByEmployeeIdAndTravelDateBetweenOrderByIdDesc(
        Long employeeId,
        java.time.LocalDate fromDate,
        java.time.LocalDate toDate
);

long countByEmployeeIdAndTravelDate(
        Long employeeId,
        java.time.LocalDate travelDate
);
    @Query(value = """
SELECT
    travel_date,
    SUM(total_expense)
FROM tour_plan
WHERE total_expense IS NOT NULL
GROUP BY travel_date
ORDER BY travel_date DESC
LIMIT 7
""", nativeQuery = true)
List<Object[]> getLast7DaysExpenseChart();

}
