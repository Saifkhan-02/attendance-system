package com.gps.attendance.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.DoctorVisit;
import com.gps.attendance.repository.DoctorVisitRepository;

@RestController
@CrossOrigin("*")
public class DoctorVisitController {

    @Autowired
    private DoctorVisitRepository repository;

    @PostMapping("/doctor-visit/save")
    public DoctorVisit saveDoctorVisit(
            @RequestBody DoctorVisit visit) {

        visit.setVisitTime(LocalTime.now());
        visit.setStatus("Completed");

        return repository.save(visit);
    }

    @GetMapping("/doctor-visit/history/{employeeId}")
    public List<DoctorVisit> getDoctorVisitHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }
    
    

// @PostMapping("/doctor-visit/save")
// public DoctorVisit saveVisit(
//         @RequestBody DoctorVisit visit){

//     visit.setVisitTime(LocalTime.now());

//     visit.setStatus("Completed");

//     return repository.save(visit);
// }
}