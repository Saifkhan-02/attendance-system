package com.gps.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gps.attendance.entity.DoctorVisit;

public interface DoctorVisitRepository extends JpaRepository<DoctorVisit, Long> {

    List<DoctorVisit> findByEmployeeId(Long employeeId);

    List<DoctorVisit> findByEmployeeIdOrderByIdDesc(Long employeeId);

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

    @Query("SELECT COUNT(d) FROM DoctorVisit d WHERE d.employeeId = :employeeId")
    long countByEmployeeId(@Param("employeeId") Long employeeId);

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

    List<DoctorVisit> findByVisitCategoryOrderByVisitDateDesc(
            String visitCategory
    );

    List<DoctorVisit> findByVisitCategoryAndRouteNameIgnoreCase(
            String visitCategory,
            String routeName
    );

    List<DoctorVisit> findByVisitCategoryAndRouteNameIgnoreCaseOrderByVisitDateDesc(
            String visitCategory,
            String routeName
    );

    @Query("""
    SELECT d.visitDate, COUNT(d)
    FROM DoctorVisit d
    GROUP BY d.visitDate
    ORDER BY d.visitDate DESC
""")
    List<Object[]> getWeeklyVisitStats();

    @Query(
            value = """
    SELECT COUNT(*)
    FROM doctor_visit
    WHERE visit_date LIKE CONCAT(
        TO_CHAR(CURRENT_DATE,'YYYY-MM'),
        '%'
    )
    """,
            nativeQuery = true
    )
    long getCurrentMonthVisitCount();

    @Query(
            value = """
SELECT COUNT(*)
FROM doctor_visit
WHERE visit_date = TO_CHAR(CURRENT_DATE,'YYYY-MM-DD')
AND visit_category = :category
""",
            nativeQuery = true
    )
    long getTodayVisitCount(
            @Param("category") String category
    );

    List<DoctorVisit> findByEmployeeIdInOrderByIdDesc(List<Long> employeeIds);

    @Query("""
SELECT d.employeeId, COUNT(d)
FROM DoctorVisit d
WHERE d.employeeId IN :employeeIds
AND d.visitDate = :today
GROUP BY d.employeeId
""")
    List<Object[]> countTodayVisitsByEmployees(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("today") String today
    );

    List<DoctorVisit> findByEmployeeIdAndVisitDateStartingWithOrderByIdDesc(
            Long employeeId,
            String month
    );

// Doctor Search (Autocomplete + Auto Fill)
    List<DoctorVisit> findByEmployeeIdAndDoctorNameContainingIgnoreCaseOrderByDoctorNameAsc(
            Long employeeId,
            String doctorName
    );

    List<DoctorVisit> findByEmployeeIdAndDoctorNameIgnoreCaseOrderByHospitalNameAsc(
            Long employeeId,
            String doctorName
    );
// Total Visit Count

    long countByDoctorNameIgnoreCase(String doctorName);

    List<DoctorVisit> findByEmployeeIdAndVisitDateBetweenOrderByIdDesc(
            Long employeeId,
            String fromDate,
            String toDate
    );

    List<DoctorVisit>
            findByEmployeeIdAndVisitCategoryAndDoctorNameContainingIgnoreCaseOrderByDoctorNameAsc(
                    Long employeeId,
                    String visitCategory,
                    String doctorName
            );

    List<DoctorVisit>
            findByEmployeeIdAndVisitCategoryAndDoctorNameIgnoreCaseOrderByHospitalNameAsc(
                    Long employeeId,
                    String visitCategory,
                    String doctorName
            );

    List<DoctorVisit>
            findByEmployeeIdAndVisitCategoryOrderByIdDesc(
                    Long employeeId,
                    String visitCategory
            );

    long countByEmployeeIdAndVisitCategory(
            Long employeeId,
            String visitCategory
    );

    List<DoctorVisit>
            findByEmployeeIdAndVisitCategoryOrderByVisitDateDesc(
                    Long employeeId,
                    String visitCategory
            );

  @Query(value = """
SELECT COUNT(
DISTINCT CONCAT(
LOWER(TRIM(doctor_name)),
'|',
LOWER(TRIM(COALESCE(hospital_name,'')))
))
FROM doctor_visit
WHERE UPPER(TRIM(visit_category)) = UPPER(TRIM(:category))
AND doctor_name IS NOT NULL
AND TRIM(doctor_name) <> ''
""", nativeQuery = true)
long countUniqueByCategory(@Param("category") String category);

@Query(value = """
SELECT COUNT(
DISTINCT CONCAT(
LOWER(TRIM(doctor_name)),
'|',
LOWER(TRIM(COALESCE(hospital_name,''))),
'|',
UPPER(TRIM(visit_category))
))
FROM doctor_visit
WHERE doctor_name IS NOT NULL
AND TRIM(doctor_name) <> ''
""", nativeQuery = true)
long countAllUniqueParties();

