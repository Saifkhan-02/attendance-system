package com.gps.attendance.entity;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DoctorVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private String employeeName;

    private String workingWith;
private String workingPersonName;



    private String doctorName;
    private String specialization;
    private String hospitalName;
    private String visitDate;

    private LocalTime visitTime;

    private String purpose;
    private String location;

    private String photo;
    private String doctorResponse;
    private String remarks;
    private String status;

    // getters and setters

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


    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization(){
        return specialization;
    }
    public void setSpecialization(String specialization){
        this.specialization = specialization;
    }

    public String getHospitalName() {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getVisitDate() {
        return visitDate;
    }
    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public LocalTime getVisitTime() {
        return visitTime;
    }
    public void setVisitTime(LocalTime visitTime) {
        this.visitTime = visitTime;
    }

    public String getPurpose() {
        return purpose;
    }
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getDoctorResponse() {
        return doctorResponse;
    }
    public void setDoctorResponse(String doctorResponse) {
        this.doctorResponse = doctorResponse;
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
}