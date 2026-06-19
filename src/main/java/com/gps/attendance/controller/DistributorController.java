package com.gps.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

@PostMapping("/distributor/save")
public Distributor saveDistributor(@RequestBody Distributor distributor) {
    distributor.setStatus("Active");
    return distributorRepository.save(distributor);
}

@PutMapping("/distributor/update/{id}")
public Distributor updateDistributor(
        @PathVariable Long id,
        @RequestBody Distributor updated
) {
    Distributor distributor = distributorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Distributor not found"));

    distributor.setDistributorName(updated.getDistributorName());
    distributor.setHeadquarters(updated.getHeadquarters());
    distributor.setAddress(updated.getAddress());
    distributor.setStatus(updated.getStatus());

    return distributorRepository.save(distributor);
}

@PutMapping("/distributor/inactive/{id}")
public Distributor inactiveDistributor(@PathVariable Long id) {
    Distributor distributor = distributorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Distributor not found"));

    distributor.setStatus("Inactive");

    return distributorRepository.save(distributor);
}

@GetMapping("/by-headquarter/{headquarter}")
public List<Distributor> getByHeadquarter(
        @PathVariable String headquarter
) {
    return distributorRepository
            .findByHeadquartersAndStatus(
                    headquarter,
                    "Active"
            );
}

@PutMapping("/distributor/toggle-status/{id}")
public Distributor toggleStatus(@PathVariable Long id) {

    Distributor distributor = distributorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Distributor not found"));

    if ("Active".equalsIgnoreCase(distributor.getStatus())) {
        distributor.setStatus("Inactive");
    } else {
        distributor.setStatus("Active");
    }

    return distributorRepository.save(distributor);
}

}