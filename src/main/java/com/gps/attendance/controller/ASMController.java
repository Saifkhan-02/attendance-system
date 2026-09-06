package com.gps.attendance.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import com.gps.attendance.dto.ASMHQResponse;
import com.gps.attendance.entity.TourPlan;
import com.gps.attendance.repository.TourPlanRepository;
import com.gps.attendance.entity.Employee;
import com.gps.attendance.entity.EmployeeHQMapping;

import com.gps.attendance.entity.Headquarter;
import com.gps.attendance.entity.LeaveRequest;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.entity.RouteMaster;
import com.gps.attendance.repository.EmployeeHQMappingRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.HeadquarterRepository;
import com.gps.attendance.repository.ProductOrderRepository;
import com.gps.attendance.repository.RouteMasterRepository;

import java.time.LocalDate;
import com.gps.attendance.entity.Attendance;
import com.gps.attendance.entity.DoctorVisit;

import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.DoctorVisitRepository;

import com.gps.attendance.repository.LeaveRequestRepository;

@RestController
@RequestMapping("/asm")
@CrossOrigin("*")
public class ASMController {

        private final EmployeeRepository employeeRepository;
        private final EmployeeHQMappingRepository mappingRepository;
        private final HeadquarterRepository headquarterRepository;
        private final ProductOrderRepository orderRepository;
        private final AttendanceRepository attendanceRepository;
        private final DoctorVisitRepository doctorVisitRepository;
        private final TourPlanRepository tourPlanRepository;
        private final LeaveRequestRepository leaveRequestRepository;
        private final RouteMasterRepository routeRepository;

        public ASMController(
                        EmployeeRepository employeeRepository,
                        EmployeeHQMappingRepository mappingRepository,
                        HeadquarterRepository headquarterRepository,
                        ProductOrderRepository orderRepository,
                        AttendanceRepository attendanceRepository,
                        DoctorVisitRepository doctorVisitRepository,
                        TourPlanRepository tourPlanRepository,
                        LeaveRequestRepository leaveRequestRepository,
                        RouteMasterRepository routeRepository) {
                this.employeeRepository = employeeRepository;
                this.mappingRepository = mappingRepository;
                this.headquarterRepository = headquarterRepository;
                this.orderRepository = orderRepository;
                this.attendanceRepository = attendanceRepository;
                this.doctorVisitRepository = doctorVisitRepository;
                this.tourPlanRepository = tourPlanRepository;
                this.leaveRequestRepository = leaveRequestRepository;
                this.routeRepository = routeRepository;
        }

        // ==========================================
        // NEWLY ADDED: Phase-1 HQ Dropdown & Summary APIs
        // ==========================================

        // 1. Get Assigned Headquarters for ASM Dropdown (DTO based response)
        @GetMapping("/{asmId}/headquarters")
        public List<ASMHQResponse> getASMHQ(@PathVariable Long asmId) {
                List<EmployeeHQMapping> mappings = mappingRepository.findByEmployeeId(asmId);
                List<ASMHQResponse> list = new ArrayList<>();

                if (mappings != null && !mappings.isEmpty()) {
                        List<Long> hqIds = mappings.stream()
                                        .map(EmployeeHQMapping::getHqId)
                                        .collect(Collectors.toList());
                        
                        List<Headquarter> hqs = headquarterRepository.findAllById(hqIds);
                        for (Headquarter h : hqs) {
                                list.add(new ASMHQResponse(h.getId(), h.getHeadquarterName()));
                        }
                }
                return list;
        }

        // 2. Get HQ Summary (MR count, Route count)
        @GetMapping("/hq-summary")
        public Map<String, Object> hqSummary(@RequestParam Long asmId, @RequestParam Long hqId) {
                Headquarter hq = headquarterRepository.findById(hqId)
                                .orElseThrow(() -> new RuntimeException("HQ not found"));
                
                List<Employee> mrs = employeeRepository.findByHeadquartersInAndRoleIgnoreCase(List.of(hq.getHeadquarterName()), "MR");
                List<RouteMaster> routes = routeRepository.findByHeadquarterNameAndStatus(hq.getHeadquarterName(), "Active");

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("headquarter", hq.getHeadquarterName());
                map.put("mrCount", mrs.size());
                map.put("routeCount", routes.size());
                
                return map;
        }

