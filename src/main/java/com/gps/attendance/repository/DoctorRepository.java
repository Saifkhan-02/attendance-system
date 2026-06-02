package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.Doctor;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

}