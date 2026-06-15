package com.gps.attendance.dto;

public class DoctorPaymentDetailsDTO {

    private final String doctorName;
    private final Double totalSale;
    private final Double paymentReceived;
    private final Double paymentDue;
    private final String status;

    public DoctorPaymentDetailsDTO(String doctorName, Double totalSale, Double paymentReceived, Double paymentDue, String status) {
        this.doctorName = doctorName;
        this.totalSale = totalSale;
        this.paymentReceived = paymentReceived;
        this.paymentDue = paymentDue;
        this.status = status;
    }

    public String getDoctorName() { return doctorName; }
    public Double getTotalSale() { return totalSale; }
    public Double getPaymentReceived() { return paymentReceived; }
    public Double getPaymentDue() { return paymentDue; }
    public String getStatus() { return status; }
}