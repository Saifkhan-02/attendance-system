package com.gps.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private String employeeName;
    private String expenseType;
    private String expenseDate;
    private Double amount;

    @Column(length = 1000)
    private String remarks;

    private String billUrl;
    private String status = "Pending";

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

    public String getExpenseType() { 
        return expenseType; 
    }
    public void setExpenseType(String expenseType) { 
        this.expenseType = expenseType; 
    }

    public String getExpenseDate() { 
        return expenseDate; 
    }
    public void setExpenseDate(String expenseDate) { 
        this.expenseDate = expenseDate; 
    }

    public Double getAmount() { 
        return amount; 
    }
    public void setAmount(Double amount) { 
        this.amount = amount; 
    }

    public String getRemarks() { 
        return remarks; 
    }
    public void setRemarks(String remarks) { 
        this.remarks = remarks; 
    }

    public String getBillUrl() { 
        return billUrl; 
    }
    public void setBillUrl(String billUrl) { 
        this.billUrl = billUrl; 
    }

    public String getStatus() { 
        return status; }
    public void setStatus(String status) { 
        this.status = status; 
    }
}