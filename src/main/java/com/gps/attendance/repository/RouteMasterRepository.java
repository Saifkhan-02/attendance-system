package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gps.attendance.entity.RouteMaster;

public interface RouteMasterRepository extends JpaRepository<RouteMaster, Long> {

    List<RouteMaster> findByHeadquarterName(String headquarterName);

    List<RouteMaster> findByHeadquarterNameAndStatus(String headquarterName, String status);

    @Query("""
SELECT DISTINCT r.headquarterName
FROM RouteMaster r
WHERE r.status = 'Active'
ORDER BY r.headquarterName
""")
List<String> getAllHeadquarters();

List<RouteMaster> findByHeadquarterNameAndStatusOrderByRouteNameAsc(
        String headquarterName,
        String status
);
}
