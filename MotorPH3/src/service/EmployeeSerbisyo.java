package service;

import dao.EmployeeDAO;
import dao.CSVHandler;
import dao.AttendanceDAO;           // Added
import dao.AttendanceCSVHandler;    // Added
import model.Employee;
import model.Attendance;           // Added
import java.util.List;
import java.time.format.TextStyle;
import java.util.Locale;

public class EmployeeSerbisyo {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;

    public EmployeeSerbisyo() {
        // The Service Layer initializes the specific implementations
        this.employeeDao = new CSVHandler();
        this.attendanceDao = new AttendanceCSVHandler();
    }

    // --- Employee Logic ---

    public List<Employee> fetchAllEmployees() {
        return employeeDao.getAll();
    }

    public Employee findById(int id) {
        return employeeDao.getAll().stream()
                .filter(e -> e.getEmpNo() == id)
                .findFirst()
                .orElse(null);
    }

    // --- Payroll Logic ---

    public double calculateMonthlyNetPay(Employee emp) {
        double gross = emp.getBasicSalary() + emp.getRiceSubsidy() + 
                       emp.getPhoneAllowance() + emp.getClothingAllowance();
        
        double deductions = emp.calculateSSS() + 
                            emp.calculatePhilHealth() + 
                            emp.calculatePagIBIG();
        
        return gross - deductions;
    }

    /**
     * Calculates salary based on actual attendance hours from CSV.
     * @param targetMonthYear Format: "JANUARY 2022"
     */
    public double calculateMonthlySalary(int empNo, String targetMonthYear) {
        List<Attendance> records = attendanceDao.getAttendanceByEmployee(empNo);
        Employee emp = findById(empNo);

        if (emp == null) return 0.0;

        double totalHours = 0;
        for (Attendance record : records) {
            // Match format with CP2 logic: "MONTH YEAR"
            String monthYear = record.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) 
                               + " " + record.getDate().getYear();
            
            if (monthYear.equalsIgnoreCase(targetMonthYear)) {
                totalHours += record.getHoursWorked();
            }
        }
        
        return totalHours * emp.getHourlyRate();
    }
}