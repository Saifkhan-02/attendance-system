package com.gps.attendance.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.web.bind.annotation.*;

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

    public ASMController(
            EmployeeRepository employeeRepository,
            EmployeeHQMappingRepository mappingRepository,
            HeadquarterRepository headquarterRepository,
            ProductOrderRepository orderRepository,
            AttendanceRepository attendanceRepository,
            DoctorVisitRepository doctorVisitRepository,
            ExpenseRepository expenseRepository) {

        this.employeeRepository = employeeRepository;
        this.mappingRepository = mappingRepository;
        this.headquarterRepository = headquarterRepository;
        this.orderRepository = orderRepository;

        this.attendanceRepository = attendanceRepository;
        this.doctorVisitRepository = doctorVisitRepository;
        this.expenseRepository = expenseRepository;
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
    public Employee getEmployeeDetails(@PathVariable Long employeeId) {
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
public Map<String, Object> getEmployeeSummary(@PathVariable Long employeeId) {

    Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    String today = LocalDate.now().toString();
    String month = today.substring(0, 7);

    long todayVisits = doctorVisitRepository
            .countByEmployeeIdAndVisitDate(employeeId, today);

    long monthVisits = doctorVisitRepository
            .countByEmployeeIdAndVisitDateStartingWith(employeeId, month);

    long todayOrders = orderRepository.countDailyInvoices(
            employeeId,
            today
    );

    long monthOrders = orderRepository.countInvoices(
            employeeId,
            today.substring(0, 4),
            today.substring(5, 7)
    );

    Double totalSales = orderRepository.getTotalSalesByEmployee(employeeId);
    Double totalCollection = orderRepository.getTotalCollectionByEmployee(employeeId);
    Double totalDue = orderRepository.getTotalDueByEmployee(employeeId);
    Double totalExpense = expenseRepository.getTotalExpenseByEmployee(employeeId);

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

    result.put("totalSales", totalSales == null ? 0 : totalSales);
    result.put("totalCollection", totalCollection == null ? 0 : totalCollection);
    result.put("totalDue", totalDue == null ? 0 : totalDue);
    result.put("totalExpense", totalExpense == null ? 0 : totalExpense);

    return result;
}

@GetMapping("/team-summary/{asmId}")
public List<Map<String, Object>> getTeamSummary(@PathVariable Long asmId) {

    List<Employee> team = getAsmTeam(asmId);

    String today = LocalDate.now().toString();

    List<Map<String, Object>> result = new ArrayList<>();

    for (Employee employee : team) {

        Map<String, Object> row = new HashMap<>();

        Long employeeId = employee.getId();

        long todayVisits = doctorVisitRepository
                .countByEmployeeIdAndVisitDate(employeeId, today);

        Long todayOrders = orderRepository
                .countDailyInvoices(employeeId, today);

        Double sales = orderRepository
                .getTotalSalesByEmployee(employeeId);

        Double collection = orderRepository
                .getTotalCollectionByEmployee(employeeId);

        Double due = orderRepository
                .getTotalDueByEmployee(employeeId);

        Double expense = expenseRepository
                .getTotalExpenseByEmployee(employeeId);

        String attendance = "Absent";

        List<Attendance> attendanceList =
                attendanceRepository.findByEmployeeId(employeeId);

        for (Attendance a : attendanceList) {

            if (a.getAttendanceDate() != null &&
                    a.getAttendanceDate().toString().equals(today)) {

                attendance = a.getStatus();
                break;
            }
        }

        row.put("employeeId", employee.getId());
        row.put("employeeName", employee.getName());
        row.put("headquarters", employee.getHeadquarters());

        row.put("todayVisits", todayVisits);
        row.put("todayOrders", todayOrders == null ? 0 : todayOrders);

        row.put("sales", sales == null ? 0 : sales);
        row.put("collection", collection == null ? 0 : collection);
        row.put("due", due == null ? 0 : due);
        row.put("expense", expense == null ? 0 : expense);

        row.put("attendance", attendance);

        result.add(row);

    }

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

@GetMapping("/working-with/{asmId}")
public List<DoctorVisit> getAsmWorkingWith(@PathVariable Long asmId) {

    List<Employee> team = getAsmTeam(asmId);

    if (team == null || team.isEmpty()) {
        return new ArrayList<>();
    }

    List<Long> employeeIds = team.stream()
            .map(Employee::getId)
            .collect(Collectors.toList());

    return doctorVisitRepository
            .findByEmployeeIdInOrderByIdDesc(employeeIds);
}

}