package com.gps.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Headquarter;

public interface HeadquarterRepository extends JpaRepository<Headquarter, Long> {
    List<Headquarter> findByStatus(String status);
    Optional<Headquarter> findByHeadquarterNameIgnoreCase(String headquarterName);
}