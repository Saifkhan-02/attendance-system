package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.DoctorPayment;

public interface DoctorPaymentRepository
        extends JpaRepository<DoctorPayment, Long> {

    List<DoctorPayment> findByDoctorId(Long doctorId);

    List<DoctorPayment> findByPaymentDateBetween(
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate
    );
}