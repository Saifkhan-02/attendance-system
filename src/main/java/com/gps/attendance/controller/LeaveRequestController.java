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

        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }
    // Admin Endpoints

    @GetMapping("/admin/leaves")
    public List<LeaveRequest> getAllLeaves() {
        return repository.findAllByOrderByIdDesc();
    }

    @PutMapping("/leave/approve/{id}")
    public String approveLeave(@PathVariable Long id) {

        LeaveRequest leave = repository.findById(id).orElse(null);

        if (leave == null) {
            return "Leave Request Not Found";
        }

        leave.setStatus("Approved");
        repository.save(leave);

        return "Leave Approved";
    }

    @PutMapping("/leave/reject/{id}")
    public String rejectLeave(@PathVariable Long id) {

        LeaveRequest leave = repository.findById(id).orElse(null);

        if (leave == null) {
            return "Leave Request Not Found";
        }

        leave.setStatus("Rejected");
        repository.save(leave);

        return "Leave Rejected";
    }
}
