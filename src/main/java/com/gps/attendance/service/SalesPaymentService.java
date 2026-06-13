package com.gps.attendance.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gps.attendance.dto.SalesPaymentSummaryDTO;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.repository.ProductOrderRepository;

@Service
public class SalesPaymentService {

    @Autowired
    private ProductOrderRepository orderRepository;

   public SalesPaymentSummaryDTO getSummary(Long employeeId, Long doctorId) {

    SalesPaymentSummaryDTO dto = new SalesPaymentSummaryDTO();

    String today = LocalDate.now().toString();
    String currentMonth = LocalDate.now().toString().substring(0, 7);

    List<ProductOrder> allOrders = orderRepository.findByEmployeeId(employeeId);

    if (doctorId != null) {
        allOrders = allOrders.stream()
                .filter(o -> doctorId.equals(o.getDoctorId()))
                .toList();
    }

    double todaySale = allOrders.stream()
            .filter(o -> today.equals(o.getOrderDate()))
            .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
            .sum();

    double monthlySale = allOrders.stream()
            .filter(o -> o.getOrderDate() != null &&
                    o.getOrderDate().startsWith(currentMonth))
            .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
            .sum();

    double received = allOrders.stream()
            .mapToDouble(o -> o.getPaidAmount() == null ? 0 : o.getPaidAmount())
            .sum();

    double due = allOrders.stream()
            .mapToDouble(o -> o.getDueAmount() == null ? 0 : o.getDueAmount())
            .sum();

    dto.setTodaySale(todaySale);
    dto.setMonthlySale(monthlySale);
    dto.setPaymentReceived(received);
    dto.setPaymentDue(due);

    return dto;
}
}