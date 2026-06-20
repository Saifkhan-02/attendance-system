package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Doctor;
import com.gps.attendance.repository.DoctorRepository;

@RestController
@CrossOrigin("*")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @PostMapping("/admin/save-doctor")
    public Doctor saveDoctor(@ModelAttribute Doctor doctor) {

        return doctorRepository.save(doctor);
    }

    @GetMapping("/admin/get-doctors")
    public List<Doctor> getDoctors() {

        return doctorRepository.findAll();
    }

    @GetMapping("/doctor/assigned/{employeeId}")
    public List<Doctor> getAssignedDoctors(
            @PathVariable Long employeeId) {

        return doctorRepository.findByEmployeeId(employeeId);
    }

    @GetMapping("/admin/doctors")
    @ResponseBody
    public List<Doctor> getAllDoctors() {

        return doctorRepository.findAll();
    }

    @GetMapping("/admin/doctor/{id}")
    public Doctor getDoctor(
            @PathVariable Long id) {

        return doctorRepository
                .findById(id)
                .orElse(null);
    }
}

// package com.gps.attendance.controller;
// import java.util.List;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import com.gps.attendance.entity.Doctor;
// import com.gps.attendance.repository.DoctorRepository;
// @RestController
// @RequestMapping("/admin")
// public class DoctorController {
//     @Autowired
//     private DoctorRepository doctorRepository;
//     @PostMapping("/save-doctor")
//     public Doctor saveDoctor(@ModelAttribute Doctor doctor){
//         return doctorRepository.save(doctor);
//     }
//     @GetMapping("/get-doctors")
//        public List<Doctor> getDoctors() {
//         return doctorRepository.findAll();
//     }
//     @GetMapping("/doctor/assigned/{employeeId}")
//         public List<Doctor> getAssignedDoctors(@PathVariable Long employeeId) {
//          return doctorRepository.findByEmployeeId(employeeId);
// }
// }
