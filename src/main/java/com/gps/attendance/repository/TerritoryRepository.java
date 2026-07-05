package com.gps.attendance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gps.attendance.entity.Territory;

public interface TerritoryRepository extends JpaRepository<Territory, Long> {

    List<Territory> findByStatus(String status);

    Territory findByTerritoryNameIgnoreCase(String territoryName);
}