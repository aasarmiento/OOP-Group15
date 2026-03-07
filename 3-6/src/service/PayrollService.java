package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.util.List;
import model.Attendance;
import model.Deduction;
import model.Employee;

public class PayrollService {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final PayrollCalculator calculator;

    public PayrollService(EmployeeDAO eDao, AttendanceDAO aDao) {
        this.employeeDao = eDao;
        this.attendanceDao = aDao;
        this.calculator = new PayrollCalculator();
    }

    // The UI calls THIS method
    public double getFinalGross(int empNo) {
        Employee emp = employeeDao.findById(empNo);
        // Delegate math to the calculator
        return calculator.calculateGross(emp); 
    }

    /**
     * MASTER SERVICE METHOD: 
     * This coordinates everything for the UI.
     */
  // Inside PayrollService.java
// Inside PayrollService.java
public Deduction calculateMonthlyPayroll(Employee emp, String month) {
    Deduction d = new Deduction();
    double salary = emp.getBasicSalary();
    
    // 1. Calculate basics using the calculator's ACTUAL method names
    double allowances = calculator.calculateTotalAllowances(emp);
    double gross = calculator.calculateGross(emp);
    
    // 2. Fix the "cannot find symbol" by using the correct names:
    d.setSss(calculator.getSSSDeduction(salary));           // Changed from calculateSSS
    d.setPhilHealth(calculator.getPhilHealthDeduction(salary)); // Changed from calculatePhilHealth
    d.setPagIbig(calculator.getPagIBIGDeduction(salary));      // Changed from calculatePagIbig
    
    // 3. Calculate Tax (Service Layer Logic)
    double taxableIncome = salary - (d.getSss() + d.getPhilHealth() + d.getPagIbig());
    d.setTax(calculator.getWithholdingTax(taxableIncome));
    
    // 4. Set final totals for the UI
    d.setTotalAllowances(allowances);
    d.setGrossPay(gross);
    d.setNetPay(gross - d.getTotal());
    
    return d;
}
    /**
     * Logic for calculating the final take-home pay.
     */
    public double calculateNetSalary(int empNo, String month) {
        // Fix: Use employeeDao.findById to match your DAO method name
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0.0;

        List<Attendance> logs = attendanceDao.getAttendanceByEmployee(empNo);
        
        // 1. Calculate hours and late minutes for the specific month
        double totalHours = calculateHoursFromLogs(logs, month);
        int lateMinutes = calculateLateMinutes(logs, month);
        
        // 2. Call the new master method in PayrollCalculator
        return calculator.calculateFullNetPay(emp, totalHours, lateMinutes);
    }

    /**
     * Helper to sum up hours.
     */
    private double calculateHoursFromLogs(List<Attendance> logs, String month) {
        double total = 0;
        for (Attendance log : logs) {
            String logMonth = log.getDate().getMonth().name();
            if (month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month)) {
                total += log.getHoursWorked(); 
            }
        }
        return total;
    }

    /**
     * Helper to sum up late minutes (using 8:10 AM grace period).
     */
    private int calculateLateMinutes(List<Attendance> logs, String month) {
        int totalLate = 0;
        java.time.LocalTime gracePeriod = java.time.LocalTime.of(8, 10);
        for (Attendance log : logs) {
            String logMonth = log.getDate().getMonth().name();
            if ((month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month)) 
                 && log.getTimeIn().isAfter(gracePeriod)) {
                totalLate += (int) java.time.Duration.between(gracePeriod, log.getTimeIn()).toMinutes();
            }
        }
        return totalLate;
    }

    // These methods delegate to the Calculator to keep logic centralized
    public double calculateGrossSalary(Employee emp, double hoursWorked) {
        return calculator.calculateGrossIncome(emp, hoursWorked);
    }

   // In PayrollService.java
public double calculateGrossPay(Employee emp) {
    // Business Rule: Gross = Basic + All Allowances
    return emp.getBasicSalary() + 
           emp.getRiceSubsidy() + 
           emp.getPhoneAllowance() + 
           emp.getClothingAllowance();
}

    
}