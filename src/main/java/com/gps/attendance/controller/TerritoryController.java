package com.gps.attendance.controller;

import com.gps.attendance.entity.Territory;
import com.gps.attendance.repository.TerritoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/territory")
@CrossOrigin("*")
public class TerritoryController {

    private final TerritoryRepository territoryRepository;

    public TerritoryController(TerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    @PostMapping("/save")
    public Territory saveTerritory(@RequestBody Territory territory) {

        if (territory.getTerritoryName() == null ||
                territory.getTerritoryName().trim().isEmpty()) {
            throw new RuntimeException("Territory name is required");
        }

        Territory existing = territoryRepository
                .findByTerritoryNameIgnoreCase(territory.getTerritoryName().trim());

        if (existing != null) {
            throw new RuntimeException("Territory already exists");
        }

        territory.setTerritoryName(territory.getTerritoryName().trim());

        if (territory.getStatus() == null || territory.getStatus().trim().isEmpty()) {
            territory.setStatus("Active");
        }

        return territoryRepository.save(territory);
    }

    @GetMapping("/all")
    public List<Territory> getAllTerritories() {
        return territoryRepository.findAll();
    }

    @GetMapping("/active")
    public List<Territory> getActiveTerritories() {
        return territoryRepository.findByStatus("Active");
    }

    @PutMapping("/update/{id}")
    public Territory updateTerritory(@PathVariable Long id,
                                     @RequestBody Territory request) {

        Territory territory = territoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territory not found"));

        territory.setTerritoryName(request.getTerritoryName());
        territory.setStateName(request.getStateName());
        territory.setStatus(request.getStatus());

        return territoryRepository.save(territory);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteTerritory(@PathVariable Long id) {
        territoryRepository.deleteById(id);
        return "Territory deleted successfully";
    }
}