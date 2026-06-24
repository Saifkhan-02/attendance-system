package com.gps.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.HQList;

public interface HQListRepository
        extends JpaRepository<HQList, Long> {

    Optional<HQList> findByHqName(String hqName);

    List<HQList> findAllByOrderByIdDesc();

}
