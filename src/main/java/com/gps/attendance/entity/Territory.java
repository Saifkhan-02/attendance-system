package com.gps.attendance.entity;

import jakarta.persistence.*;

@Entity
public class Territory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String territoryName;

    private String stateName;

    private String status = "Active";

    public Long getId() {
        return id;
    }

    public String getTerritoryName() {
        return territoryName;
    }

    public void setTerritoryName(String territoryName) {
        this.territoryName = territoryName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}