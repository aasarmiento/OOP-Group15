package service;

import dao.CSVHandler;
import java.util.ArrayList;
import java.util.List;
import model.Employee;

public class PayrollSystem {
    private final  CSVHandler handler = new CSVHandler();

    // Helper method to refresh data (Centralizing the data load)
    private List<Employee> getLatestData() {
        return handler.getAll();
    }

    public Employee getCalculatedEmployee(String empId, String requesterRole) {
        if (!requesterRole.equalsIgnoreCase("Accounting") && !requesterRole.equalsIgnoreCase("Admin")) {
            System.err.println("Access Denied: Unauthorized payroll access.");
            return null;
        }

        try {
            int idToFind = Integer.parseInt(empId); 
            List<Employee> allEmployees = getLatestData(); 

            for (Employee emp : allEmployees) {
                if (emp.getEmpNo() == idToFind) {
                    // CRC Logic: Active Manager calculates its own salary
                    emp.calculateSalary(); 
                    return emp; 
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Employee ID must be a number.");
        }
        return null; 
    }

    /**
     * CEO/Admin Logic: Get Direct Reports
     * Used by Manuel to see Lim, Aquino, and Reyes
     */
    public List<Employee> getDirectReports(String supervisorName) {
        List<Employee> reports = new ArrayList<>();
        List<Employee> allEmployees = getLatestData(); 

        for (Employee emp : allEmployees) {
            // Null-safe comparison to prevent crashes on "N/A" supervisors
            if (emp.getSupervisor() != null && emp.getSupervisor().equalsIgnoreCase(supervisorName)) {
                reports.add(emp);
            }
        }
        return reports;
    }

    /**
     * Global Oversight: Calculate Total Company Liability
     */
    public double getTotalCompanyPayroll() {
        double total = 0;
        for (Employee emp : getLatestData()) {
            // Polymorphism: different roles might have different calculation logic
            total += emp.getGrossSemiMonthlyRate();
        }
        return total;
    }
}