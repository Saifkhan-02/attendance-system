package com.gps.attendance.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gps.attendance.entity.Employee;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.service.CloudinaryService;


@RestController
@CrossOrigin("*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
private CloudinaryService cloudinaryService;

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
    @PutMapping("/employee/update-photo/{id}")
public ResponseEntity<?> updateProfilePhoto(
        @PathVariable Long id,
        @RequestBody Employee request) {

    Employee employee =
            repository.findById(id).orElse(null);

    if (employee == null) {
        return ResponseEntity.badRequest()
                .body("Employee Not Found");
    }

    employee.setProfileImage(request.getProfileImage());

    repository.save(employee);

    return ResponseEntity.ok(employee);
}

@PutMapping("/employee/update-profile/{id}")
public Employee updateEmployeeProfile(
        @PathVariable Long id,
        @RequestBody Employee updatedEmployee
) {
    Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    employee.setEmail(updatedEmployee.getEmail());
    employee.setMobile(updatedEmployee.getMobile());
    employee.setHeadquarters(updatedEmployee.getHeadquarters());

    return repository.save(employee);
}

@PutMapping("/employee/upload-photo/{id}")
public Employee uploadEmployeePhoto(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file
) throws IOException {

        System.out.println("Upload API Hit");
    System.out.println("Employee ID = " + id);
    System.out.println("File empty = " + file.isEmpty());
    System.out.println("File size = " + file.getSize());
    
    Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    String imageUrl = cloudinaryService.uploadProfileImage(file, id);

    employee.setProfileImage(imageUrl);

    return repository.save(employee);
}

@PutMapping("/employee/assign-headquarter")
public Employee assignHeadquarter(@RequestBody Map<String, Object> request) {

    Long employeeId = Long.valueOf(request.get("employeeId").toString());
    String headquarterName = request.get("headquarterName").toString();

    Employee employee = repository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    employee.setHeadquarters(headquarterName);

    return repository.save(employee);
}
}