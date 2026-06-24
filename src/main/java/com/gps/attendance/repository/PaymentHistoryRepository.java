package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.PaymentHistory;

public interface PaymentHistoryRepository
        extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findAllByOrderByPaymentDateDesc();
}