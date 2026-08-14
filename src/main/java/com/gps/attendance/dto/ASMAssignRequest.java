package com.gps.attendance.dto;

import java.util.List;

public class ASMAssignRequest {
    private Long asmId;
    private List<Long> hqIds;

    public Long getAsmId() { return asmId; }
    public void setAsmId(Long asmId) { this.asmId = asmId; }

    public List<Long> getHqIds() { return hqIds; }
    public void setHqIds(List<Long> hqIds) { this.hqIds = hqIds; }
}