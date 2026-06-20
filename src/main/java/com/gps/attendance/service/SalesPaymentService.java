package com.gps.attendance.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gps.attendance.dto.DoctorPaymentDetailsDTO;
import com.gps.attendance.dto.SalesChartDTO;
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
//   Charts and reports
  public List<SalesChartDTO> getDailyChart(Long employeeId, Long doctorId) {

    List<ProductOrder> orders = orderRepository.findByEmployeeId(employeeId);

    if (doctorId != null) {
        orders = orders.stream()
                .filter(o -> doctorId.equals(o.getDoctorId()))
                .toList();
    }

    return orders.stream()
            .filter(o -> o.getOrderDate() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                    ProductOrder::getOrderDate
            ))
            .entrySet()
            .stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .map(entry -> {

                double sale = entry.getValue().stream()
                        .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
                        .sum();

                double paid = entry.getValue().stream()
                        .mapToDouble(o -> o.getPaidAmount() == null ? 0 : o.getPaidAmount())
                        .sum();

                double due = entry.getValue().stream()
                        .mapToDouble(o -> o.getDueAmount() == null ? 0 : o.getDueAmount())
                        .sum();

                return new SalesChartDTO(
                        entry.getKey(),
                        sale,
                        paid,
                        due
                );
            })
            .toList();
}
//  Doctor table access data on summary page
public List<DoctorPaymentDetailsDTO> getDoctorPaymentDetails(Long employeeId) {

    List<ProductOrder> orders = orderRepository.findByEmployeeId(employeeId);

    return orders.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                    ProductOrder::getDoctorName
            ))
            .entrySet()
            .stream()
            .map(entry -> {

                double totalSale = entry.getValue().stream()
                        .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
                        .sum();

                double paid = entry.getValue().stream()
                        .mapToDouble(o -> o.getPaidAmount() == null ? 0 : o.getPaidAmount())
                        .sum();

                double due = entry.getValue().stream()
                        .mapToDouble(o -> o.getDueAmount() == null ? 0 : o.getDueAmount())
                        .sum();

                String status;

                if (due == 0) {
                    status = "Paid";
                } else if (paid == 0) {
                    status = "Due";
                } else {
                    status = "Partial";
                }

                return new DoctorPaymentDetailsDTO(
                        entry.getKey(),
                        totalSale,
                        paid,
                        due,
                        status
                );
            })
            .toList();
}

public double getAdminTotalSale() {

    List<ProductOrder> orders = orderRepository.findAll();

    return orders.stream()
            .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
            .sum();
}

public double getEmployeeSale(Long employeeId) {

    List<ProductOrder> orders = orderRepository.findByEmployeeId(employeeId);

    return orders.stream()
            .mapToDouble(o -> o.getOrderAmount() == null ? 0 : o.getOrderAmount())
            .sum();
}

}