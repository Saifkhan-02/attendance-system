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
public TourPlan saveTourPlan(@RequestBody TourPlan tourPlan) {

    if (repository.existsByEmployeeIdAndTravelDate(
            tourPlan.getEmployeeId(),
            tourPlan.getTravelDate()
    )) {
        throw new RuntimeException("Tour already submitted for this date");
    }

    if (tourPlan.getStatus() == null || tourPlan.getStatus().isBlank()) {
        tourPlan.setStatus("PENDING");
    }

    calculateTotals(tourPlan);

    return repository.save(tourPlan);
}

    @GetMapping("/tour-plan/history/{employeeId}")
    public List<TourPlan> getTourHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    // Admin endpoint to view all tour plans
    
   @GetMapping("/admin/tour-plans")
public List<TourPlan> getAllTourPlans() {

    return repository.findAllByOrderByIdDesc();
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

    calculateTotals(plan);

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

@PostMapping("/tour-plan/monthly/save")
public List<TourPlan> saveMonthlyTourPlan(@RequestBody List<TourPlan> plans) {

    if (plans == null || plans.isEmpty()) {
        throw new RuntimeException("Tour plan is empty");
    }

    Long employeeId = plans.get(0).getEmployeeId();
    String month = plans.get(0).getMonth();

    boolean alreadyExists =
            repository.existsByEmployeeIdAndMonth(employeeId, month);

    if (alreadyExists) {
        throw new RuntimeException("Tour plan for this month already submitted.");
    }

    for (TourPlan plan : plans) {
       if (plan.getStatus() == null || plan.getStatus().isBlank()) {
            plan.setStatus("PENDING");
        }       

        calculateTotals(plan);   
    }
    

    return repository.saveAll(plans);
}

@GetMapping("/tour-plan/monthly/history/{employeeId}/{month}")
public List<TourPlan> getMonthlyTourPlan(
        @PathVariable Long employeeId,
        @PathVariable String month
) {
   return repository.findByEmployeeIdAndMonthOrderByIdDesc(
        employeeId,
        month);
}

private void calculateTotals(TourPlan plan) {

    double fare = plan.getFareAmount() == null ? 0 : plan.getFareAmount();
    double da = plan.getDaAmount() == null ? 0 : plan.getDaAmount();
    double other = plan.getOtherAmount() == null ? 0 : plan.getOtherAmount();

    plan.setTotalExpense(fare + da + other);
}
@GetMapping("/tour-plan/summary/{employeeId}/{month}")
public Map<String, Object> getEmployeeTourSummary(
        @PathVariable Long employeeId,
        @PathVariable String month) {

    List<TourPlan> plans =
            repository.findByEmployeeIdAndMonthOrderByIdDesc(employeeId, month);

    double totalKm = 0;
    double totalFare = 0;
    double totalDa = 0;
    double totalOther = 0;
    double totalExpense = 0;

    for (TourPlan plan : plans) {

        totalKm += plan.getTravelKm() == null ? 0 : plan.getTravelKm();
        totalFare += plan.getFareAmount() == null ? 0 : plan.getFareAmount();
        totalDa += plan.getDaAmount() == null ? 0 : plan.getDaAmount();
        totalOther += plan.getOtherAmount() == null ? 0 : plan.getOtherAmount();

        if (plan.getTotalExpense() != null) {
            totalExpense += plan.getTotalExpense();
        } else {
            totalExpense +=
                    (plan.getFareAmount() == null ? 0 : plan.getFareAmount())
                    + (plan.getDaAmount() == null ? 0 : plan.getDaAmount())
                    + (plan.getOtherAmount() == null ? 0 : plan.getOtherAmount());
        }
    }

    Map<String, Object> result = new HashMap<>();

    result.put("totalKm", totalKm);
    result.put("totalFare", totalFare);
    result.put("totalDa", totalDa);
    result.put("totalOther", totalOther);
    result.put("totalExpense", totalExpense);

    return result;
}

@GetMapping("/tour-plan/expense-chart")
public List<Object[]> getExpenseChart() {

    List<Object[]> data = repository.getLast7DaysExpenseChart();

    java.util.Collections.reverse(data);

    return data;
}

}