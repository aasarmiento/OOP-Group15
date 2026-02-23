package service;

import dao.*; 
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import model.*;

public class EmployeeSerbisyo {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;

    public EmployeeSerbisyo() {
        this.employeeDao = new CSVHandler();
        this.attendanceDao = new AttendanceCSVHandler();
    }

    
    public List<Employee> fetchAllEmployees() {
        return employeeDao.getAll();
    }

    
    public Employee findById(int id) {
        return employeeDao.findById(id);
    }

   
    public double calculateMonthlySalary(int empNo, String targetMonthYear) {
       
        double grossIncome = computeActualSalary(empNo, targetMonthYear);
        
      
        if (grossIncome <= 0) return 0.0;

       
        return calculateNetPay(empNo, grossIncome);
    }
    
  
    public double computeActualSalary(int empNo, String targetMonthYear) {
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0.0;

        List<Attendance> records = attendanceDao.getAttendanceByEmployee(empNo);

        double totalHours = 0;
        for (Attendance record : records) {
            String monthYear = record.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) 
                               + " " + record.getDate().getYear();
            
            if (monthYear.equalsIgnoreCase(targetMonthYear)) {
                totalHours += record.getHoursWorked(); 
            }
        }
        
        return totalHours * emp.getHourlyRate();
    }

    
    public double calculateNetPay(int empNo, double grossIncome) {
        double sss = PayrollCalculator.getSSSDeduction(grossIncome);
        double ph = PayrollCalculator.getPhilHealthDeduction(grossIncome);
        double pi = PayrollCalculator.getPagIBIGDeduction();
        
        double taxable = grossIncome - (sss + ph + pi);
        double tax = PayrollCalculator.getWithholdingTax(taxable);
        
        return grossIncome - (sss + ph + pi + tax);
    }
}