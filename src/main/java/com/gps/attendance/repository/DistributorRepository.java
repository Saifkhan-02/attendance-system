package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gps.attendance.entity.Distributor;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {

    List<Distributor> findByHeadquartersIgnoreCaseAndStatus(
            String headquarters,
            String status
    );

    @Query("SELECT DISTINCT d.headquarters FROM Distributor d WHERE d.status = 'Active'")
List<String> findDistinctActiveHeadquarters();

List<Distributor> findByHeadquartersAndStatus(
        String headquarters,
        String status
);

}