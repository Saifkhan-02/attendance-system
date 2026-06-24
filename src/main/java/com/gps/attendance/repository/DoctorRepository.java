package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gps.attendance.entity.Doctor;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

    List<Doctor> findByEmployeeId(Long employeeId);

    List<Doctor> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<Doctor> findAllByOrderByIdDesc();

    Doctor findByDoctorNameIgnoreCase(String doctorName);

}

// package com.gps.attendance.repository;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
// import com.gps.attendance.entity.Doctor;
// @Repository
// public interface DoctorRepository
//         extends JpaRepository<Doctor, Long> {
//                 List<Doctor> findByEmployeeId(Long employeeId);

// }
