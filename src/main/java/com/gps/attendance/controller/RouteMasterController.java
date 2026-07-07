package com.gps.attendance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.RouteMaster;
import com.gps.attendance.repository.RouteMasterRepository;

@RestController
@CrossOrigin("*")
@RequestMapping("/route-master")
public class RouteMasterController {

    private final RouteMasterRepository routeMasterRepository;

    public RouteMasterController(RouteMasterRepository routeMasterRepository) {
        this.routeMasterRepository = routeMasterRepository;
    }

    @GetMapping("/all")
    public List<RouteMaster> getAllRoutes() {
        return routeMasterRepository.findAll();
    }

    @GetMapping("/by-headquarter/{headquarterName}")
    public List<RouteMaster> getRoutesByHeadquarter(@PathVariable String headquarterName) {
        return routeMasterRepository.findByHeadquarterName(headquarterName);
    }

    @GetMapping("/active/by-headquarter/{headquarterName}")
    public List<RouteMaster> getActiveRoutesByHeadquarter(@PathVariable String headquarterName) {
        return routeMasterRepository.findByHeadquarterNameAndStatus(headquarterName, "Active");
    }

    @PostMapping("/save")
    public RouteMaster saveRoute(@RequestBody RouteMaster routeMaster) {
        routeMaster.setStatus("Active");
        return routeMasterRepository.save(routeMaster);
    }

    @PutMapping("/update/{id}")
    public RouteMaster updateRoute(@PathVariable Long id, @RequestBody RouteMaster updated) {
        RouteMaster route = routeMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setHeadquarterName(updated.getHeadquarterName());
        route.setRouteName(updated.getRouteName());
        route.setDistanceKm(updated.getDistanceKm());
        route.setRouteType(updated.getRouteType());
        route.setStatus(updated.getStatus());

        return routeMasterRepository.save(route);
    }

    @PutMapping("/toggle-status/{id}")
    public RouteMaster toggleStatus(@PathVariable Long id) {
        RouteMaster route = routeMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if ("Active".equalsIgnoreCase(route.getStatus())) {
            route.setStatus("Inactive");
        } else {
            route.setStatus("Active");
        }

        return routeMasterRepository.save(route);
    }

    @DeleteMapping("/delete/{id}")
public ResponseEntity<String> deleteRoute(@PathVariable Long id) {
    routeMasterRepository.deleteById(id);
    return ResponseEntity.ok("Route deleted successfully");
}

}