package com.gps.attendance.controller;

import com.gps.attendance.dto.ASMAssignRequest;
import com.gps.attendance.entity.ASMHQMapping;
import com.gps.attendance.repository.ASMHQMappingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/asm-hq")
@CrossOrigin("*")
public class ASMAdminController {

    private final ASMHQMappingRepository asmHQMappingRepository;

    public ASMAdminController(ASMHQMappingRepository asmHQMappingRepository) {
        this.asmHQMappingRepository = asmHQMappingRepository;
    }

    @PostMapping("/assign")
    @Transactional
    public ResponseEntity<?> assignHQsToASM(@RequestBody ASMAssignRequest request) {
        asmHQMappingRepository.deleteByAsmId(request.getAsmId());

        if (request.getHqIds() != null) {
            for (Long hqId : request.getHqIds()) {
                ASMHQMapping mapping = new ASMHQMapping();
                mapping.setAsmId(request.getAsmId());
                mapping.setHqId(hqId);
                mapping.setStatus("Active");
                asmHQMappingRepository.save(mapping);
            }
        }

        return ResponseEntity.ok().body("{\"message\": \"HQs assigned successfully\"}");
    }

    @GetMapping("/{asmId}")
    public List<ASMHQMapping> getAssignedHQs(@PathVariable Long asmId) {
        return asmHQMappingRepository.findByAsmIdAndStatus(asmId, "Active");
    }
}
