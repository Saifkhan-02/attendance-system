package com.gps.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
}