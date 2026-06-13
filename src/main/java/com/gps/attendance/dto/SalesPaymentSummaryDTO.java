package com.gps.attendance.dto;

public class SalesPaymentSummaryDTO {

    private Double todaySale;
    private Double monthlySale;
    private Double paymentReceived;
    private Double paymentDue;

    public Double getTodaySale() {
        return todaySale;
    }

    public void setTodaySale(Double todaySale) {
        this.todaySale = todaySale;
    }

    public Double getMonthlySale() {
        return monthlySale;
    }

    public void setMonthlySale(Double monthlySale) {
        this.monthlySale = monthlySale;
    }

    public Double getPaymentReceived() {
        return paymentReceived;
    }

    public void setPaymentReceived(Double paymentReceived) {
        this.paymentReceived = paymentReceived;
    }

    public Double getPaymentDue() {
        return paymentDue;
    }

    public void setPaymentDue(Double paymentDue) {
        this.paymentDue = paymentDue;
    }
}