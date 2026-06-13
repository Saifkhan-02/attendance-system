package com.gps.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.dto.SalesPaymentSummaryDTO;
import com.gps.attendance.service.SalesPaymentService;

@CrossOrigin("*")
@RestController
public class SalesPaymentController {

    @Autowired
    private SalesPaymentService salesPaymentService;

   @GetMapping("/sales-payment/summary")
public SalesPaymentSummaryDTO getSummary(
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) Long doctorId
) {
    return salesPaymentService.getSummary(employeeId, doctorId);
}
}