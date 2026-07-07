package com.gps.attendance.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.gps.attendance.entity.TourPlan;
import com.gps.attendance.repository.TourPlanRepository;
import com.gps.attendance.entity.Employee;
import com.gps.attendance.entity.EmployeeHQMapping;
import com.gps.attendance.entity.Headquarter;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.repository.EmployeeHQMappingRepository;
import com.gps.attendance.repository.EmployeeRepository;
import com.gps.attendance.repository.HeadquarterRepository;
import com.gps.attendance.repository.ProductOrderRepository;

import java.time.LocalDate;
import com.gps.attendance.entity.Attendance;
import com.gps.attendance.entity.DoctorVisit;
import com.gps.attendance.entity.Expense;
import com.gps.attendance.repository.AttendanceRepository;
import com.gps.attendance.repository.DoctorVisitRepository;
import com.gps.attendance.repository.ExpenseRepository;

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
    private final ExpenseRepository expenseRepository;

    private final TourPlanRepository tourPlanRepository;

    public ASMController(
            EmployeeRepository employeeRepository,
            EmployeeHQMappingRepository mappingRepository,
            HeadquarterRepository headquarterRepository,
            ProductOrderRepository orderRepository,
            AttendanceRepository attendanceRepository,
            DoctorVisitRepository doctorVisitRepository,
            ExpenseRepository expenseRepository,
            TourPlanRepository tourPlanRepository) {

        this.employeeRepository = employeeRepository;
        this.mappingRepository = mappingRepository;
        this.headquarterRepository = headquarterRepository;
        this.orderRepository = orderRepository;

        this.attendanceRepository = attendanceRepository;
        this.doctorVisitRepository = doctorVisitRepository;
        this.expenseRepository = expenseRepository;
        this.tourPlanRepository = tourPlanRepository;
    
    }

    @GetMapping("/team/{asmId}")
    public List<Employee> getAsmTeam(@PathVariable Long asmId) {

        Employee asm = employeeRepository.findById(asmId)
                .orElseThrow(() -> new RuntimeException("ASM not found"));

        if (asm.getRole() == null || !asm.getRole().equalsIgnoreCase("ASM")) {
            throw new RuntimeException("This employee is not ASM");
        }

        List<EmployeeHQMapping> mappings =
                mappingRepository.findByEmployeeId(asmId);

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

    long todayTourPlans =
        tourPlanRepository.countByEmployeeIdAndTravelDate(employeeId, todayDate);

    long todayVisits = doctorVisitRepository
            .countByEmployeeIdAndVisitDate(employeeId, today);

    long monthVisits = doctorVisitRepository
            .countByEmployeeIdAndVisitDateStartingWith(employeeId, month);

    long todayOrders = orderRepository.countDailyInvoices(employeeId, today);

    long monthOrders = orderRepository.countInvoices(
            employeeId,
            today.substring(0, 4),
            today.substring(5, 7)
    );
    

 List<Object[]> todaySalesRows =
        orderRepository.getTodaySalesSummaryByEmployee(employeeId, today);

double todaySales = 0;
double todayCollection = 0;
double todayDue = 0;

if (todaySalesRows != null && !todaySalesRows.isEmpty()) {
    Object[] row = todaySalesRows.get(0);

    todaySales = ((Number) row[0]).doubleValue();
    todayCollection = ((Number) row[1]).doubleValue();
    todayDue = ((Number) row[2]).doubleValue();
}

Double todayExpense = expenseRepository.getTodayExpenseByEmployee(employeeId, today);
    List<Attendance> todayAttendance =
            attendanceRepository.findByEmployeeId(employeeId);

    String attendanceStatus = "Absent";

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
    result.put("totalExpense", todayExpense == null ? 0 : todayExpense);

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
public List<Expense> getMrExpenses(@PathVariable Long employeeId) {
    return expenseRepository.findByEmployeeIdOrderByIdDesc(employeeId);
}

@GetMapping("/dashboard-fast/{asmId}")
public Map<String, Object> getDashboardFast(@PathVariable Long asmId) {

    List<Employee> team = getAsmTeam(asmId);
    String todayStr = LocalDate.now().toString();
    LocalDate todayDate = LocalDate.now();

    Map<String, Object> response = new HashMap<>();

    if (team == null || team.isEmpty()) {
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

    Map<Long, Object[]> salesMap = new HashMap<>();
    for (Object[] row : orderRepository.getSalesSummaryByEmployees(employeeIds)) {
        salesMap.put((Long) row[0], row);
    }

    Map<Long, Double> expenseMap = new HashMap<>();
    for (Object[] row : expenseRepository.getExpenseSummaryByEmployees(employeeIds)) {
        expenseMap.put((Long) row[0], ((Number) row[1]).doubleValue());
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

        double expense = expenseMap.getOrDefault(id, 0.0);

        if (present > 0) totalPresent++;

        totalVisits += visits;
        totalOrders += orders;
        totalSales += sales;
        totalCollection += collection;
        totalDue += due;
        totalExpense += expense;

        Map<String, Object> row = new HashMap<>();
        row.put("employeeId", id);
        row.put("employeeName", e.getName());
        row.put("headquarters", e.getHeadquarters());
        row.put("attendance", present > 0 ? "Present" : "Absent");
        row.put("visits", visits);
        row.put("orders", orders);
        row.put("sales", sales);
        row.put("collection", collection);
        row.put("due", due);
        row.put("expense", expense);

        leaderboard.add(row);
    }

    leaderboard.sort((a, b) -> Double.compare(
            Double.parseDouble(b.get("sales").toString()),
            Double.parseDouble(a.get("sales").toString())
    ));

    int rank = 1;
    for (Map<String, Object> row : leaderboard) {
        row.put("rank", rank++);
    }

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
public List<Map<String, Object>> getWorkingWith(@PathVariable Long asmId) {

    List<Employee> team = getAsmTeam(asmId);
    List<Map<String, Object>> result = new ArrayList<>();

    for (Employee e : team) {
        Map<String, Object> row = new HashMap<>();

        row.put("employeeId", e.getId());
        row.put("employeeName", e.getName());
        row.put("headquarters", e.getHeadquarters());
        row.put("role", e.getRole());
        row.put("status", e.getStatus());

        result.add(row);
    }

    return result;
}

@GetMapping("/employee-details-fast/{employeeId}")
public Map<String, Object> getEmployeeDetailsFast(
        @PathVariable Long employeeId,
        @RequestParam(defaultValue = "all") String filterType,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) String month
) {
    Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    List<Attendance> attendance;
    List<DoctorVisit> visits;
    List<ProductOrder> orders;
    List<Expense> expenses;
    List<TourPlan> tourPlans = null;

    if ("date".equalsIgnoreCase(filterType) && date != null && !date.isEmpty()) {
        attendance = attendanceRepository.findByEmployeeIdAndAttendanceDateOrderByIdDesc(
                employeeId, LocalDate.parse(date));
        visits = doctorVisitRepository.findByEmployeeIdAndVisitDateOrderByVisitTimeDesc(
                employeeId, date);
        orders = orderRepository.findByEmployeeIdAndOrderDateOrderByIdDesc(
                employeeId, date);
        expenses = expenseRepository.findByEmployeeIdAndExpenseDateOrderByIdDesc(
                employeeId, date);

        // tourPlans = tourPlanRepository
        // .findByEmployeeIdAndTravelDateBetweenOrderByIdDesc(
        //         employeeId,
        //         LocalDate.parse(date),
        //         LocalDate.parse(date)
        // );

    } else if ("month".equalsIgnoreCase(filterType) && month != null && !month.isEmpty()) {
        attendance = attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByIdDesc(
                employeeId,
                LocalDate.parse(month + "-01"),
                LocalDate.parse(month + "-01").plusMonths(1).minusDays(1));
        visits = doctorVisitRepository.findByEmployeeIdAndVisitDateStartingWithOrderByIdDesc(
                employeeId, month);
        orders = orderRepository.findByEmployeeIdAndOrderDateStartingWithOrderByIdDesc(
                employeeId, month);
        expenses = expenseRepository.findByEmployeeIdAndExpenseDateStartingWithOrderByIdDesc(
                employeeId, month);
        LocalDate from = LocalDate.parse(month + "-01");
LocalDate to = from.plusMonths(1).minusDays(1);

tourPlans = tourPlanRepository
        .findByEmployeeIdAndTravelDateBetweenOrderByIdDesc(
                employeeId,
                from,
                to
        );

    } else {
        attendance = attendanceRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        visits = doctorVisitRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        orders = orderRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        expenses = expenseRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        tourPlans = tourPlanRepository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    Map<String, Object> summary = getEmployeeSummary(employeeId);

    Map<String, Object> result = new HashMap<>();
    result.put("employee", employee);
    result.put("summary", summary);
    result.put("attendance", attendance);
    result.put("visits", visits);
    result.put("orders", orders);
    result.put("expenses", expenses);
    result.put("tourPlans", tourPlans);

    return result;
}

@GetMapping("/headquarters/{asmId}")
public List<String> getAsmHeadquarters(@PathVariable Long asmId) {

    List<EmployeeHQMapping> mappings =
            mappingRepository.findByEmployeeId(asmId);

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


}