package com.gps.attendance.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
// import java.util.ArrayList;
// import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gps.attendance.dto.ChangePasswordRequest;
import com.gps.attendance.dto.EmployeeRequest;
import com.gps.attendance.entity.Employee;
import com.gps.attendance.entity.EmployeeHQMapping;
import com.gps.attendance.entity.Headquarter;
import com.gps.attendance.repository.DoctorVisitRepository;
import com.gps.attendance.repository.EmployeeHQMappingRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.HeadquarterRepository;
import com.gps.attendance.service.CloudinaryService;

@RestController
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private CloudinaryService cloudinaryService;


    @Autowired
    private DoctorVisitRepository doctorVisitRepository;

    @Autowired
    private EmployeeHQMappingRepository mappingRepository;

    @Autowired
    private HeadquarterRepository headquarterRepository;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Employee employee) {

    Employee emp = repository.findByUsernameAndPassword(
            employee.getUsername(),
            employee.getPassword()
    );

    if (emp != null) {

        String token = UUID.randomUUID().toString();

        emp.setSessionToken(token);
        repository.save(emp);

        Map<String, Object> response = new HashMap<>();

        response.put("role", emp.getRole());
        response.put("id", emp.getId());
        response.put("name", emp.getName());
        response.put("username", emp.getUsername());
        response.put("email", emp.getEmail());
        response.put("headquarters", emp.getHeadquarters());
        response.put("profileImage", emp.getProfileImage());
        response.put("sessionToken", token);

        return ResponseEntity.ok(response);
    }

    return ResponseEntity
            .badRequest()
            .body("Invalid Username or Password");
}
    @GetMapping("/employee/{id}")
    public ResponseEntity<?> getEmployeeById(
            @PathVariable Long id) {

        Employee employee
                = repository.findById(id)
                        .orElse(null);

        if (employee == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Employee Not Found");
        }

        return ResponseEntity.ok(employee);
    }

 @GetMapping("/get-employees")
public List<Employee> getEmployees() {
    return repository.findAllByOrderByNameAsc();
}

    @PutMapping("/employee/update-photo/{id}")
    public ResponseEntity<?> updateProfilePhoto(
            @PathVariable Long id,
            @RequestBody Employee request) {

        Employee employee
                = repository.findById(id).orElse(null);

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

    // Editable Fields
    employee.setEmail(updatedEmployee.getEmail());
    employee.setMobile(updatedEmployee.getMobile());
    employee.setRole(updatedEmployee.getRole());
    employee.setReportingManager(updatedEmployee.getReportingManager());
    employee.setHeadquarters(updatedEmployee.getHeadquarters());
    employee.setStatus(updatedEmployee.getStatus());

    return repository.save(employee);
}

    @PutMapping("/employee/upload-photo/{id}")
    public Employee uploadEmployeePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String imageUrl = cloudinaryService.uploadProfileImage(file, id);

        employee.setProfileImage(imageUrl);

        return repository.save(employee);
    }

    @PostMapping("/register")
    public String registerEmployee(@RequestBody Employee employee) {

        if (employee.getUsername() == null
                || employee.getUsername().trim().isEmpty()) {
            return "Username is required";
        }

        employee.setUsername(employee.getUsername().trim());

        if (repository.findByUsername(employee.getUsername()) != null) {
            return "Username already exists";
        }

        if (employee.getEmail() != null
                && !employee.getEmail().trim().isEmpty()
                && repository.findByEmail(employee.getEmail()) != null) {
            return "Email already exists";
        }

        if (employee.getMobile() != null
                && !employee.getMobile().trim().isEmpty()
                && repository.findByMobile(employee.getMobile()) != null) {
            return "Mobile number already exists";
        }

        repository.save(employee);

        return "Employee Registered Successfully";
    }

    @PostMapping("/register-role-based")
