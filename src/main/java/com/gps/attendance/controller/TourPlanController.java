package com.gps.attendance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.TourPlan;
import com.gps.attendance.repository.TourPlanRepository;

@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE
        })
@RestController
public class TourPlanController {

    @Autowired
    private TourPlanRepository repository;

    @PostMapping("/tour-plan/save")
    public TourPlan saveTourPlan(
            @RequestBody TourPlan tourPlan) {

        tourPlan.setStatus("PENDING");

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

    plan.setStatus("APPROVED");

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

    plan.setStatus("REJECTED");

    repository.save(plan);

    return "Tour Plan Rejected";
}
@PutMapping("/tour-plan/update-expense/{id}")
public String updateExpense(

        @PathVariable Long id,

        @RequestBody TourPlan request) {

    TourPlan plan =
            repository.findById(id)
            .orElse(null);

    if(plan == null){
        return "Tour Plan Not Found";
    }

    plan.setDaAmount(
            request.getDaAmount());

    plan.setOtherAmount(
            request.getOtherAmount());

    plan.setRemarks(
            request.getRemarks());

    repository.save(plan);

    return "Expense Updated";
}
@GetMapping("/admin/dashboard-stats")
public Map<String, Object> getDashboardStats() {

    Map<String, Object> result = new HashMap<>();

    long pending =
            repository.countByStatus("PENDING");

    long approved =
            repository.countByStatus("APPROVED");

    long rejected =
            repository.countByStatus("REJECTED");

    List<TourPlan> allTours =
            repository.findAll();

    double totalExpense = 0;

    for (TourPlan plan : allTours) {

        double da =
                plan.getDaAmount() == null
                        ? 0
                        : plan.getDaAmount();

        double other =
                plan.getOtherAmount() == null
                        ? 0
                        : plan.getOtherAmount();

        totalExpense += (da + other);
    }

    result.put("pending", pending);
    result.put("approved", approved);
    result.put("rejected", rejected);
    result.put("totalExpense", totalExpense);

    return result;
}

}