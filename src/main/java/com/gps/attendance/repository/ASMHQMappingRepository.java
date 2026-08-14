package com.gps.attendance.repository;

import com.gps.attendance.entity.ASMHQMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ASMHQMappingRepository extends JpaRepository<ASMHQMapping, Long> {
    List<ASMHQMapping> findByAsmIdAndStatus(Long asmId, String status);
    boolean existsByAsmIdAndHqId(Long asmId, Long hqId);
    void deleteByAsmId(Long asmId);
}