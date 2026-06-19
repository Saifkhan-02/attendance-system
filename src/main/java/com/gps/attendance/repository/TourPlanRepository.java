package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.TourPlan;

public interface TourPlanRepository
        extends JpaRepository<TourPlan, Long> {

    List<TourPlan> findByEmployeeId(Long employeeId);

    List<TourPlan> findByStatus(String status);

    long countByStatus(String status);
    
    List<TourPlan> findByEmployeeIdAndMonth(Long employeeId, String month);

boolean existsByEmployeeIdAndMonth(Long employeeId, String month);

boolean existsByEmployeeIdAndTravelDate(
        Long employeeId,
        java.time.LocalDate travelDate
);

}