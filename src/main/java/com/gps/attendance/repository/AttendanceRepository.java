package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}