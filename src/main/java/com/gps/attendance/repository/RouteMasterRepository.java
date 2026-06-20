package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.RouteMaster;

public interface RouteMasterRepository extends JpaRepository<RouteMaster, Long> {

    List<RouteMaster> findByHeadquarterName(String headquarterName);

    List<RouteMaster> findByHeadquarterNameAndStatus(String headquarterName, String status);
}