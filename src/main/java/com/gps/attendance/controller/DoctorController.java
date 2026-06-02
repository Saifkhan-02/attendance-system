package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Doctor;
import com.gps.attendance.repository.DoctorRepository;

@RestController
@RequestMapping("/admin")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @PostMapping("/save-doctor")
    public Doctor saveDoctor(@ModelAttribute Doctor doctor){

        return doctorRepository.save(doctor);

    }
    @GetMapping("/get-doctors")
       public List<Doctor> getDoctors() {

    return doctorRepository.findAll();

}
}