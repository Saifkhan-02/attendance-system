package com.gps.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Headquarter;
import com.gps.attendance.repository.HeadquarterRepository;

@RestController
@CrossOrigin("*")
@RequestMapping("/headquarter")
public class HeadquarterController {

    private final HeadquarterRepository headquarterRepository;

    public HeadquarterController(HeadquarterRepository headquarterRepository) {
        this.headquarterRepository = headquarterRepository;
    }

    @GetMapping("/active")
    public List<Headquarter> getActiveHeadquarters() {
        return headquarterRepository.findByStatus("Active");
    }

    @PostMapping("/save")
    public Headquarter saveHeadquarter(@RequestBody Headquarter headquarter) {
        headquarter.setStatus("Active");
        return headquarterRepository.save(headquarter);
    }

    @GetMapping("/all")
public List<Headquarter> getAllHeadquarters() {
    return headquarterRepository.findAll();
}

@PutMapping("/toggle-status/{id}")
public Headquarter toggleStatus(@PathVariable Long id) {

    Headquarter headquarter = headquarterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Headquarter not found"));

    if ("Active".equalsIgnoreCase(headquarter.getStatus())) {
        headquarter.setStatus("Inactive");
    } else {
        headquarter.setStatus("Active");
    }

    return headquarterRepository.save(headquarter);
}
}