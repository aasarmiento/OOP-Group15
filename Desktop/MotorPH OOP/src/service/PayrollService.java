package service;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Attendance;
import model.AttendanceSummary;
import model.Employee;
import model.PayrollBreakdown;

public class PayrollService {
    private final EmployeeDAO employeeDao;
    private final PayrollCalculator calculator;
    private final EmployeeManagementService attendanceService;
    private final AttendanceDAO attendanceDao; // Added field to fix "cannot be resolved"

    private final LocalTime EXPECTED_IN = LocalTime.of(8, 0);
    private final LocalTime GRACE_PERIOD_CUTOFF = LocalTime.of(8, 10);
    private static final String PAYROLL_HISTORY_FILE = "resources/payroll_history.csv";

    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final LocalTime GRACE_CUTOFF = LocalTime.of(8, 10);
    private static final int REQUIRED_MINUTES_PER_DAY = 8 * 60;
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final int LUNCH_BREAK_MINUTES = 60;

    // Updated Constructor to include AttendanceDAO
    public PayrollService(EmployeeDAO dao, PayrollCalculator calc, EmployeeManagementService attService) {
        this.employeeDao = dao;
        this.calculator = calc;
        this.attendanceService = attService;
        this.attendanceDao = new AttendanceCSVHandler();
    }

    public AttendanceSummary summarizeAttendance(int empNo, String month, String year) {
        Object[][] rawLogs = attendanceService.getAttendanceLogs(empNo, month, year);
        if (rawLogs == null) {
            rawLogs = new Object[0][];
        }

        HashMap<LocalDate, LocalTime> firstLogInPerDay = new HashMap<>();
        HashMap<LocalDate, LocalTime> lastLogOutPerDay = new HashMap<>();
        Set<LocalDate> presentDates = new HashSet<>();

        for (Object[] row : rawLogs) {
            if (row == null || row.length < 3) continue;

            try {
                LocalDate date = LocalDate.parse(row[0].toString().trim(), UI_DATE_FORMAT);

                String inStr = row[1].toString().trim();
                String outStr = row[2].toString().trim();

                LocalTime timeIn = "N/A".equalsIgnoreCase(inStr) || inStr.isEmpty()
                        ? null
                        : LocalTime.parse(inStr);

                LocalTime timeOut = "N/A".equalsIgnoreCase(outStr) || outStr.isEmpty()
                        ? null
                        : LocalTime.parse(outStr);

                if (timeIn != null) {
                    presentDates.add(date);

                    LocalTime existingFirst = firstLogInPerDay.get(date);
                    if (existingFirst == null || timeIn.isBefore(existingFirst)) {
                        firstLogInPerDay.put(date, timeIn);
                    }
                }

                if (timeOut != null) {
                    LocalTime existingLast = lastLogOutPerDay.get(date);
                    if (existingLast == null || timeOut.isAfter(existingLast)) {
                        lastLogOutPerDay.put(date, timeOut);
                    }
                }

            } catch (Exception ignored) {
            }
        }

        int lateMinutes = 0;
        int undertimeMinutes = 0;
        int workedMinutes = 0;

        LocalDate today = LocalDate.now();

        for (LocalDate date : presentDates) {
            LocalTime firstIn = firstLogInPerDay.get(date);
            LocalTime lastOut = lastLogOutPerDay.get(date);

            int workedToday = calculateNetWorkedMinutes(firstIn, lastOut);
            workedMinutes += workedToday;

            if (firstIn != null && firstIn.isAfter(GRACE_CUTOFF) && !date.equals(today)) {
                lateMinutes += (int) Duration.between(GRACE_CUTOFF, firstIn).toMinutes();
            }

            if (!date.equals(today) && workedToday < REQUIRED_MINUTES_PER_DAY) {
                undertimeMinutes += (REQUIRED_MINUTES_PER_DAY - workedToday);
            }
        }

        int expectedWorkDays = countExpectedWorkDays(month, year);
        int presentDays = presentDates.size();
        int absentDays = Math.max(0, expectedWorkDays - presentDays);

        return new AttendanceSummary(
            presentDays,
            expectedWorkDays,
            absentDays,
            lateMinutes,
            undertimeMinutes,
            workedMinutes
        );
    }

    private int countExpectedWorkDays(String month, String year) {
    int y = Integer.parseInt(year);
    Month m = Month.valueOf(month.toUpperCase());

    LocalDate start = LocalDate.of(y, m, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

    LocalDate today = LocalDate.now();
    if (today.getYear() == y && today.getMonth() == m && today.isBefore(end)) {
        end = today;
    }

    int count = 0;
    LocalDate current = start;

    while (!current.isAfter(end)) {
        DayOfWeek dow = current.getDayOfWeek();
        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
            count++;
        }
        current = current.plusDays(1);
    }

    
    return count;
    
    }

