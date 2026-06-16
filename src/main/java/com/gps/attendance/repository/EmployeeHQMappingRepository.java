package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.EmployeeHQMapping;

public interface EmployeeHQMappingRepository
        extends JpaRepository<EmployeeHQMapping, Long> {

    List<EmployeeHQMapping> findByEmployeeId(Long employeeId);

    List<EmployeeHQMapping> findByHqId(Long hqId);
}
