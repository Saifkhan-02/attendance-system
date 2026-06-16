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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Employee;
import com.gps.attendance.entity.EmployeeHQMapping;
import com.gps.attendance.entity.HQList;
import com.gps.attendance.repository.EmployeeHQMappingRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.HQListRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/hq")
public class HQController {

    @Autowired
    private HQListRepository hqRepository;

    @Autowired
    private EmployeeHQMappingRepository mappingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/employee/{employeeId}")
    public List<HQList> getEmployeeHQs(
            @PathVariable Long employeeId) {

        List<EmployeeHQMapping> mappings
                = mappingRepository.findByEmployeeId(employeeId);

        List<HQList> hqList = new ArrayList<>();

        for (EmployeeHQMapping mapping : mappings) {

            HQList hq
                    = hqRepository.findById(mapping.getHqId())
                            .orElse(null);

            if (hq != null) {
                hqList.add(hq);
            }
        }

        return hqList;
    }

    @GetMapping("/distance")
    public Map<String, Object> calculateDistance(
            @RequestParam String from,
            @RequestParam String to) {

        HQList fromHQ
                = hqRepository.findByHqName(from)
                        .orElse(null);

        HQList toHQ
                = hqRepository.findByHqName(to)
                        .orElse(null);

        if (fromHQ == null || toHQ == null) {
            throw new RuntimeException("HQ Not Found");
        }

        double km = calculateKm(
                fromHQ.getLatitude(),
                fromHQ.getLongitude(),
                toHQ.getLatitude(),
                toHQ.getLongitude());

        double fare = km * 2;

        Map<String, Object> result = new HashMap<>();

        result.put("km", Math.round(km));
        result.put("fare", Math.round(fare));

        return result;
    }

    private double calculateKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final int R = 6371;

        double latDistance
                = Math.toRadians(lat2 - lat1);

        double lonDistance
                = Math.toRadians(lon2 - lon1);

        double a
                = Math.sin(latDistance / 2)
                * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c
                = 2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a));

        return R * c;
    }

    @PostMapping("/save")
    public HQList saveHQ(@RequestBody HQList hq) {
        return hqRepository.save(hq);
    }

    @GetMapping("/all")
    public List<HQList> getAllHQ() {
        return hqRepository.findAll();
    }

    @PostMapping("/mapping/save")
    public EmployeeHQMapping saveMapping(
            @RequestBody EmployeeHQMapping mapping) {

        return mappingRepository.save(mapping);
    }

    @GetMapping("/mapping/all")
    public List<Map<String, Object>> getAllMappings() {

        List<EmployeeHQMapping> mappings = mappingRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();

        for (EmployeeHQMapping m : mappings) {

            Employee emp = employeeRepository.findById(m.getEmployeeId()).orElse(null);

            HQList hq
                    = hqRepository.findById(m.getHqId()).orElse(null);

            Map<String, Object> row = new HashMap<>();

            row.put("id", m.getId());

            row.put("employeeName",
                    emp != null ? emp.getName() : "");

            row.put("hqName",
                    hq != null ? hq.getHqName() : "");

            result.add(row);
        }

        return result;
    }

    @DeleteMapping("/delete/{id}")
    public String deleteHQ(@PathVariable Long id) {

        List<EmployeeHQMapping> mappings
                = mappingRepository.findByHqId(id);

        if (!mappings.isEmpty()) {
            mappingRepository.deleteAll(mappings);
        }

        hqRepository.deleteById(id);

        return "HQ Deleted";
    }

    @DeleteMapping("/mapping/delete/{id}")
    public String deleteMapping(@PathVariable Long id) {

        mappingRepository.deleteById(id);

        return "Mapping Deleted Successfully";
    }
}