        // ==========================================
        // EXISTING APIs
        // ==========================================

        @GetMapping("/team/{asmId}")
        public List<Employee> getAsmTeam(@PathVariable Long asmId) {

                Employee asm = employeeRepository.findById(asmId)
                                .orElseThrow(() -> new RuntimeException("ASM not found"));

                if (asm.getRole() == null || !asm.getRole().equalsIgnoreCase("ASM")) {
                        throw new RuntimeException("This employee is not ASM");
                }
                List<EmployeeHQMapping> mappings = mappingRepository.findByEmployeeId(asmId);
                if (mappings == null || mappings.isEmpty()) {
                        return new ArrayList<>();
                }
                List<Long> hqIds = mappings.stream()
                                .map(EmployeeHQMapping::getHqId)
                                .collect(Collectors.toList());
                List<Headquarter> hqs = headquarterRepository.findAllById(hqIds);
                List<String> hqNames = hqs.stream()
                                .map(Headquarter::getHeadquarterName)
                                .collect(Collectors.toList());
                if (hqNames.isEmpty()) {
                        return new ArrayList<>();
                }
                return employeeRepository.findByHeadquartersInAndRoleIgnoreCase(hqNames, "MR");
        }

        @GetMapping("/employee/{employeeId}")
        public Employee getEmployeeDetails(@PathVariable @NonNull Long employeeId) {
                return employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        @GetMapping("/employee-orders/{employeeId}")
        public List<ProductOrder> getEmployeeOrders(@PathVariable Long employeeId) {
                return orderRepository.findByEmployeeId(employeeId);
        }

        @GetMapping("/employee-sales-summary/{employeeId}")
        public Map<String, Object> getEmployeeSalesSummary(@PathVariable Long employeeId) {
                List<ProductOrder> orders = orderRepository.findByEmployeeId(employeeId);
                double totalSale = 0;
                double totalCollection = 0;
                double totalDue = 0;
                for (ProductOrder order : orders) {
                        totalSale += order.getOrderAmount() == null ? 0 : order.getOrderAmount();
                        totalCollection += order.getPaidAmount() == null ? 0 : order.getPaidAmount();
                        totalDue += order.getDueAmount() == null ? 0 : order.getDueAmount();
                }
                Map<String, Object> result = new HashMap<>();
                result.put("totalOrders", orders.size());
                result.put("totalSale", totalSale);
                result.put("totalCollection", totalCollection);
                result.put("totalDue", totalDue);
                return result;
        }

        @GetMapping("/employee-summary/{employeeId}")
        public Map<String, Object> getEmployeeSummary(@PathVariable @NonNull Long employeeId) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));
                String today = LocalDate.now().toString();
                LocalDate todayDate = LocalDate.now();
                String month = today.substring(0, 7);
                long todayTourPlans = tourPlanRepository.countByEmployeeIdAndTravelDate(employeeId, todayDate);
                long todayVisits = doctorVisitRepository
                                .countByEmployeeIdAndVisitDate(employeeId, today);
                long monthVisits = doctorVisitRepository
                                .countByEmployeeIdAndVisitDateStartingWith(employeeId, month);
                long todayOrders = orderRepository.countDailyInvoices(employeeId, today);
                long monthOrders = orderRepository.countInvoices(
                                employeeId,
                                today.substring(0, 4),
                                today.substring(5, 7));
                List<Object[]> todaySalesRows = orderRepository.getTodaySalesSummaryByEmployee(employeeId, today);
                double todaySales = 0;
                double todayCollection = 0;
                double todayDue = 0;
                if (todaySalesRows != null && !todaySalesRows.isEmpty()) {
                        Object[] row = todaySalesRows.get(0);
                        todaySales = ((Number) row[0]).doubleValue();
                        todayCollection = ((Number) row[1]).doubleValue();
                        todayDue = ((Number) row[2]).doubleValue();
                }
                List<TourPlan> todayPlans = tourPlanRepository
                                .findByEmployeeIdAndTravelDateOrderByIdDesc(
                                                employeeId,
                                                todayDate);
                double todayExpense = todayPlans.stream()
                                .mapToDouble(plan -> plan.getTotalExpense() == null
                                                ? 0.0
                                                : plan.getTotalExpense())
                                .sum();
                List<Attendance> todayAttendance = attendanceRepository.findByEmployeeId(employeeId);
                String attendanceStatus = "Not Marked Yet";
                for (Attendance a : todayAttendance) {
                        if (a.getAttendanceDate() != null
                                        && a.getAttendanceDate().toString().equals(today)) {
                                attendanceStatus = a.getStatus();
                                break;
                        }
                }
                Map<String, Object> result = new HashMap<>();
                result.put("employeeId", employee.getId());
                result.put("name", employee.getName());
                result.put("role", employee.getRole());
                result.put("headquarters", employee.getHeadquarters());
                result.put("mobile", employee.getMobile());
                result.put("status", employee.getStatus());
                result.put("attendanceStatus", attendanceStatus);
                result.put("todayVisits", todayVisits);
                result.put("monthVisits", monthVisits);
                result.put("todayOrders", todayOrders);
                result.put("monthOrders", monthOrders);
                result.put("totalSales", todaySales);
                result.put("totalCollection", todayCollection);
                result.put("totalDue", todayDue);
                result.put("totalExpense", todayExpense);
                result.put("todayTourPlans", todayTourPlans);
                return result;
        }

        @GetMapping("/top-performer/{asmId}")
        public Map<String, Object> getTopPerformer(@PathVariable Long asmId) {
                List<Employee> team = getAsmTeam(asmId);
                Employee bestEmployee = null;
                double highestSales = 0;
                for (Employee employee : team) {
                        Double sale = orderRepository
                                        .getTotalSalesByEmployee(employee.getId());
                        if (sale == null) {
                                sale = 0.0;
                        }
                        if (sale > highestSales) {
                                highestSales = sale;
                                bestEmployee = employee;
                        }
                }
                Map<String, Object> map = new HashMap<>();
                if (bestEmployee == null) {
                        map.put("employeeId", 0);
                        map.put("name", "-");
                        map.put("headquarters", "-");
                        map.put("sales", 0);
                        map.put("visits", 0);
                        map.put("orders", 0);
                        return map;
                }
                long visits = doctorVisitRepository
                                .countByEmployeeId(bestEmployee.getId());
                long orders = orderRepository
                                .countOrdersByEmployeeId(bestEmployee.getId());
                map.put("employeeId", bestEmployee.getId());
                map.put("name", bestEmployee.getName());
                map.put("headquarters", bestEmployee.getHeadquarters());
                map.put("sales", highestSales);
                map.put("visits", visits);
                map.put("orders", orders);
                return map;
        }

        @GetMapping("/mr-attendance/{employeeId}")
        public List<Attendance> getMrAttendance(@PathVariable Long employeeId) {
                return attendanceRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        }

        @GetMapping("/mr-visits/{employeeId}")
        public List<DoctorVisit> getMrVisits(@PathVariable Long employeeId) {
                return doctorVisitRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        }

        @GetMapping("/mr-orders/{employeeId}")
        public List<ProductOrder> getMrOrders(@PathVariable Long employeeId) {
                return orderRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        }

        @GetMapping("/mr-expenses/{employeeId}")
        public List<TourPlan> getMrExpenses(@PathVariable Long employeeId) {
                return tourPlanRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        }

