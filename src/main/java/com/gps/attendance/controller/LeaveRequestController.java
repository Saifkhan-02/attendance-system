package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gps.attendance.entity.LeaveRequest;
import com.gps.attendance.repository.LeaveRequestRepository;

@RestController
@CrossOrigin("*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestRepository repository;

    @PostMapping("/leave/apply")
    public LeaveRequest applyLeave(
            @RequestBody LeaveRequest leave) {

        leave.setStatus("Pending");

        return repository.save(leave);
    }

    @GetMapping("/leave/history/{employeeId}")
    public List<LeaveRequest> getHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }
}