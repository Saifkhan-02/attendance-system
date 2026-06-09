package com.gps.attendance.controller;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.DoctorVisit;
import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.DoctorVisitRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.LeaveRequestRepository;



@RestController
@CrossOrigin("*")
public class DoctorVisitController {

    @Autowired
    private DoctorVisitRepository repository;

    @Autowired
private AttendanceRepository attendanceRepository;

@Autowired
private LeaveRequestRepository leaveRequestRepository;

@Autowired
private EmployeeRepository employeeRepository;

    @PostMapping("/doctor-visit/save")
    public DoctorVisit saveDoctorVisit(
            @RequestBody DoctorVisit visit) {

        visit.setVisitTime(LocalTime.now());
        visit.setStatus("Completed");

        return repository.save(visit);
    }

    @GetMapping("/doctor-visit/history/{employeeId}")
    public List<DoctorVisit> getDoctorVisitHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }

    @GetMapping("/doctor-visit/count")
    public long getDoctorVisitCount() {
        return repository.count();
    }

    @GetMapping("/doctor-visit/all")
    public List<DoctorVisit> getAllDoctorVisits() {
        return repository.findAll();
    }

    @GetMapping("/doctor-visit/top10")
    public List<DoctorVisit> getTop10DoctorVisits() {

        return repository.findAllByOrderByIdDesc(
                PageRequest.of(0, 10)
        );
    }

    @GetMapping("/doctor-visit/search/{keyword}")
    public List<DoctorVisit> searchDoctorVisits(
            @PathVariable String keyword) {

        return repository
                .findByEmployeeNameContainingIgnoreCase(keyword);
    }

    @GetMapping("/doctor-visit/chart-data")
    public List<Object[]> getChartData() {

        return repository.getMonthlyVisitStats();
    }

    @GetMapping("/doctor-visit/chart-data-daily")
    public List<Object[]> getDailyChartData() {

        return repository.getDailyVisitStats();
    }

    @GetMapping("/attendance/chart-data")
public Map<String, Long> getAttendanceChartData() {

    long totalEmployees = employeeRepository.count();

    long present =
            attendanceRepository.countByStatus("Present");

    long leave =
            leaveRequestRepository.countByStatus("Approved");

    long absent =
            totalEmployees - present - leave;

    Map<String, Long> data =
            new HashMap<>();

    data.put("present", present);
    data.put("absent", absent);
    data.put("leave", leave);

    return data;
}
@GetMapping("/doctor-visit/unique-doctors/{employeeId}")
public List<DoctorVisit> getUniqueDoctors(@PathVariable Long employeeId) {
    return repository.findByEmployeeId(employeeId)
            .stream()
            .filter(v -> v.getDoctorName() != null && !v.getDoctorName().isBlank())
            .collect(Collectors.toMap(
                    DoctorVisit::getDoctorName,
                    v -> v,
                    (oldValue, newValue) -> newValue
            ))
            .values()
            .stream()
            .toList();
}

@GetMapping("/doctor-visit/daily-target/{employeeId}")
public Map<String, Object> getDailyTarget(@PathVariable Long employeeId) {

    String today = java.time.LocalDate.now().toString();

    long achievement =
            repository.countByEmployeeIdAndVisitDate(employeeId, today);

    int target = 25;

    int progress =
            (int) Math.min((achievement * 100) / target, 100);

    Map<String, Object> data = new HashMap<>();

    data.put("target", target);
    data.put("achievement", achievement);
    data.put("progress", progress);
    data.put("date", today);

    return data;
}
// @PostMapping("/doctor-visit/save")
// public DoctorVisit saveVisit(
//         @RequestBody DoctorVisit visit){
//     visit.setVisitTime(LocalTime.now());
//     visit.setStatus("Completed");
//     return repository.save(visit);
// }
}
