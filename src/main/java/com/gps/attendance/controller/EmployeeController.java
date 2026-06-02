package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Employee;
import com.gps.attendance.repository.EmployeeRepository;

@RestController
@CrossOrigin("*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository repository;

    @PostMapping("/register")
    public String registerEmployee(
            @RequestBody Employee employee) {
                
                
    // System.out.println("========== REGISTER API HIT ==========");
    // System.out.println(employee.getName());
    // System.out.println(employee.getUsername());


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

    @GetMapping("/employee/{id}")
public ResponseEntity<?> getEmployeeById(
        @PathVariable Long id) {

    Employee employee =
            repository.findById(id)
                    .orElse(null);

    if(employee == null) {

        return ResponseEntity
                .badRequest()
                .body("Employee Not Found");
    }

    return ResponseEntity.ok(employee);
}

    @GetMapping("/get-employees")
    public List<Employee> getEmployees() {
          return repository.findAll();
          }
}