public String registerRoleBasedEmployee(@RequestBody EmployeeRequest request) {

    if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
        return "Username is required";
    }

    if (repository.findByUsername(request.getUsername().trim()) != null) {
        return "Username already exists";
    }

    if (request.getEmail() != null
            && !request.getEmail().trim().isEmpty()
            && repository.findByEmail(request.getEmail()) != null) {
        return "Email already exists";
    }

    if (request.getMobile() != null
            && !request.getMobile().trim().isEmpty()
            && repository.findByMobile(request.getMobile()) != null) {
        return "Mobile number already exists";
    }

    Employee employee = new Employee();

    employee.setName(request.getName());
    employee.setEmail(request.getEmail());
    employee.setUsername(request.getUsername().trim());
    employee.setPassword(request.getPassword());
    employee.setMobile(request.getMobile());
    employee.setRole(request.getRole());
    employee.setJoiningDate(request.getJoiningDate());
    employee.setStatus(request.getStatus());

    if ("MR".equalsIgnoreCase(request.getRole())) {
        employee.setHeadquarters(request.getHeadquarters());
    } else {
        employee.setHeadquarters("");
    }

    Employee savedEmployee = repository.save(employee);

    if ("MR".equalsIgnoreCase(request.getRole())) {

        Headquarter hq = headquarterRepository
                .findByHeadquarterNameIgnoreCase(request.getHeadquarters())
                .orElseThrow(() -> new RuntimeException("Headquarter not found"));

        EmployeeHQMapping mapping = new EmployeeHQMapping();
        mapping.setEmployeeId(savedEmployee.getId());
        mapping.setHqId(hq.getId());

        mappingRepository.save(mapping);
    }

    if ("ASM".equalsIgnoreCase(request.getRole())) {

        if (request.getAssignedHeadquarters() == null
                || request.getAssignedHeadquarters().isEmpty()) {
            throw new RuntimeException("Please assign at least one HQ to ASM");
        }

        for (String hqName : request.getAssignedHeadquarters()) {

            Headquarter hq = headquarterRepository
                    .findByHeadquarterNameIgnoreCase(hqName)
                    .orElseThrow(() -> new RuntimeException("Headquarter not found: " + hqName));

            EmployeeHQMapping mapping = new EmployeeHQMapping();
            mapping.setEmployeeId(savedEmployee.getId());
            mapping.setHqId(hq.getId());

            mappingRepository.save(mapping);
        }
    }

    return "Employee Registered Successfully";
}
   @GetMapping("/admin/employees")
@ResponseBody
public List<Employee> getAllEmployees() {
    return repository.findAllByOrderByNameAsc();
}

    @GetMapping("/admin/employee/{id}")
    @ResponseBody
    public Employee getEmployee(@PathVariable Long id) {

        return repository
                .findById(id)
                .orElse(null);
    }

  @PutMapping("/employee/assign-headquarter")
public Employee assignHeadquarter(@RequestBody Map<String, Object> request) {

    Long employeeId = Long.valueOf(request.get("employeeId").toString());
    String headquarterName = request.get("headquarterName").toString();

    Employee employee = repository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // Employee table update
    employee.setHeadquarters(headquarterName);
    repository.save(employee);

    // System.out.println("Employee ID = " + employeeId);
    // System.out.println("Headquarter Name = " + headquarterName);
    // System.out.println("Searching HQ : " + headquarterName);

      Headquarter headquarter = headquarterRepository
        .findByHeadquarterNameIgnoreCase(headquarterName)
        .orElseThrow(() -> new RuntimeException("Headquarter not found"));

System.out.println("HQ ID = " + headquarter.getId());

if (!mappingRepository.existsByEmployeeIdAndHqId(employeeId, headquarter.getId())) {

    System.out.println("Saving mapping...");

    EmployeeHQMapping mapping = new EmployeeHQMapping();
    mapping.setEmployeeId(employeeId);
    mapping.setHqId(headquarter.getId());

    mappingRepository.save(mapping);

    System.out.println("Mapping Saved");
}

    return employee;
}

    @PutMapping("/employee/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request) {

        Employee employee = repository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String newPassword = request.getNewPassword();

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (!newPassword.matches(".*\\d.*")) {
            throw new RuntimeException("Password must contain at least 1 number");
        }

        if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new RuntimeException("Password must contain at least 1 special character");
        }

        if (employee.getPassword().equals(newPassword)) {
            throw new RuntimeException("New password cannot be same as current password");
        }

        employee.setPassword(newPassword);
        repository.save(employee);

        return "Password changed successfully";
    }

    @GetMapping("/employee/{id}/doctor-count")
    public Long getDoctorCount(@PathVariable Long id) {

        return doctorVisitRepository.countByEmployeeId(id);

    }

@GetMapping("/employee/session/check/{employeeId}")
public ResponseEntity<?> checkEmployeeSession(
        @PathVariable Long employeeId,
        @RequestParam String token) {

    Employee employee = repository.findById(employeeId)
            .orElse(null);

    if (employee == null) {
        return ResponseEntity
                .badRequest()
                .body("Employee Not Found");
    }

    if (employee.getSessionToken() == null ||
            !employee.getSessionToken().equals(token)) {

        return ResponseEntity
                .status(401)
                .body("Session Expired");
    }

    return ResponseEntity.ok("VALID");
}

@DeleteMapping("/employee/{id}")
public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {

    if (!repository.existsById(id)) {
        return ResponseEntity.badRequest().body("Employee not found");
    }

    repository.deleteById(id);

    return ResponseEntity.ok("Employee deleted successfully.");
}


}