@GetMapping("/dashboard-fast/{asmId}")
        public Map<String, Object> getDashboardFast(@PathVariable Long asmId) {
                List<Employee> team = getAsmTeam(asmId);
                String todayStr = LocalDate.now().toString();
                LocalDate todayDate = LocalDate.now();
                Map<String, Object> response = new HashMap<>();
                if (team == null || team.isEmpty()) {
                        response.put("date", todayStr);
                        response.put("team", 0);
                        response.put("present", 0);
                        response.put("visits", 0);
                        response.put("orders", 0);
                        response.put("sales", 0);
                        response.put("collection", 0);
                        response.put("due", 0);
                        response.put("expense", 0);
                        response.put("leaderboard", new ArrayList<>());
                        return response;
                }
                List<Long> employeeIds = team.stream()
                                .map(Employee::getId)
                                .collect(Collectors.toList());
                
                Map<Long, Long> attendanceMap = new HashMap<>();
                for (Object[] row : attendanceRepository.countTodayAttendanceByEmployees(employeeIds, todayDate)) {
                        attendanceMap.put((Long) row[0], (Long) row[1]);
                }
                
                Map<Long, Long> visitMap = new HashMap<>();
                for (Object[] row : doctorVisitRepository.countTodayVisitsByEmployees(employeeIds, todayStr)) {
                        visitMap.put((Long) row[0], (Long) row[1]);
                }
                
                Map<Long, Long> todayOrderMap = new HashMap<>();
                for (Object[] row : orderRepository.countTodayOrdersByEmployees(employeeIds, todayStr)) {
                        todayOrderMap.put((Long) row[0], (Long) row[1]);
                }

                // 1. Initialize salesMap here
                Map<Long, Object[]> salesMap = new HashMap<>();
                for (Object[] row : orderRepository.getSalesSummaryByEmployees(employeeIds)) {
                        salesMap.put((Long) row[0], row);
                }
                
      // 1. Team ke har employee ki Aaj ki (Today) aur Monthly sales nikalne ke liye sahi logic
String monthStr = todayStr.substring(0, 7); // e.g., "2026-08"
Map<Long, Double> todaySalesMap = new HashMap<>();
Map<Long, Double> monthlySalesMap = new HashMap<>();

for (Employee e : team) {
    List<ProductOrder> empOrders = orderRepository.findByEmployeeId(e.getId());
    double todaySum = 0;
    double monthlySum = 0;

    if (empOrders != null) {
        for (ProductOrder ord : empOrders) {
            if (ord.getOrderDate() != null) {
                String ordDate = ord.getOrderDate().trim();
                
                // Monthly Sale (Match YYYY-MM)
                if (ordDate.startsWith(monthStr)) {
                    monthlySum += (ord.getOrderAmount() != null ? ord.getOrderAmount() : 0.0);
                }
                
                // Today's Order Value (Match exact YYYY-MM-DD)
                if (ordDate.equals(todayStr.trim())) {
                    todaySum += (ord.getOrderAmount() != null ? ord.getOrderAmount() : 0.0);
                }
            }
        }
    }
    todaySalesMap.put(e.getId(), todaySum);
    monthlySalesMap.put(e.getId(), monthlySum);
}        Map<Long, Double> expenseMap = new HashMap<>();
                for (Object[] row : tourPlanRepository.getExpenseSummaryByEmployees(employeeIds)) {
                        expenseMap.put(
                                        (Long) row[0],
                                        ((Number) row[1]).doubleValue());
                }

                List<Map<String, Object>> leaderboard = new ArrayList<>();
                long totalPresent = 0;
                long totalVisits = 0;
                long totalOrders = 0;
                double totalSales = 0;
                double totalCollection = 0;
                double totalDue = 0;
                double totalExpense = 0;

                for (Employee e : team) {
                        Long id = e.getId();
                        long present = attendanceMap.getOrDefault(id, 0L);
                        long visits = visitMap.getOrDefault(id, 0L);
                        long orders = todayOrderMap.getOrDefault(id, 0L);
                        double sales = 0;
                        double collection = 0;
                        double due = 0;
                        
                        Object[] salesRow = salesMap.get(id);
                        if (salesRow != null) {
                                sales = ((Number) salesRow[2]).doubleValue();
                                collection = ((Number) salesRow[3]).doubleValue();
                                due = ((Number) salesRow[4]).doubleValue();
                        }
                        
                        double monthlySales = monthlySalesMap.getOrDefault(id, 0.0);
                        double expense = expenseMap.getOrDefault(id, 0.0);
                         double todayOrderVal = todaySalesMap.getOrDefault(id, 0.0);
                

                        if (present > 0)
                                totalPresent++;
                        totalVisits += visits;
                        totalOrders += orders;
                        totalSales += monthlySales; 
                        totalCollection += collection;
                        totalDue += due;
                        totalExpense += expense;

                        Map<String, Object> row = new HashMap<>();
                        row.put("employeeId", id);
                        row.put("employeeName", e.getName());
                        row.put("headquarters", e.getHeadquarters());
                       // row.put("date", orderDateStr); // Ab yahan aaj ki date ki jagah order ki real date dikhegi
                        row.put("attendance", present > 0 ? "Present" : "Not Marked Yet");
                        row.put("visits", visits);
                        row.put("orders", orders);
                        row.put("todayOrderValue", todayOrderVal); 
                        row.put("sales", monthlySales);            
                        row.put("collection", collection);
                        row.put("due", due);
                        row.put("expense", expense);
                        leaderboard.add(row);
                }

                leaderboard.sort((a, b) -> Double.compare(
                                Double.parseDouble(b.get("sales").toString()),
                                Double.parseDouble(a.get("sales").toString())));
                
                int rank = 1;
                for (Map<String, Object> row : leaderboard) {
                        row.put("rank", rank++);
                }

                response.put("date", todayStr);
                response.put("team", team.size());
                response.put("present", totalPresent);
                response.put("visits", totalVisits);
                response.put("orders", totalOrders);
                response.put("sales", totalSales);
                response.put("collection", totalCollection);
                response.put("due", totalDue);
                response.put("expense", totalExpense);
                response.put("leaderboard", leaderboard);
                return response;
        }

        @GetMapping("/working-with/{asmId}")
        public List<DoctorVisit> getWorkingWith(@PathVariable Long asmId) {
                List<Employee> team = getAsmTeam(asmId);
                if (team == null || team.isEmpty()) {
                        return new ArrayList<>();
                }
                List<Long> employeeIds = team.stream()
                                .map(Employee::getId)
                                .collect(Collectors.toList());
                return doctorVisitRepository.findByEmployeeIdInOrderByIdDesc(employeeIds);
        }

        @GetMapping("/employee-details-fast/{employeeId}")
        public Map<String, Object> getEmployeeDetailsFast(
                        @PathVariable Long employeeId,
                        @RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));
                List<Attendance> attendance;
                List<DoctorVisit> visits;
                List<ProductOrder> orders;
                List<TourPlan> tourPlans;
                List<LeaveRequest> leaves;
                boolean hasFromDate = fromDate != null && !fromDate.isBlank();
                boolean hasToDate = toDate != null && !toDate.isBlank();
                if (hasFromDate && hasToDate) {
                        LocalDate from = LocalDate.parse(fromDate);
                        LocalDate to = LocalDate.parse(toDate);
                        if (to.isBefore(from)) {
                                throw new IllegalArgumentException(
                                                "To Date cannot be before From Date");
                        }
                        attendance = attendanceRepository
                                        .findByEmployeeIdAndAttendanceDateBetweenOrderByIdDesc(employeeId, from, to);
                        visits = doctorVisitRepository
                                        .findByEmployeeIdAndVisitDateBetweenOrderByIdDesc(employeeId, fromDate, toDate);
                        orders = orderRepository
                                        .findByEmployeeIdAndOrderDateBetweenOrderByIdDesc(employeeId, fromDate, toDate);
                        tourPlans = tourPlanRepository
                                        .findByEmployeeIdAndTravelDateBetweenOrderByIdDesc(employeeId, from, to);
                        leaves = leaveRequestRepository
                                        .findByEmployeeIdAndFromDateBetweenOrderByIdDesc(employeeId, from, to);
                } else {
                        attendance = attendanceRepository
                                        .findByEmployeeIdOrderByIdDesc(employeeId);
                        visits = doctorVisitRepository
                                        .findByEmployeeIdOrderByIdDesc(employeeId);
                        orders = orderRepository
                                        .findByEmployeeIdOrderByIdDesc(employeeId);
                        tourPlans = tourPlanRepository
                                        .findByEmployeeIdOrderByIdDesc(employeeId);
                        leaves = leaveRequestRepository
                                        .findByEmployeeIdOrderByIdDesc(employeeId);
                }
                Map<String, Object> summary = getEmployeeSummary(employeeId);
                Map<String, Object> result = new HashMap<>();
                result.put("employee", employee);
                result.put("summary", summary);
                result.put("attendance", attendance);
                result.put("visits", visits);
                result.put("orders", orders);
                result.put("tourPlans", tourPlans);
                result.put("leaves", leaves);
                return result;
        }

        @GetMapping("/headquarters/{asmId}")
        public List<String> getAsmHeadquarters(@PathVariable Long asmId) {

                List<EmployeeHQMapping> mappings = mappingRepository.findByEmployeeId(asmId);

                if (mappings == null || mappings.isEmpty()) {
                        return new ArrayList<>();
                }

                List<Long> hqIds = mappings.stream()
                                .map(EmployeeHQMapping::getHqId)
                                .collect(Collectors.toList());

                List<Headquarter> hqs = headquarterRepository.findAllById(hqIds);

                return hqs.stream()
                                .map(Headquarter::getHeadquarterName)
                                .collect(Collectors.toList());
        }

        @GetMapping("/all-headquarters")
        public List<Headquarter> getAllHeadquarters() {
                return headquarterRepository.findAll();
        }

        @PutMapping("/headquarters/{asmId}")
        @Transactional
        public String updateAsmHeadquarters(
                        @PathVariable Long asmId,
                        @RequestBody List<Long> hqIds) {
                mappingRepository.deleteByEmployeeId(asmId);

                if (hqIds != null) {
                        for (Long hqId : hqIds) {
                                EmployeeHQMapping mapping = new EmployeeHQMapping();
                                mapping.setEmployeeId(asmId);
                                mapping.setHqId(hqId);

                                mappingRepository.save(mapping);
                        }
                }

                return "ASM headquarters updated successfully";
        }

        @GetMapping("/team-attendance/daily/{asmId}")
        public List<Map<String, Object>> getTeamDailyAttendance(
                        @PathVariable Long asmId,
                        @RequestParam String date) {
                List<Employee> team = getAsmTeam(asmId);
                List<Map<String, Object>> result = new ArrayList<>();

                if (team == null || team.isEmpty()) {
                        return result;
                }
                LocalDate selectedDate = LocalDate.parse(date);
                List<Long> employeeIds = team.stream()
                                .map(Employee::getId)
                                .collect(Collectors.toList());
                List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdInAndAttendanceDate(
                                employeeIds,
                                selectedDate);
                Map<Long, Attendance> attendanceMap = new HashMap<>();

                for (Attendance attendance : attendanceList) {
                        attendanceMap.put(attendance.getEmployeeId(), attendance);
                }

                LocalDate today = LocalDate.now();

                for (Employee employee : team) {
                        Attendance attendance = attendanceMap.get(employee.getId());

                        String status;
                        String attendanceTime = "-";
                        String location = "-";

                        if (attendance != null) {
                                status = attendance.getStatus() == null
                                                ? "Present"
                                                : attendance.getStatus();

                                if (attendance.getAttendanceTime() != null) {
                                        attendanceTime = attendance.getAttendanceTime().toString();
                                }

                                if (attendance.getLocation() != null) {
                                        location = attendance.getLocation();
                                }
                        } else {
                                status = selectedDate.equals(today)
                                                ? "Not Marked Yet"
                                                : "Absent";
                        }

                        Map<String, Object> row = new HashMap<>();
                        row.put("employeeId", employee.getId());
                        row.put("employeeName", employee.getName());
                        row.put("headquarters", employee.getHeadquarters());
                        row.put("date", selectedDate);
                        row.put("attendanceTime", attendanceTime);
                        row.put("location", location);
                        row.put("status", status);

                        result.add(row);
                }

                return result;
        }

        @GetMapping("/team-attendance/monthly/{asmId}")
        public List<Map<String, Object>> getTeamMonthlyAttendance(
                        @PathVariable Long asmId,
                        @RequestParam String month) {
                List<Employee> team = getAsmTeam(asmId);
                List<Map<String, Object>> result = new ArrayList<>();
                if (team == null || team.isEmpty()) {
                        return result;
                }
                LocalDate fromDate = LocalDate.parse(month + "-01");
                LocalDate monthEnd = fromDate.plusMonths(1).minusDays(1);
                LocalDate today = LocalDate.now();
                LocalDate toDate = monthEnd.isAfter(today) ? today : monthEnd;
                List<Long> employeeIds = team.stream()
                                .map(Employee::getId)
                                .collect(Collectors.toList());
                List<Attendance> attendanceList = attendanceRepository
                                .findByEmployeeIdInAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                                                employeeIds,
                                                fromDate,
                                                toDate);
                Map<Long, Long> presentMap = new HashMap<>();
                Map<Long, Long> leaveMap = new HashMap<>();
                for (Attendance attendance : attendanceList) {
                        Long employeeId = attendance.getEmployeeId();
                        String status = attendance.getStatus() == null
                                        ? ""
                                        : attendance.getStatus().trim();
                        if ("Present".equalsIgnoreCase(status)) {
                                presentMap.put(employeeId, presentMap.getOrDefault(employeeId, 0L) + 1);
                        } else if ("Leave".equalsIgnoreCase(status)
                                        || "Approved Leave".equalsIgnoreCase(status)) {

                                leaveMap.put(employeeId, leaveMap.getOrDefault(employeeId, 0L) + 1);
                        }
                }
                long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
                for (Employee employee : team) {
                        long presentDays = presentMap.getOrDefault(employee.getId(), 0L);
                        long leaveDays = leaveMap.getOrDefault(employee.getId(), 0L);
                        long absentDays = Math.max(elapsedDays - presentDays - leaveDays, 0);
                        double percentage = elapsedDays == 0 ? 0 : (presentDays * 100.0) / elapsedDays;
                        Map<String, Object> row = new HashMap<>();
                        row.put("employeeId", employee.getId());
                        row.put("employeeName", employee.getName());
                        row.put("headquarters", employee.getHeadquarters());
                        row.put("month", month);
                        row.put("presentDays", presentDays);
                        row.put("absentDays", absentDays);
                        row.put("leaveDays", leaveDays);
                        row.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
                        result.add(row);
                }
                return result;
        }
