package com.gps.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Attendance;
import com.gps.attendance.repository.AttendanceRepository;

@RestController
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceRepository repository;

    @PostMapping("/mark-attendance")
    public ResponseEntity<?> markAttendance(
            @RequestBody Attendance attendance) {

        if (attendance.getEmployeeId() == null) {
            return ResponseEntity.badRequest()
                    .body("Employee ID is required");
        }

        LocalDate today = LocalDate.now();

        if (repository.existsByEmployeeIdAndAttendanceDate(
                attendance.getEmployeeId(),
                today)) {

            return ResponseEntity
                    .badRequest()
                    .body("Attendance already marked for today");
        }

        attendance.setAttendanceDate(today);
        attendance.setAttendanceTime(LocalTime.now());

        repository.save(attendance);

        return ResponseEntity.ok("Attendance Marked Successfully");
    }

    @GetMapping("/attendance/history/{employeeId}")
    public List<Attendance> getAttendanceHistory(
            @PathVariable Long employeeId) {

        System.out.println("History API Hit : " + employeeId);
        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    @GetMapping("/attendance-list")
    public List<Attendance> getAllAttendance() {
        return repository.findAllByOrderByIdDesc();
    }
}
