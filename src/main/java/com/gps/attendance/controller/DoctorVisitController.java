package com.gps.attendance.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gps.attendance.entity.DoctorVisit;
import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.DoctorVisitRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.LeaveRequestRepository;
import com.gps.attendance.service.CloudinaryService;

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

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/doctor-visit/save")
    public ResponseEntity<?> saveDoctorVisit(
            @RequestBody DoctorVisit visit) {

        if (visit.getDoctorName() == null || visit.getDoctorName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Doctor name is required");
        }

        String doctorName = visit.getDoctorName().trim();

        Optional<DoctorVisit> existingVisit
                = repository.findFirstByEmployeeIdAndDoctorNameIgnoreCaseAndVisitDate(
                        visit.getEmployeeId(),
                        doctorName,
                        visit.getVisitDate()
                );

        if (existingVisit.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("This doctor visit is already submitted today.");
        }

        visit.setDoctorName(doctorName);
        visit.setVisitDate(LocalDate.now(ZoneId.of("Asia/Kolkata")).toString());
        visit.setVisitTime(LocalTime.now(ZoneId.of("Asia/Kolkata")));
        visit.setStatus("Completed");

        DoctorVisit savedVisit = repository.save(visit);

        return ResponseEntity.ok(savedVisit);
    }

    @GetMapping("/doctor-visit/history/{employeeId}")
    public List<DoctorVisit> getDoctorVisitHistory(
            @PathVariable Long employeeId) {

        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    @GetMapping("/doctor-visit/history/today/{employeeId}")
    public List<DoctorVisit> getTodayDoctorVisitHistory(
            @PathVariable Long employeeId
    ) {
        String today = LocalDate.now().toString();

        return repository.findByEmployeeIdAndVisitDateOrderByIdDesc(
                employeeId,
                today
        );
    }

    @GetMapping("/doctor-visit/count")
    public long getDoctorVisitCount() {
        return repository.count();
    }

    @GetMapping("/doctor-visit/monthly-count")
    public long getMonthlyVisitCount() {
        return repository.getCurrentMonthVisitCount();
    }

    @GetMapping("/doctor-visit/today-count")
    public long getTodayVisitCount() {
        return repository.getTodayVisitCount();
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

    @GetMapping("/doctor-visit/chart-data-weekly")
    public List<Object[]> getWeeklyChartData() {

        List<Object[]> data = repository
                .getWeeklyVisitStats()
                .stream()
                .limit(7)
                .collect(java.util.stream.Collectors.toList());

        java.util.Collections.reverse(data);

        return data;
    }

    @GetMapping("/attendance/chart-data")
    public Map<String, Long> getAttendanceChartData() {

        LocalDate today = LocalDate.now();

        long totalEmployees = employeeRepository.count();

        long present
                = attendanceRepository
                        .countByAttendanceDateAndStatus(
                                today,
                                "Present"
                        );

        long leave
                = leaveRequestRepository
                        .countByFromDateAndStatus(
                                today,
                                "Approved"
                        );

        long absent
                = Math.max(0, totalEmployees - present - leave);

        Map<String, Long> data
                = new HashMap<>();

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

    @GetMapping("/doctor-visit/directory")
    public List<DoctorVisit> getDoctorDirectory() {

        return repository.findAll()
                .stream()
                .filter(v -> v.getDoctorName() != null
                && !v.getDoctorName().isBlank())
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

        long achievement
                = repository.countByEmployeeIdAndVisitDate(employeeId, today);

        int target = 25;

        int progress
                = (int) Math.min((achievement * 100) / target, 100);

        long remaining = Math.max(0, target - achievement);

        long extraVisits = Math.max(0, achievement - target);

        // System.out.println("EMPLOYEE ID: " + employeeId);
        // System.out.println("TODAY: " + today);
        // System.out.println("ACHIEVEMENT: " + achievement);

        Map<String, Object> data = new HashMap<>();

        data.put("target", target);

        data.put("achievement", achievement);

        data.put("remaining", remaining);

        data.put("extraVisits", extraVisits);

        data.put("progress", progress);

        data.put("date", today);

        return data;
    }

    @PostMapping("/doctor-visit/upload-photo/{employeeId}")
    public String uploadDoctorVisitPhoto(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return cloudinaryService.uploadDoctorVisitImage(file, employeeId);
    }

@GetMapping("/doctor-visit/search-doctor")
public List<DoctorVisit> searchDoctor(
       @RequestParam Long employeeId,
@RequestParam String doctorName) {

    List<DoctorVisit> doctors = repository
            .findByEmployeeIdAndDoctorNameContainingIgnoreCaseOrderByDoctorNameAsc(
        employeeId,
        doctorName
)
            .stream()
            .collect(Collectors.toMap(
                    d -> (d.getDoctorName() + "|" +
                          d.getHospitalName() + "|" +
                          d.getSpecialization()).toLowerCase(),
                    d -> d,
                    (oldValue, newValue) ->
                            oldValue.getVisitDate().compareTo(newValue.getVisitDate()) >= 0
                                    ? oldValue
                                    : newValue
            ))
            .values()
            .stream()
            .toList();

    for (DoctorVisit doctor : doctors) {

        long totalVisits =
                repository.countByDoctorNameIgnoreCase(
                        doctor.getDoctorName());

        doctor.setTotalVisitCount(totalVisits);

    }

    return doctors;
}

@GetMapping("/doctor-visit/exact-doctor")
public List<DoctorVisit> getExactDoctor(

        @RequestParam Long employeeId,
        @RequestParam String doctorName) {

   List<DoctorVisit> doctors = repository
        .findByEmployeeIdAndDoctorNameIgnoreCaseOrderByHospitalNameAsc(
                employeeId,
                doctorName)
        .stream()
        .collect(Collectors.toMap(

                d -> (
                        d.getDoctorName() + "|" +
                        d.getHospitalName() + "|" +
                        d.getSpecialization()
                ).toLowerCase(),

                d -> d,

                (oldValue, newValue) ->

                        oldValue.getVisitDate().compareTo(newValue.getVisitDate()) >= 0
                                ? oldValue
                                : newValue

        ))
        .values()
        .stream()
        .toList();

for (DoctorVisit doctor : doctors) {

    doctor.setTotalVisitCount(
            repository.countByDoctorNameIgnoreCase(
                    doctor.getDoctorName()
            )
    );

}

return doctors;
        
}

    @PutMapping("/doctor-visit/update/{id}")
    public DoctorVisit updateDoctorVisit(
            @PathVariable Long id,
            @RequestBody DoctorVisit updatedVisit
    ) {
        DoctorVisit visit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found"));

        visit.setWorkingWith(updatedVisit.getWorkingWith());
        visit.setWorkingPersonName(updatedVisit.getWorkingPersonName());
        visit.setRemarks(updatedVisit.getRemarks());
        visit.setLandmark(updatedVisit.getLandmark());

        return repository.save(visit);
    }

    @GetMapping("/employee/{id}/visit-count")
    public Long getVisitCount(@PathVariable Long id) {
        return repository.countByEmployeeId(id);
    }

@GetMapping("/doctor-visit/search-party")
public List<DoctorVisit> searchParty(
        @RequestParam Long employeeId,
        @RequestParam String visitCategory,
        @RequestParam String name
) {
    return repository
            .findByEmployeeIdAndVisitCategoryAndDoctorNameContainingIgnoreCaseOrderByDoctorNameAsc(
                    employeeId,
                    visitCategory,
                    name
            );
}

@GetMapping("/doctor-visit/exact-party")
public List<DoctorVisit> getExactParty(
        @RequestParam Long employeeId,
        @RequestParam String visitCategory,
        @RequestParam String name
) {
    return repository
            .findByEmployeeIdAndVisitCategoryAndDoctorNameIgnoreCaseOrderByHospitalNameAsc(
                    employeeId,
                    visitCategory,
                    name
            );
}

@GetMapping("/doctor-visit/category-summary/{employeeId}")
public Map<String, Long> getCategorySummary(
        @PathVariable Long employeeId
) {
    Map<String, Long> result = new HashMap<>();

    result.put(
            "doctors",
            repository.countByEmployeeIdAndVisitCategory(
                    employeeId,
                    "DOCTOR"
            )
    );

    result.put(
            "chemists",
            repository.countByEmployeeIdAndVisitCategory(
                    employeeId,
                    "CHEMIST"
            )
    );

    return result;
}

@GetMapping("/doctor-visit/list/{employeeId}")
public List<DoctorVisit> getPartyList(
        @PathVariable Long employeeId,
        @RequestParam(required = false) String category
) {
    if (category != null && !category.isBlank()) {
        return repository
                .findByEmployeeIdAndVisitCategoryOrderByIdDesc(
                        employeeId,
                        category
                );
    }

    return repository.findByEmployeeIdOrderByIdDesc(employeeId);
}

@GetMapping("/doctor-visit/unique-parties/{employeeId}")
public List<DoctorVisit> getUniqueParties(
        @PathVariable Long employeeId,
        @RequestParam String category
) {
    List<DoctorVisit> visits =
            repository
                    .findByEmployeeIdAndVisitCategoryOrderByVisitDateDesc(
                            employeeId,
                            category
                    );

    Map<String, DoctorVisit> uniqueMap = new LinkedHashMap<>();

    for (DoctorVisit visit : visits) {
        String name = visit.getDoctorName();

        if (name == null || name.isBlank()) {
            continue;
        }

        String uniqueKey =
                name.trim().toLowerCase()
                + "|"
                + (visit.getHospitalName() == null
                    ? ""
                    : visit.getHospitalName().trim().toLowerCase());

        uniqueMap.putIfAbsent(uniqueKey, visit);
    }

    return new ArrayList<>(uniqueMap.values());
}

}
