package com.gps.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gps.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeId(Long employeeId);

    List<Attendance> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<Attendance> findAllByOrderByIdDesc();

    long countByStatus(String status);

    long countByAttendanceDateAndStatus(
        LocalDate attendanceDate,
        String status
);

    long countByEmployeeIdAndAttendanceDateBetween(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate
    );

    boolean existsByEmployeeIdAndAttendanceDate(
        Long employeeId,
        java.time.LocalDate attendanceDate
);
@Query("""
        SELECT a.employeeId, COUNT(a)
        FROM Attendance a
        WHERE a.employeeId IN :employeeIds
        AND a.attendanceDate = :today
        GROUP BY a.employeeId
        """)
        List<Object[]> countTodayAttendanceByEmployees(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("today") LocalDate today);

List<Attendance> findByEmployeeIdAndAttendanceDateOrderByIdDesc(
        Long employeeId,
        LocalDate attendanceDate
);

List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByIdDesc(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate
);
}