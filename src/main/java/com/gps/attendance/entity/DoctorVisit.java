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

    private String dob;
    private String anniversaryDate;
    private String hospitalName;
    private String visitDate;

    private LocalTime visitTime;

    private String location;

    private String landmark;
    private String latitude;
    private String longitude;
    private String accuracy;

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

    public String getworkingWith(){
        return workingWith;
    }
    public void setworkingWith(String workingWith) {
        this.workingWith = workingWith;
    }

    public String getWorkingPersonName() {
        return workingPersonName;
    }
    public void setWorkingPersonName(String workingPersonName) {
        this.workingPersonName = workingPersonName;
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

    public String getDOB() {
        return dob;
    }
    public void setDOB(String dob) {
        this.dob = dob;
    }

       public String getAnniversaryDate() {
        return anniversaryDate;
    }
    public void setAnniversaryDate(String anniversaryDate) {
        this.anniversaryDate = anniversaryDate;
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


    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getLandmark(){
        return landmark;
    }
    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
      public String getLatitude(){
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

      public String getLongitude(){
        return  longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude =  longitude;
    }

      public String getAccuracy(){
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
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