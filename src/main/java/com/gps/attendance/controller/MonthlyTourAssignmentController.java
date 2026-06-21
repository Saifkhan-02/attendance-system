package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.MonthlyTourAssignment;
import com.gps.attendance.repository.MonthlyTourAssignmentRepository;

@RestController
@CrossOrigin("*")
public class MonthlyTourAssignmentController {

    @Autowired
    private MonthlyTourAssignmentRepository repository;

@PostMapping("/admin/tour-assignment/save")
public List<MonthlyTourAssignment> saveAssignments(
        @RequestBody List<MonthlyTourAssignment> assignments) {

    if (assignments == null || assignments.isEmpty()) {
        throw new RuntimeException("No assignment found");
    }

    Long employeeId = assignments.get(0).getEmployeeId();
    String month = assignments.get(0).getMonth();

    if (repository.existsByEmployeeIdAndMonth(employeeId, month)) {
        throw new RuntimeException("Tour plan already assigned for this employee and month");
    }

    return repository.saveAll(assignments);
}

@PutMapping("/admin/tour-assignment/update")
public List<MonthlyTourAssignment> updateAssignments(
        @RequestBody List<MonthlyTourAssignment> assignments) {

    if (assignments == null || assignments.isEmpty()) {
        throw new RuntimeException("No assignment found");
    }

    Long employeeId = assignments.get(0).getEmployeeId();
    String month = assignments.get(0).getMonth();

    repository.deleteByEmployeeIdAndMonth(employeeId, month);

    return repository.saveAll(assignments);
}

    @GetMapping("/tour-assignment/{employeeId}/{month}")
    public List<MonthlyTourAssignment> getAssignments(
            @PathVariable Long employeeId,
            @PathVariable String month) {

        return repository.findByEmployeeIdAndMonth(
                employeeId,
                month);
    }
}