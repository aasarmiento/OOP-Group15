package service;

import dao.EmployeeDAO; 
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;
import model.Employee;
import model.PayrollBreakdown;

public class PayrollService {
    private final EmployeeDAO employeeDao;
    private final EmployeeManagementService attendanceService; 
    private final DolePolicy dolePolicy; 

    private final LocalTime EXPECTED_IN = LocalTime.of(8, 0);
    private final LocalTime GRACE_PERIOD_CUTOFF = LocalTime.of(8, 10);

    public PayrollService(EmployeeDAO dao, PayrollCalculator calc, EmployeeManagementService attService) {
        this.employeeDao = dao;
        this.attendanceService = attService;
        this.dolePolicy = new DolePolicy(); 
    }

    /**
     * ito ung nagbribridge na kaninng lapses natin: Updated to calculate late minutes before computing payroll.
     */
    public PayrollBreakdown calculateFullPayroll(int empId, String month, String year) {
        Employee emp = employeeDao.findById(empId);
        if (emp == null) return null;

        // We need both hours and late minutes for a correct MotorPH calculation
        AttendanceSummary summary = getAttendanceSummary(empId, month, year);
        
        // Calculate late deduction amount here to pass to DolePolicy
        double lateDeduction = (emp.getHourlyRate() / 60.0) * summary.totalLateMinutes;
        
        return dolePolicy.compute(emp, summary.totalHours, lateDeduction);
    }

   
    public List<Object[]> getFullPayrollReport(String month, String year) {
        List<Object[]> reportData = new ArrayList<>();
        List<Employee> allEmployees = attendanceService.getAllEmployees();

        for (Employee emp : allEmployees) {
            AttendanceSummary summary = this.getAttendanceSummary(emp.getEmpNo(), month, year);
            
            
            if (summary.totalHours <= 0) {
                continue; 
            }
            
            double lateDeduction = (emp.getHourlyRate() / 60.0) * summary.totalLateMinutes;
            
            PayrollBreakdown breakdown = dolePolicy.compute(emp, summary.totalHours, lateDeduction);

            reportData.add(new Object[]{
                emp.getEmpNo(),
                emp.getLastName() + ", " + emp.getFirstName(),
                emp.getPosition(), 
                emp.getBasicSalary(),     
                breakdown.getTotalAllowances(),          
                breakdown.getGrossPay(), 
                breakdown.getNetPay(),   
                "Processed"              
            });
        }
        return reportData;
    }

   
    private static class AttendanceSummary {
        double totalHours = 0;
        int totalLateMinutes = 0;
    }

    public AttendanceSummary getAttendanceSummary(int empNo, String month, String year) {
        Object[][] logs = attendanceService.getAttendanceLogs(empNo, month, year);
        AttendanceSummary summary = new AttendanceSummary();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 
        
        if (logs == null) return summary;

        for (Object[] row : logs) {
            try {
                LocalDate date = LocalDate.parse(row[0].toString().trim(), dtf);
                String inStr = row[1].toString().trim();
                String outStr = row[2].toString().trim();

                LocalTime timeIn = (inStr.equals("N/A") || inStr.isEmpty()) ? null : LocalTime.parse(inStr);
                LocalTime timeOut = (outStr.equals("N/A") || outStr.isEmpty()) ? null : LocalTime.parse(outStr);

                Attendance record = new Attendance(empNo, date, timeIn, timeOut);
                this.processAttendance(record); 
                
                summary.totalHours += record.getHoursWorked();
                summary.totalLateMinutes += record.getLateMinutes();
                
            } catch (Exception e) {
                continue; 
            }
        }
        return summary;
    }

    public double getTotalHoursForPeriod(int empNo, String month, String year) {
        return getAttendanceSummary(empNo, month, year).totalHours;
    }

    public void processAttendance(Attendance attendance) {
        if (attendance.getTimeIn() == null || attendance.getTimeOut() == null) {
            attendance.setHoursWorked(0);
            attendance.setLateMinutes(0);
            return;
        }

        // MotorPH Requirement: If after 8:10, calculate late from 8:00
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

    public List<Object[]> getHistoricalSummary(String month, String year) {
    List<Object[]> summary = new java.util.ArrayList<>();
    

    
    return summary;
}
}