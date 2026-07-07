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
<<<<<<< HEAD
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
=======

@Query("""
SELECT a
FROM Attendance a
WHERE a.employeeId = :employeeId
AND YEAR(a.attendanceDate) = :year
AND MONTH(a.attendanceDate) = :month
ORDER BY a.attendanceDate DESC
""")
List<Attendance> findMonthlyAttendance(
        @Param("employeeId") Long employeeId,
        @Param("month") int month,
        @Param("year") int year
);

@Query("""
SELECT COUNT(a)
FROM Attendance a
WHERE a.employeeId = :employeeId
AND YEAR(a.attendanceDate) = :year
AND MONTH(a.attendanceDate) = :month
AND a.status = 'Present'
""")
long countPresentDays(
        @Param("employeeId") Long employeeId,
        @Param("month") int month,
        @Param("year") int year
);

>>>>>>> 503dee86bc0ffa31ddf8813ac47b58c6957f6a34
}