package com.gps.attendance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_master")
public class RouteMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String headquarterName;

    private String routeName;

    private Double distanceKm = 0.0;

    private String routeType = "Field Route"; // Field Route / Activity

    private String status = "Active";

    public Long getId() {
        return id;
    }

    public String getHeadquarterName() {
        return headquarterName;
    }

    public String getRouteName() {
        return routeName;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public String getRouteType() {
        return routeType;
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

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}