package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.AsmHqMapping;

public interface AsmHqMappingRepository
        extends JpaRepository<AsmHqMapping, Long> {

    List<AsmHqMapping> findByAsmId(Long asmId);

    List<AsmHqMapping> findByHeadquarterName(String headquarterName);

    void deleteByAsmId(Long asmId);
}