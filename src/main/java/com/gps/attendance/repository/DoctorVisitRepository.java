package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.DoctorVisit;

public interface DoctorVisitRepository
        extends JpaRepository<DoctorVisit, Long> {

    List<DoctorVisit> findByEmployeeId(Long employeeId);
}