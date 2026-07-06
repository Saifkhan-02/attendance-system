package com.gps.attendance.dto;

import java.util.List;

public class EmployeeRequest {

    private Long id;

    private String name;
    private String username;
    private String password;
    private String email;
    private String mobile;
    private String designation;

    private String role; // MR / ASM

    private String headquarters; // MR ke liye single HQ

    private List<String> assignedHeadquarters; // ASM ke liye multiple HQ

    private String joiningDate;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public List<String> getAssignedHeadquarters() {
        return assignedHeadquarters;
    }

    public void setAssignedHeadquarters(List<String> assignedHeadquarters) {
        this.assignedHeadquarters = assignedHeadquarters;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}