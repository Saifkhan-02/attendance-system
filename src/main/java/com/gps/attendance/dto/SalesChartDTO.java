package com.gps.attendance.dto;

public class SalesChartDTO {

    private final String label;
    private final Double saleAmount;
    private final Double paidAmount;
    private final Double dueAmount;
 
    public SalesChartDTO(String label, Double saleAmount, Double paidAmount, Double dueAmount) {
        this.label = label;
        this.saleAmount = saleAmount;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
    }


    public String getLabel() {
        return label;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Double getDueAmount() {
        return dueAmount;
    }
}