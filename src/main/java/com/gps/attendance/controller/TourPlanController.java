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

import com.gps.attendance.entity.TourPlan;
import com.gps.attendance.repository.TourPlanRepository;

@RestController
@CrossOrigin("*")
public class TourPlanController {

    @Autowired
    private TourPlanRepository repository;

    @PostMapping("/tour-plan/save")
    public TourPlan saveTourPlan(
            @RequestBody TourPlan tourPlan) {

        tourPlan.setStatus("Planned");

        return repository.save(tourPlan);
    }

    @GetMapping("/tour-plan/history/{employeeId}")
    public List<TourPlan> getTourHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }

    // Admin endpoint to view all tour plans
    @GetMapping("/admin/tour-plans")
    public List<TourPlan> getAllTourPlans() {

        return repository.findAll();
    }

    @PutMapping("/tour-plan/approve/{id}")
public String approveTourPlan(
        @PathVariable Long id){

    TourPlan plan =
            repository.findById(id)
            .orElse(null);

    if(plan == null){
        return "Tour Plan Not Found";
    }

    plan.setStatus("Approved");

    repository.save(plan);

    return "Tour Plan Approved";
}

@PutMapping("/tour-plan/reject/{id}")
public String rejectTourPlan(
        @PathVariable Long id){

    TourPlan plan =
            repository.findById(id)
            .orElse(null);

    if(plan == null){
        return "Tour Plan Not Found";
    }

    plan.setStatus("Rejected");

    repository.save(plan);

    return "Tour Plan Rejected";
}

}