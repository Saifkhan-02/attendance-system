package com.gps.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Distributor;
import com.gps.attendance.repository.DistributorRepository;

@RestController
@CrossOrigin("*")
public class DistributorController {

    private final DistributorRepository distributorRepository;

    public DistributorController(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    @GetMapping("/distributor/by-headquarter/{headquarters}")
    public List<Distributor> getDistributorsByHeadquarters(
            @PathVariable String headquarters
    ) {
        return distributorRepository
                .findByHeadquartersIgnoreCaseAndStatus(headquarters, "Active");
    }

    @GetMapping("/distributor/all")
public List<Distributor> getAllDistributors() {
    return distributorRepository.findAll();
}

@GetMapping("/distributor/headquarters")
public List<String> getHeadquarters() {
    return distributorRepository.findDistinctActiveHeadquarters();
}
}