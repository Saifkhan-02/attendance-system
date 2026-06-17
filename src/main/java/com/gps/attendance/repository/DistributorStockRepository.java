package com.gps.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.DistributorStock;

public interface DistributorStockRepository extends JpaRepository<DistributorStock, Long> {

    List<DistributorStock> findByDistributorId(Long distributorId);

    Optional<DistributorStock> findByDistributorIdAndProductId(
            Long distributorId,
            Long productId
    );
}