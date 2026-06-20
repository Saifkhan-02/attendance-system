package com.gps.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tour_plan")
public class TourPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    @Column(name = "tour_type")
    private String tourType;

    @Column(name = "from_hq")
    private String fromHq;

    @Column(name = "to_hq")
    private String toHq;

    @Column(name = "headquarter")
private String headquarter;

@Column(name = "month")
private String month;

@Column(name = "route_name")
private String routeName;

@Column(name = "week_day")
private String weekDay;

    @Column(name = "travel_km")
    private Double travelKm;

    @Column(name = "fare_amount")
    private Double fareAmount;

    @Column(name = "out_station")
    private String outStation;

    @Column(name = "da_amount")
    private Double daAmount;

    @Column(name = "other_amount")
    private Double otherAmount;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "status")
    private String status;

    @Column(name = "admin_remark")
    private String adminRemark;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "edited_by")
    private String editedBy;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TourPlan() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getTourType() {
        return tourType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public String getFromHq() {
        return fromHq;
    }

    public void setFromHq(String fromHq) {
        this.fromHq = fromHq;
    }

    public String getToHq() {
        return toHq;
    }

    public void setToHq(String toHq) {
        this.toHq = toHq;
    }

    public String getHeadquarter() {
    return headquarter;
}

public void setHeadquarter(String headquarter) {
    this.headquarter = headquarter;
}

public String getMonth() {
    return month;
}

public void setMonth(String month) {
    this.month = month;
}

public String getRouteName() {
    return routeName;
}

public void setRouteName(String routeName) {
    this.routeName = routeName;
}

public String getWeekDay() {
    return weekDay;
}

public void setWeekDay(String weekDay) {
    this.weekDay = weekDay;
}

    public Double getTravelKm() {
        return travelKm;
    }

    public void setTravelKm(Double travelKm) {
        this.travelKm = travelKm;
    }

    public Double getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(Double fareAmount) {
        this.fareAmount = fareAmount;
    }

    public String getOutStation() {
        return outStation;
    }

    public void setOutStation(String outStation) {
        this.outStation = outStation;
    }

    public Double getDaAmount() {
        return daAmount;
    }

    public void setDaAmount(Double daAmount) {
        this.daAmount = daAmount;
    }

    public Double getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(Double otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(String editedBy) {
        this.editedBy = editedBy;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}