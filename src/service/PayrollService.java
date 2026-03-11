package service;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.EmployeeDAO; // Added this
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.Attendance; // Added for historical summary logic
import model.Employee; // Added for historical summary logic
import model.PeriodSummary;     // Added for historical summary logic

public class PayrollService {
    private final EmployeeDAO employeeDao;
    private final PayrollCalculator calculator;
    private final EmployeeManagementService attendanceService;
    private final AttendanceDAO attendanceDao; // Added field to fix "cannot be resolved"

    private final LocalTime EXPECTED_IN = LocalTime.of(8, 0);
    private final LocalTime GRACE_PERIOD_CUTOFF = LocalTime.of(8, 10);

    // Updated Constructor to include AttendanceDAO
    public PayrollService(EmployeeDAO dao, PayrollCalculator calc, EmployeeManagementService attService) {
        this.employeeDao = dao;
        this.calculator = calc;
        this.attendanceService = attService;
        this.attendanceDao = new AttendanceCSVHandler();
    }

    // --- NEW METHOD FOR BATCH PROCESSING (Used by PayrollFinances UI) ---
public List<Object[]> getFullPayrollReport(String month, String year) {
    List<Object[]> reportData = new ArrayList<>();
    List<Employee> allEmployees = attendanceService.getAllEmployees();

    for (Employee emp : allEmployees) {
        double totalHours = getTotalHoursForPeriod(emp.getEmpNo(), month, year);
        double grossIncome = calculator.calculateGrossIncome(emp, totalHours);
        PeriodSummary summary = calculator.calculateFullSummary(emp, grossIncome);
        
        // Calculate total benefits/allowances
        double totalAllowances = emp.getRiceSubsidy() + 
                                 emp.getPhoneAllowance() + 
                                 emp.getClothingAllowance();

        reportData.add(new Object[]{
            emp.getEmpNo(),
            emp.getLastName() + ", " + emp.getFirstName(),
            "Employee", // Change to emp.getPosition() if you have that method
            emp.getBasicSalary(),     // FIXED: Fetch actual basic salary
            totalAllowances,          // FIXED: Fetch actual allowances
            summary.getGrossIncome(),
            summary.getNetPay(),
            "Processed"
        });
    }
    return reportData;
}

    // --- HELPER METHOD: Calculate hours from Attendance Logs ---
    public double getTotalHoursForPeriod(int empNo, String month, String year) {
        Object[][] logs = attendanceService.getAttendanceLogs(empNo, month, year);
        double totalHours = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy"); // Matched your CSV format

        if (logs == null) return 0;

        for (Object[] row : logs) {
            try {
                LocalDate date = LocalDate.parse(row[0].toString().trim(), dtf);
                String inStr = row[1].toString().trim();
                String outStr = row[2].toString().trim();

                LocalTime timeIn = inStr.equals("N/A") ? null : LocalTime.parse(inStr);
                LocalTime timeOut = outStr.equals("N/A") ? null : LocalTime.parse(outStr);

                Attendance record = new Attendance(empNo, date, timeIn, timeOut);
                this.processAttendance(record); 
                totalHours += record.getHoursWorked();
                
            } catch (Exception e) {
                continue; 
            }
        }
        return totalHours;
    }

    // --- BUSINESS LOGIC: Calculate Gross for a Single Employee ---
    public double getFinalGross(int empNo, String month, String year) {
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0;
        
        double totalHours = getTotalHoursForPeriod(empNo, month, year);
        return calculator.calculateGrossIncome(emp, totalHours);
    }

    // --- VALIDATION: Handle Clock-in Rules and Grace Periods ---
    public void processAttendance(Attendance attendance) {
        if (attendance.getTimeIn() == null || attendance.getTimeOut() == null) {
            attendance.setHoursWorked(0);
            return;
        }

        if (attendance.getTimeIn().isAfter(GRACE_PERIOD_CUTOFF)) {
            attendance.setLate(true); 
            long lateMins = Duration.between(EXPECTED_IN, attendance.getTimeIn()).toMinutes();
            attendance.setLateMinutes((int) lateMins);
        } else {
            attendance.setLate(false);
            attendance.setLateMinutes(0);
        }

        long totalMinutes = Duration.between(attendance.getTimeIn(), attendance.getTimeOut()).toMinutes();
        double hours = (totalMinutes - 60) / 60.0;
        attendance.setHoursWorked(Math.max(0, hours));
    }

    // --- FIXED HISTORICAL SUMMARY METHOD ---
public List<Object[]> getHistoricalSummary(String month, String year) {
    List<Object[]> history = new java.util.ArrayList<>();
    // Fetch real processed records for the selected month/year
    List<Object[]> realData = getFullPayrollReport(month, year); 
    
    for (Object[] row : realData) {
        history.add(new Object[]{
            month + " " + year, // Period
            row[0],             // Employee ID
            row[1],             // Full Name
            row[6],             // Net Pay
            row[7]              // Status
        });
    }
    return history;
}

    private boolean isPeriodClosed(String month, String year) {
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            return y < 2026 || (y == 2026 && m < 3);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}