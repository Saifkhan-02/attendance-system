package com.gps.attendance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "headquarters")
public class Headquarter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String headquarterName;
    private String status = "Active";

    public Long getId() {
        return id;
    }

    public String getHeadquarterName() {
        return headquarterName;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHeadquarterName(String headquarterName) {
        this.headquarterName = headquarterName;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}