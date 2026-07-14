package com.gps.attendance.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Employee;
import com.gps.attendance.entity.EmployeeHQMapping;
import com.gps.attendance.entity.Headquarter;
import com.gps.attendance.repository.EmployeeHQMappingRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.HeadquarterRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/hq")
public class HQController {

    @Autowired
    private EmployeeHQMappingRepository mappingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @GetMapping("/employee/{employeeId}")
    public List<Headquarter> getEmployeeHQs(
            @PathVariable Long employeeId) {

        List<EmployeeHQMapping> mappings =
                mappingRepository.findByEmployeeId(employeeId);

        List<Headquarter> hqList = new ArrayList<>();

        for (EmployeeHQMapping mapping : mappings) {
            Headquarter hq = headquarterRepository
                    .findById(mapping.getHqId())
                    .orElse(null);

            if (hq != null) {
                hqList.add(hq);
            }
        }

        return hqList;
    }

    @PostMapping("/mapping/save")
    public EmployeeHQMapping saveMapping(
            @RequestBody EmployeeHQMapping mapping) {

        if (mappingRepository.existsByEmployeeIdAndHqId(
                mapping.getEmployeeId(),
                mapping.getHqId())) {

            throw new RuntimeException(
                    "HQ already assigned to employee");
        }

        return mappingRepository.save(mapping);
    }

    @GetMapping("/mapping/all")
    public List<Map<String, Object>> getAllMappings() {

        List<EmployeeHQMapping> mappings =
                mappingRepository.findAll();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (EmployeeHQMapping mapping : mappings) {

            Employee employee = employeeRepository
                    .findById(mapping.getEmployeeId())
                    .orElse(null);

            Headquarter headquarter = headquarterRepository
                    .findById(mapping.getHqId())
                    .orElse(null);

            Map<String, Object> row = new HashMap<>();

            row.put("id", mapping.getId());

            row.put(
                    "employeeName",
                    employee != null ? employee.getName() : ""
            );

            row.put(
                    "hqName",
                    headquarter != null
                            ? headquarter.getHeadquarterName()
                            : ""
            );

            result.add(row);
        }

        return result;
    }

    @DeleteMapping("/mapping/delete/{id}")
    public String deleteMapping(
            @PathVariable Long id) {

        mappingRepository.deleteById(id);

        return "Mapping Deleted Successfully";
    }
}