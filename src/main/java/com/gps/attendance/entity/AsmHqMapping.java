package com.gps.attendance.entity;

import jakarta.persistence.*;

@Entity
public class AsmHqMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long asmId;

    private String asmName;

    private String headquarterName;

    public Long getId() {
        return id;
    }

    public Long getAsmId() {
        return asmId;
    }

    public void setAsmId(Long asmId) {
        this.asmId = asmId;
    }

    public String getAsmName() {
        return asmName;
    }

    public void setAsmName(String asmName) {
        this.asmName = asmName;
    }

    public String getHeadquarterName() {
        return headquarterName;
    }

    public void setHeadquarterName(String headquarterName) {
        this.headquarterName = headquarterName;
    }
}