    // --- NEW METHOD FOR BATCH PROCESSING (Used by PayrollFinances UI) ---
    public List<Object[]> getFullPayrollReport(String month, String year) {
    List<Object[]> reportData = new ArrayList<>();
    List<Employee> allEmployees = attendanceService.getAllEmployees();

    for (Employee emp : allEmployees) {
        AttendanceSummary attendance = summarizeAttendance(emp.getEmpNo(), month, year);
        PayrollBreakdown breakdown = calculator.calculateBreakdown(emp, attendance);

        double totalAllowances = emp.getRiceSubsidy()
                + emp.getPhoneAllowance()
                + emp.getClothingAllowance();

        reportData.add(new Object[]{
            emp.getEmpNo(),
            emp.getLastName() + ", " + emp.getFirstName(),
            emp.getPosition(),
            emp.getBasicSalary(),
            totalAllowances,
            breakdown.getGrossPay(),
            breakdown.getNetPay(),
            "Processed"
        });
    }

        return reportData;
    }



public List<Object[]> generatePayrollForPeriod(String month, String year) {
    List<Object[]> generated = new ArrayList<>();
    List<Employee> allEmployees = attendanceService.getAllEmployees();

    for (Employee emp : allEmployees) {
        AttendanceSummary attendance = summarizeAttendance(emp.getEmpNo(), month, year);
        PayrollBreakdown breakdown = calculator.calculateBreakdown(emp, attendance);

        double totalAllowances =
                emp.getRiceSubsidy() +
                emp.getPhoneAllowance() +
                emp.getClothingAllowance();

        Object[] row = new Object[]{
            month + " " + year,                             // 0 period
            emp.getEmpNo(),                                 // 1
            emp.getFirstName() + " " + emp.getLastName(),   // 2
            emp.getPosition(),                              // 3
            emp.getBasicSalary(),                           // 4
            totalAllowances,                                // 5
            breakdown.getGrossPay(),                        // 6
            breakdown.getSss(),                             // 7
            breakdown.getPhilhealth(),                      // 8
            breakdown.getPagibig(),                         // 9
            breakdown.getWithholdingTax(),                  // 10
            breakdown.getNetPay(),                          // 11
            "PAID"                                          // 12
        };

        generated.add(row);
    }

    savePayrollBatch(month, year, generated);
    return generated;
}

    // --- HELPER METHOD: Calculate hours from Attendance Logs ---
    public double getTotalHoursForPeriod(int empNo, String month, String year) {
        Object[][] logs = attendanceService.getAttendanceLogs(empNo, month, year);
        double totalHours = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    private void savePayrollBatch(String month, String year, List<Object[]> rows) {
    List<String> existingLines = new ArrayList<>();
    String targetPeriod = month + " " + year;

    File file = new File(PAYROLL_HISTORY_FILE);

    if (file.exists()) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // keep header
                if (line.startsWith("Period,")) {
                    existingLines.add(line);
                    continue;
                }

                // remove old rows for same period before replacing
                String[] parts = line.split(",");
                if (parts.length > 0 && !parts[0].trim().equalsIgnoreCase(targetPeriod)) {
                    existingLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    if (existingLines.isEmpty()) {
        existingLines.add("Period,EmpID,Name,Position,BasicSalary,Allowances,GrossIncome,SSS,PhilHealth,PagIBIG,Tax,NetPay,Status");
    }

    DecimalFormat df = new DecimalFormat("0.00");

    for (Object[] row : rows) {
        existingLines.add(
            row[0] + "," +   // Period
            row[1] + "," +   // EmpID
            row[2] + "," +   // Name
            row[3] + "," +   // Position
            df.format((double) row[4]) + "," +
            df.format((double) row[5]) + "," +
            df.format((double) row[6]) + "," +
            df.format((double) row[7]) + "," +
            df.format((double) row[8]) + "," +
            df.format((double) row[9]) + "," +
            df.format((double) row[10]) + "," +
            df.format((double) row[11]) + "," +
            row[12]
        );
    }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            for (String line : existingLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Object[]> loadPayrollBatch(String month, String year) {
    List<Object[]> rows = new ArrayList<>();
    File file = new File(PAYROLL_HISTORY_FILE);
    String targetPeriod = month + " " + year;

    if (!file.exists()) return rows;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        boolean firstLine = true;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] p = line.split(",");
            if (p.length < 13) continue;

            if (p[0].trim().equalsIgnoreCase(targetPeriod)) {
                rows.add(new Object[]{
                    Integer.parseInt(p[1].trim()),   // ID
                    p[2].trim(),                     // Name
                    p[3].trim(),                     // Position
                    Double.parseDouble(p[4].trim()), // Basic
                    Double.parseDouble(p[5].trim()), // Allowances
                    Double.parseDouble(p[6].trim()), // Gross
                    Double.parseDouble(p[11].trim()), // Net
                    p[12].trim()                     // Status
                });
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return rows;
}


    // --- BUSINESS LOGIC: Calculate Gross for a Single Employee ---
    public double getFinalGross(int empNo, String month, String year) {
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0;
        
        AttendanceSummary attendance = summarizeAttendance(empNo, month, year);
        PayrollBreakdown breakdown = calculator.calculateBreakdown(emp, attendance);
        return breakdown.getGrossPay();
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

    int netMinutes = calculateNetWorkedMinutes(attendance.getTimeIn(), attendance.getTimeOut());
    attendance.setHoursWorked(netMinutes / 60.0);
    }

    // --- FIXED HISTORICAL SUMMARY METHOD ---
public List<Object[]> getHistoricalSummary(String month, String year) {
    List<Object[]> history = new ArrayList<>();
    List<Object[]> realData = loadPayrollBatch(month, year);

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


    private int calculateNetWorkedMinutes(LocalTime timeIn, LocalTime timeOut) {
    if (timeIn == null || timeOut == null || !timeOut.isAfter(timeIn)) {
        return 0;
    }

    int grossMinutes = (int) Duration.between(timeIn, timeOut).toMinutes();

    // Deduct 1 hour lunch only if the work span covers the full lunch window
    boolean spansFullLunch = !timeIn.isAfter(LUNCH_START) && !timeOut.isBefore(LUNCH_END);
    int lunchDeduction = spansFullLunch ? LUNCH_BREAK_MINUTES : 0;

        return Math.max(0, grossMinutes - lunchDeduction);
    }
}