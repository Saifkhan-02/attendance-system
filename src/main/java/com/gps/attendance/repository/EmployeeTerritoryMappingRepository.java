package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.EmployeeTerritoryMapping;

public interface EmployeeTerritoryMappingRepository
        extends JpaRepository<EmployeeTerritoryMapping, Long> {

            

    List<EmployeeTerritoryMapping> findByEmployeeId(Long employeeId);

    List<EmployeeTerritoryMapping> findByTerritoryNameIn(List<String> territoryNames);

    void deleteByEmployeeId(Long employeeId);

    List<EmployeeTerritoryMapping> findByEmployeeIdIn(
        List<Long> employeeIds
);
}