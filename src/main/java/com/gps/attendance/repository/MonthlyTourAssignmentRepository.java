package com.gps.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.gps.attendance.entity.MonthlyTourAssignment;

public interface MonthlyTourAssignmentRepository
        extends JpaRepository<MonthlyTourAssignment, Long> {

    List<MonthlyTourAssignment>
    findByEmployeeIdAndMonth(
            Long employeeId,
            String month);

    boolean existsByEmployeeIdAndTourDate(
            Long employeeId,
            LocalDate tourDate);

    boolean existsByEmployeeIdAndMonth(Long employeeId, String month);

    @Transactional
    void deleteByEmployeeIdAndMonth(Long employeeId, String month);
}