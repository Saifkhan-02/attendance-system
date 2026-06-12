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
        
        attendanceRepository.countByEmployeeIdAndAttendanceDateBetween(
        employeeId,
        startDate,
        endDate
        );

        data.put("doctorVisits",
                doctorVisitRepository.countByEmployeeIdAndVisitDateStartingWith(employeeId, month));

        data.put("doctorList",
                doctorVisitRepository.countDistinctDoctorByEmployeeAndMonth(employeeId, month));

        data.put("orders",
                productOrderRepository.countByEmployeeIdAndOrderDateStartingWith(employeeId, month));

        return data;
    }
}