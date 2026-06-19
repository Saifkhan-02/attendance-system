package com.gps.attendance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "distributor_stock")
public class DistributorStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long distributorId;
    private String distributorName;

    private Long productId;
    private String productName;

    private Integer availableUnits = 0;
 

    private String headquarters;

    private String status = "Active";

    public Long getId() {
        return id;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getAvailableUnits() {
        return availableUnits;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setAvailableUnits(Integer availableUnits) {
        this.availableUnits = availableUnits;
    }

   public void setHeadquarters(String headquarters) {
       this.headquarters = headquarters;
   }

    public void setStatus(String status) {
        this.status = status;
    }
}