package service;

import model.*; 
import java.util.ArrayList;
import java.util.List;

import dao.CSVHandler;

/**
 * PayrollSystem (Controller)
 * Utilizing Runtime Polymorphism to process all staff types.
 */
public class PayrollSystem {
    
    // Using the Parent type 'Employee' allows this list to hold any subclass
    private final List<Employee> masterEmployeeList = new ArrayList<>();

    public void addEmployee(Employee emp) {
        if (emp != null) {
            this.masterEmployeeList.add(emp);
        }
    }

    /**
     * Executes the system-wide payroll.
     * Polymorphism ensures each subclass uses its specific rules.
     */
    public void processFullPayroll() {
        if (masterEmployeeList.isEmpty()) {
            System.err.println("ALERT: No employee records found in the system.");
            return;
        }

        System.out.println("===============================================");
        System.out.println("       MOTORPH SYSTEM-WIDE PAYROLL RUN        ");
        System.out.println("===============================================");
        
        for (Employee emp : masterEmployeeList) {
            try {
                // Runtime Polymorphism in action: 
                // Even though 'emp' is declared as Employee, Java calls the 
                // specific calculateSalary() of ITStaff, HRStaff, or Admin.
                emp.calculateSalary(); 
                
                System.out.println("Employee ID : " + emp.getEmpNo());
                System.out.println("Name        : " + emp.getLastName() + ", " + emp.getFirstName());
                System.out.println("Position    : " + emp.getPosition());
                System.out.println("Status      : " + emp.getStatus());
                System.out.println("Role Access : " + emp.getRole());
                
                // Formatted output for currency
                System.out.printf("Gross Pay   : PHP %,.2f%n", emp.calculateGrossSalary());
                System.out.printf("Deductions  : PHP %,.2f%n", emp.computeDeductions());
                System.out.printf("Net Pay     : PHP %,.2f%n", emp.calculateNetPay());
                System.out.println("-----------------------------------------------");
                
            } catch (Exception e) {
                // Prevents one broken employee record from stopping the whole payroll run
                System.err.println("ERROR: Could not process payroll for ID " + emp.getEmpNo());
                // e.printStackTrace(); // Uncomment only for debugging broken CSV data
            }
        }
        System.out.println("Payroll Run Complete.");
    }

    /**
     * TEST MAIN METHOD
     */
    public static void main(String[] args) {
        PayrollSystem motorPH = new PayrollSystem();

        // 1. Admin 
        motorPH.addEmployee(new Admin(10001, "Garcia", "Manuel III", null, "Makati", "966-860-270", "SSS#", "PH#", "TIN#", "PI#", "Regular", "CEO", "N/A", 90000, 1500, 2000, 1000, 45000, 535.71));
        
        // 2. ITStaff
        motorPH.addEmployee(new ITStaff(10005, "Hernandez", "Eduard", null, "Misamis", "088-861-012", "SSS#", "PH#", "TIN#", "PI#", "Regular", "IT", "Lim", 52670, 1500, 1000, 1000, 26335, 313.51));
        
        // 3. HRStaff
        motorPH.addEmployee(new HRStaff(10006, "Villanueva", "Andrea Mae", null, "Las Pinas", "918-621-603", "SSS#", "PH#", "TIN#", "PI#", "Regular", "HR Manager", "Lim", 52670, 1500, 1000, 1000, 26335, 313.51));

        motorPH.processFullPayroll();
    }

    // Inside your PayrollFrame or Service class
CSVHandler handler = new CSVHandler();
List<Employee> allEmployees = handler.getAll();

for (Employee emp : allEmployees) {
    System.out.println("Loaded: " + emp.getFirstName() + " " + emp.getLastName());
    // You can now add these to your JTable model!
}
}