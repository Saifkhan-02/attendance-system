package com.gps.attendance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


    @Entity @Table(name="asm_hq_mapping")
public class ASMHQMapping{
@Id 
@GeneratedValue(strategy=GenerationType.IDENTITY)
private Long id;
private Long asmId;
private Long hqId;
private LocalDateTime assignedDate=LocalDateTime.now();
private String status="Active";
public Long getId(){return id;} public void setId(Long id){this.id=id;}
public Long getAsmId(){return asmId;} public void setAsmId(Long asmId){this.asmId=asmId;}
public Long getHqId(){return hqId;} public void setHqId(Long hqId){this.hqId=hqId;}
public LocalDateTime getAssignedDate(){return assignedDate;} public void setAssignedDate(LocalDateTime assignedDate){this.assignedDate=assignedDate;}
public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
    