// Get Active Routes for a Selected HQ (ASM side)
    @GetMapping("/routes")
    public List<RouteMaster> getRoutesByHeadquarter(@RequestParam String headquarterName) {
        return routeRepository.findByHeadquarterNameAndStatus(headquarterName, "Active");
    }
// Submit ASM Tour Plan
    @PostMapping("/tour/submit")
    @Transactional
    public ResponseEntity<?> submitTourPlan(@RequestBody TourPlan tourPlan) {
        // Ensure role or identifier is marked for ASM if required by entity
        tourPlanRepository.save(tourPlan);
        return ResponseEntity.ok().body("{\"message\": \"Tour plan submitted successfully\"}");
    }
// Get ASM Tour History
    @GetMapping("/tour/history/{asmId}")
    public List<TourPlan> getTourHistory(@PathVariable Long asmId) {
        return tourPlanRepository.findByEmployeeIdOrderByIdDesc(asmId);
    }

    // Get all active routes for all assigned HQs of an ASM
    @GetMapping("/routes/{asmId}")
    public List<RouteMaster> getAsmRoutes(@PathVariable Long asmId) {
        // 1. Get all assigned HQs for the ASM
        List<EmployeeHQMapping> mappings = mappingRepository.findByEmployeeId(asmId);
        if (mappings == null || mappings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> hqIds = mappings.stream()
                        .map(EmployeeHQMapping::getHqId)
                        .collect(Collectors.toList());

        List<Headquarter> hqs = headquarterRepository.findAllById(hqIds);
        if (hqs.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> hqNames = hqs.stream()
                        .map(Headquarter::getHeadquarterName)
                        .collect(Collectors.toList());

        // 2. Return active routes belonging to these headquarters
        return routeRepository.findByHeadquarterNameInAndStatus(hqNames, "Active");
    }

}