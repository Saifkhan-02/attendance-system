package com.gps.attendance.repository;

import com.gps.attendance.entity.GlobalStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GlobalStockRepository extends JpaRepository<GlobalStock, Long> {

    List<GlobalStock> findByStatus(String status);
}