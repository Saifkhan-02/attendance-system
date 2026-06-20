package com.gps.attendance.dto;

public class ChangePasswordRequest {

    private Long employeeId;
    private String currentPassword;
    private String newPassword;

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}