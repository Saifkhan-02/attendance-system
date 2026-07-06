package com.gps.attendance.dto;

public class ASMDashboardSummaryDTO {
private Integer team;

private Long present;

private Long visits;

private Long orders;

private Double sales;

private Double collection;

private Double due;

private Double expense;

public ASMDashboardSummaryDTO() {
}

public Integer getTeam() {
    return team;
}

public void setTeam(Integer team) {
    this.team = team;    
}

public Long getPresent() {
    return present;
}

public void setPresent(Long present) {
    this.present = present;
}

public Long getVisits() {
    return visits;
}

public void setVisits(Long visits) {
    this.visits = visits;
}

public Long getOrders() {
    return orders;
}

public void setOrders(Long orders) {
    this.orders = orders;
}

public Double getSales() {
    return sales;
}

public void setSales(Double sales) {
    this.sales = sales;
}

public Double getCollection() {
    return collection;
}

public void setCollection(Double collection) {
    this.collection = collection;
}

public Double getDue() {
    return due;
}

public void setDue(Double due) {
    this.due = due;
}

public Double getExpense() {
    return expense;
}    

public void setExpense(Double expense) {
    this.expense = expense;
}

}
