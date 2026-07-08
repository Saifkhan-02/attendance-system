package com.gps.attendance.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.DoctorVisitRepository;
import com.gps.attendance.repository.ProductOrderRepository;

@RestController
@RequestMapping("/summary")
@CrossOrigin("*")
public class SummaryController {

    private final AttendanceRepository attendanceRepository;
    private final DoctorVisitRepository doctorVisitRepository;
    private final ProductOrderRepository productOrderRepository;

    public SummaryController(
            AttendanceRepository attendanceRepository,
            DoctorVisitRepository doctorVisitRepository,
            ProductOrderRepository productOrderRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.doctorVisitRepository = doctorVisitRepository;
        this.productOrderRepository = productOrderRepository;
    }

    @GetMapping("/monthly/{employeeId}")
    public Map<String, Long> getMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam String month
    ) {
        Map<String, Long> data = new HashMap<>();

    
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
       long attendanceCount =
        attendanceRepository.countByEmployeeIdAndAttendanceDateBetween(
                employeeId,
                startDate,
                endDate
        );

data.put("attendance", attendanceCount);
        data.put("doctorVisits",
                doctorVisitRepository.countByEmployeeIdAndVisitDateStartingWith(employeeId, month));

        data.put("doctorList",
                doctorVisitRepository.countDistinctDoctorByEmployeeAndMonth(employeeId, month));

        data.put("orders",
                productOrderRepository.countByEmployeeIdAndOrderDateStartingWith(employeeId, month));

        return data;
    }

    @GetMapping("/incentive/{employeeId}")
public Map<String, Object> getIncentiveSummary(
        @PathVariable Long employeeId,
        @RequestParam String month
) {
    Double monthlySales = productOrderRepository
            .getMonthlySalesByEmployee(employeeId, month);

            if (monthlySales == null) {
                    monthlySales = 0.0;
            }

    int incentive = calculateIncentive(monthlySales);
    int nextMilestone = getNextMilestone(monthlySales);

    double remaining = Math.max(nextMilestone - monthlySales, 0);

    Map<String, Object> data = new HashMap<>();
    data.put("monthlySales", monthlySales);
    data.put("incentive", incentive);
    data.put("nextMilestone", nextMilestone);
    data.put("remaining", remaining);

    return data;
}

private int calculateIncentive(Double sales) {
    if (sales == null) sales = 0.0;

    if (sales >= 450000) return 40000;
    if (sales >= 400000) return 35000;
    if (sales >= 300000) return 20000;
    if (sales >= 250000) return 15000;
    if (sales >= 200000) return 10000;
    if (sales >= 150000) return 5000;

    return 0;
}

private int getNextMilestone(Double sales) {
    if (sales == null) sales = 0.0;

    if (sales < 150000) return 150000;
    if (sales < 200000) return 200000;
    if (sales < 250000) return 250000;
    if (sales < 300000) return 300000;
    if (sales < 400000) return 400000;
    if (sales < 450000) return 450000;

    return 450000;
}

}