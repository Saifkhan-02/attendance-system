package com.gps.attendance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "distributors")
public class Distributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String distributorName;
    private String headquarters;
    private String address;
    private String status = "Active";

    public Long getId() {
        return id;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}