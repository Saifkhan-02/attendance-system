package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    List<ProductOrder> findByEmployeeId(Long employeeId);

    long countByEmployeeIdAndOrderDateStartingWith(Long employeeId, String month);

     List<ProductOrder> findByDoctorId(Long doctorId);

    // Date wise
    List<ProductOrder> findByOrderDate(String orderDate);

    // Month wise
    List<ProductOrder> findByOrderDateStartingWith(String month);

    List<ProductOrder>
            findByEmployeeNameContainingIgnoreCaseAndDoctorNameContainingIgnoreCase(
                    String employeeName,
                    String doctorName
            );
}
