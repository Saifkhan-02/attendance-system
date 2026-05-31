package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Employee;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Employee findByUsernameAndPassword(
            String username,
            String password
    );
}