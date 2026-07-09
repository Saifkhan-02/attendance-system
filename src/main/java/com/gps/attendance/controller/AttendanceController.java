package com.gps.attendance.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Attendance;
import com.gps.attendance.entity.LeaveRequest;
import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.LeaveRequestRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@RestController
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceRepository repository;

    @Autowired
private EmployeeRepository employeeRepository;

@Autowired
private LeaveRequestRepository leaveRequestRepository;

    @PostMapping("/mark-attendance")
    public ResponseEntity<?> markAttendance(
            @RequestBody Attendance attendance) {

        if (attendance.getEmployeeId() == null) {
            return ResponseEntity.badRequest()
                    .body("Employee ID is required");
        }

        LocalDate today = LocalDate.now();

        if (repository.existsByEmployeeIdAndAttendanceDate(
                attendance.getEmployeeId(),
                today)) {

            return ResponseEntity
                    .badRequest()
                    .body("Attendance already marked for today");
        }

        attendance.setAttendanceDate(today);
        attendance.setAttendanceTime(LocalTime.now());

        repository.save(attendance);

        return ResponseEntity.ok("Attendance Marked Successfully");
    }

    @GetMapping("/attendance/history/{employeeId}")
    public List<Attendance> getAttendanceHistory(
            @PathVariable Long employeeId) {

        System.out.println("History API Hit : " + employeeId);
        return repository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    @GetMapping("/attendance-list")
    public List<Attendance> getAllAttendance() {
        return repository.findAllByOrderByIdDesc();
    }

    @GetMapping("/attendance-summary")
public Map<String, Long> getAttendanceSummary() {

    long total = employeeRepository.count();

    long present = repository.countByAttendanceDateAndStatus(
            LocalDate.now(),
            "Present"
    );

    long leave = leaveRequestRepository.countEmployeesOnLeave(
            LocalDate.now(),
            "Approved"
    );

    long absent = total - present - leave;

    Map<String, Long> summary = new HashMap<>();

    summary.put("totalEmployees", total);
    summary.put("presentEmployees", present);
    summary.put("absentEmployees", absent);
    summary.put("leaveEmployees", leave);

    return summary;
}
private long calculateLeaveDays(
        Long employeeId,
        int month,
        int year) {

    List<LeaveRequest> leaves =
            leaveRequestRepository.findByEmployeeIdAndStatus(
                    employeeId,
                    "Approved");

    long totalLeaveDays = 0;

    LocalDate monthStart =
            LocalDate.of(year, month, 1);

    LocalDate monthEnd =
            monthStart.withDayOfMonth(monthStart.lengthOfMonth());

    for (LeaveRequest leave : leaves) {

        LocalDate start =
                leave.getFromDate().isBefore(monthStart)
                ? monthStart
                : leave.getFromDate();

        LocalDate end =
                leave.getToDate().isAfter(monthEnd)
                ? monthEnd
                : leave.getToDate();

        if (!start.isAfter(end)) {

            totalLeaveDays += ChronoUnit.DAYS.between(start, end) + 1;

        }

    }

    return totalLeaveDays;
}


@GetMapping("/attendance/employee-summary")
public Map<String, Object> getEmployeeSummary(
        @RequestParam Long employeeId,
        @RequestParam String month) {

    String[] parts = month.split("-");

    int year = Integer.parseInt(parts[0]);
    int monthValue = Integer.parseInt(parts[1]);

    long presentDays = repository.countPresentDays(
            employeeId,
            monthValue,
            year);

//     List<Attendance> attendanceList =
//             repository.findMonthlyAttendance(
//                     employeeId,
//                     monthValue,
//                     year);

    long leaveDays = calculateLeaveDays(
        employeeId,
        monthValue,
        year
);

    int totalDays = LocalDate.of(year, monthValue, 1).lengthOfMonth();

    long absentDays = totalDays - presentDays - leaveDays;

    double attendancePercentage =
            totalDays == 0 ? 0 :
            (presentDays * 100.0) / totalDays;

    Map<String, Object> summary = new HashMap<>();

    summary.put("presentDays", presentDays);
    summary.put("absentDays", absentDays);
    summary.put("leaveDays", leaveDays);
    summary.put("attendancePercentage",
            Math.round(attendancePercentage * 100.0) / 100.0);

    return summary;
}

@GetMapping("/attendance/employee-report")
public List<Attendance> getEmployeeReport(
        @RequestParam Long employeeId,
        @RequestParam String month) {

    String[] parts = month.split("-");

    int year = Integer.parseInt(parts[0]);
    int monthValue = Integer.parseInt(parts[1]);

    return repository.findMonthlyAttendance(
            employeeId,
            monthValue,
            year
    );
}

@GetMapping("/attendance/report")
public ResponseEntity<byte[]> downloadAttendanceReport(
        @RequestParam Long employeeId,
        @RequestParam String month) {

    try {

        String[] parts = month.split("-");

        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);

        List<Attendance> attendanceList =
                repository.findMonthlyAttendance(
                        employeeId,
                        monthValue,
                        year);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        Document document = new Document();

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("ATTENDANCE REPORT"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Employee ID : " + employeeId));
        document.add(new Paragraph("Month : " + month));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Records : " + attendanceList.size()));

        document.close();

        HttpHeaders headers = new HttpHeaders();

        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=Attendance_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.internalServerError().build();

    }
}

}
