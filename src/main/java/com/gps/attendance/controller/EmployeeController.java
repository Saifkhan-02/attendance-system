package com.gps.attendance.controller;

import com.gps.attendance.entity.Employee;
import com.gps.attendance.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository repository;

    @PostMapping("/register")
    public String registerEmployee(
            @RequestBody Employee employee) {

        repository.save(employee);

        return "Employee Registered Successfully";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Employee employee) {

        Employee emp =
                repository.findByUsernameAndPassword(
                        employee.getUsername(),
                        employee.getPassword());

        if(emp != null){

            return ResponseEntity.ok(emp);

        }

        return ResponseEntity
                .badRequest()
                .body("Invalid Username or Password");
    }
}