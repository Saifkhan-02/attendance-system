package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Employee findByUsernameAndPassword(
            String username,
            String password);

    Employee findByUsername(String username);

    

    Employee findByEmail(String email);

    Employee findByMobile(String mobile);

    List<Employee> findAllByOrderByNameAsc();
    List<Employee> findAllByOrderByIdDesc();
}
