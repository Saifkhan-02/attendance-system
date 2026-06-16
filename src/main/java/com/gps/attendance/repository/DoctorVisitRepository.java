package com.gps.attendance.repository;

import java.util.List;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gps.attendance.entity.DoctorVisit;

public interface DoctorVisitRepository extends JpaRepository<DoctorVisit, Long> {

    List<DoctorVisit> findByEmployeeId(Long employeeId);

    List<DoctorVisit> findAllByOrderByIdDesc(Pageable pageable);

    List<DoctorVisit> findByEmployeeNameContainingIgnoreCase(String keyword);

    long countByEmployeeIdAndVisitDate(Long employeeId, String visitDate);
    long countByEmployeeIdAndVisitDateStartingWith(Long employeeId, String month);

    @Query("""
        SELECT SUBSTRING(d.visitDate,1,7), COUNT(d)
        FROM DoctorVisit d
        GROUP BY SUBSTRING(d.visitDate,1,7)
        ORDER BY SUBSTRING(d.visitDate,1,7)
    """)
    List<Object[]> getMonthlyVisitStats();

    @Query("""
        SELECT d.visitDate, COUNT(d)
        FROM DoctorVisit d
        GROUP BY d.visitDate
        ORDER BY d.visitDate DESC
        LIMIT 7
    """)
    List<Object[]> getDailyVisitStats();

    // Count distinct doctors by employee and month
    @Query("""
    SELECT COUNT(DISTINCT d.doctorName)
    FROM DoctorVisit d
    WHERE d.employeeId = :employeeId
    AND d.visitDate LIKE CONCAT(:month, '%')
""")
long countDistinctDoctorByEmployeeAndMonth(Long employeeId, String month);

// Duplicate protection
Optional<DoctorVisit> findFirstByEmployeeIdAndDoctorNameIgnoreCaseAndVisitDate(
        Long employeeId,
        String doctorName,
        String visitDate
);
List<DoctorVisit> findByEmployeeIdAndVisitDateOrderByIdDesc(
        Long employeeId,
        String visitDate
);
List<DoctorVisit> findByEmployeeIdAndVisitDateOrderByVisitTimeDesc(
        Long employeeId,
        String visitDate
);

}