    @Query("""
SELECT DISTINCT d.employeeName
FROM DoctorVisit d
WHERE d.visitCategory = :category
AND d.employeeName IS NOT NULL
AND TRIM(d.employeeName) <> ''
ORDER BY d.employeeName
""")
    List<String> findAllEmployeeNames(
            @Param("category") String category
    );

    @Query("""
SELECT DISTINCT d.doctorName
FROM DoctorVisit d
WHERE d.visitCategory = :category
AND d.doctorName IS NOT NULL
AND TRIM(d.doctorName) <> ''
ORDER BY d.doctorName
""")
    List<String> findAllDoctorNames(
            @Param("category") String category
    );

   @Query(
    value = """
SELECT d
FROM DoctorVisit d
WHERE d.visitCategory = :visitCategory
AND (:employeeName IS NULL OR :employeeName=''
     OR LOWER(d.employeeName) LIKE LOWER(CONCAT('%',:employeeName,'%')))
AND (:doctorName IS NULL OR :doctorName=''
     OR LOWER(d.doctorName) LIKE LOWER(CONCAT('%',:doctorName,'%')))
AND (:visitDate IS NULL OR :visitDate=''
     OR d.visitDate = :visitDate)
ORDER BY d.id DESC
""",
    countQuery = """
SELECT COUNT(d)
FROM DoctorVisit d
WHERE d.visitCategory = :visitCategory
AND (:employeeName IS NULL OR :employeeName=''
     OR LOWER(d.employeeName) LIKE LOWER(CONCAT('%',:employeeName,'%')))
AND (:doctorName IS NULL OR :doctorName=''
     OR LOWER(d.doctorName) LIKE LOWER(CONCAT('%',:doctorName,'%')))
AND (:visitDate IS NULL OR :visitDate=''
     OR d.visitDate = :visitDate)
"""
)

    Page<DoctorVisit> findDoctorVisits(
            @Param("visitCategory") String visitCategory,
            @Param("employeeName") String employeeName,
            @Param("doctorName") String doctorName,
            @Param("visitDate") String visitDate,
            Pageable pageable
    );

    @Query(
    value = """
SELECT d
FROM DoctorVisit d
WHERE d.id IN (
    SELECT MAX(d2.id)
    FROM DoctorVisit d2
    WHERE d2.visitCategory = :visitCategory
    GROUP BY
        LOWER(TRIM(d2.doctorName)),
        LOWER(TRIM(COALESCE(d2.hospitalName,'')))
)
AND (:employeeName IS NULL OR :employeeName=''
     OR LOWER(d.employeeName) LIKE LOWER(CONCAT('%',:employeeName,'%')))
AND (:doctorName IS NULL OR :doctorName=''
     OR LOWER(d.doctorName) LIKE LOWER(CONCAT('%',:doctorName,'%')))
     AND (
    :headquarter IS NULL
    OR :headquarter = ''
    OR UPPER(TRIM(d.headquarter)) = UPPER(TRIM(:headquarter))
)

AND (
    :route IS NULL
    OR :route = ''
    OR UPPER(TRIM(d.routeName)) = UPPER(TRIM(:route))
)
ORDER BY d.id DESC
""",
countQuery = """
SELECT COUNT(*)
FROM DoctorVisit d
WHERE d.id IN (
    SELECT MAX(d2.id)
    FROM DoctorVisit d2
    WHERE d2.visitCategory = :visitCategory
    GROUP BY
        LOWER(TRIM(d2.doctorName)),
        LOWER(TRIM(COALESCE(d2.hospitalName,'')))
)
AND (:employeeName IS NULL OR :employeeName=''
     OR LOWER(d.employeeName) LIKE LOWER(CONCAT('%',:employeeName,'%')))
AND (:doctorName IS NULL OR :doctorName=''
     OR LOWER(d.doctorName) LIKE LOWER(CONCAT('%',:doctorName,'%')))
    AND (
    :headquarter IS NULL
    OR :headquarter = ''
    OR UPPER(TRIM(d.headquarter)) = UPPER(TRIM(:headquarter))
)

AND (
    :route IS NULL
    OR :route = ''
    OR UPPER(TRIM(d.routeName)) = UPPER(TRIM(:route))
)
"""
)
Page<DoctorVisit> findUniqueDoctors(
        @Param("visitCategory") String visitCategory,
        @Param("employeeName") String employeeName,
        @Param("doctorName") String doctorName,
        @Param("headquarter") String headquarter,
        @Param("route") String route,
        Pageable pageable
);
}
