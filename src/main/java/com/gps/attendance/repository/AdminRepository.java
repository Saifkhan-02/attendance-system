package com.gps.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gps.attendance.entity.Admin;

public interface AdminRepository
        extends JpaRepository<Admin, Long> {

    Admin findByUsernameAndPassword(
            String username,
            String password);

    Admin findByUsername(String username);
}