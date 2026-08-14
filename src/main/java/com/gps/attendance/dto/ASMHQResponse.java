package com.gps.attendance.dto;

public class ASMHQResponse{
private Long id;
private String headquarterName;
public ASMHQResponse(){}
public ASMHQResponse(Long id,String headquarterName){
    this.id=id;
    this.headquarterName=headquarterName;
}
public Long getId(){
    return id;
} 
public void setId(Long id){
    this.id=id;}
public String getHeadquarterName(){
    return headquarterName;} 
public void setHeadquarterName(String headquarterName){
    this.headquarterName=headquarterName;}
}
