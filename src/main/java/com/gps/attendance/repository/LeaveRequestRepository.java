package com.gps.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gps.attendance.entity.LeaveRequest;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<LeaveRequest> findAllByOrderByIdDesc();

    long countByStatus(String status);

    long countByFromDateAndStatus(
            java.time.LocalDate fromDate,
            String status
    );

    @Query("""
SELECT COUNT(l)
FROM LeaveRequest l
WHERE :today BETWEEN l.fromDate AND l.toDate
AND l.status = :status
""")
long countEmployeesOnLeave(
        @Param("today") LocalDate today,
        @Param("status") String status
);

@Query("""
SELECT COUNT(l)
FROM LeaveRequest l
WHERE l.employeeId = :employeeId
AND l.status = 'Approved'
AND YEAR(l.fromDate) = :year
AND MONTH(l.fromDate) = :month
""")
long countEmployeeLeaveDays(
        @Param("employeeId") Long employeeId,
        @Param("month") int month,
        @Param("year") int year
);

List<LeaveRequest> findByEmployeeIdAndStatus(
        Long employeeId,
        String status
);


List<LeaveRequest> findByEmployeeIdAndFromDateBetweenOrderByIdDesc(
        Long employeeId,
        LocalDate fromDate,
        LocalDate toDate
);

}