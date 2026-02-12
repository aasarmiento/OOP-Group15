package model;

import java.util.List;
import java.util.ArrayList;

/**
 * HRStaff Class (Concrete Child)
 * Focus: Personnel Management and Leave Processing.
 */
public class HRStaff extends Employee implements IHROperations, IPayrollCalculations {
//superclass is Employee and implements the interface IHROperations & IPayrollCalculations

    // Constructors
    public HRStaff() {
        super(); 
    }

    public HRStaff(int empNo, String lastName, String firstName, java.time.LocalDate birthday, 
               String address, String phone, String sss, String philhealth, String tin, 
               String pagibig, String status, String position, String supervisor, 
               double basicSalary, double riceSubsidy, double phoneAllowance, 
               double clothingAllowance, double grossRate, double hourlyRate) {
    
    // Calls the updated Employee(int empNo, ...) constructor
    super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, 
          tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, 
          phoneAllowance, clothingAllowance, grossRate, hourlyRate);
}

    // --- iimplement na IHROperations ---
    @Override
    public void updateEmployeeDetails(int empID, Employee updatedData) {
        // Logic: Access EmployeeDatabase and overwrite specific lines
    }

    @Override
    public Employee viewEmployeeProfile(int empID) {
        return this; // In a real app, search for the object by ID
    }

    @Override
    public void approveLeaveRequest(String leaveID) {
        System.out.println("Leave " + leaveID + " approved by HR.");
    }

    @Override
    public void rejectLeaveRequest(String leaveID, String reason) { 
        System.out.println("Leave " + leaveID + " rejected. Reason: " + reason);
    }

    @Override
    public List<LeaveRequest> viewAllLeaveRequests() {
        return new ArrayList<>();
    }

    // --- Implementation of IUserOperations ---
    @Override
    public Role getRole() {
        return Role.HR_STAFF; 
    }
    //Login reads from the CSV file by passing the username and password.

    @Override public boolean login() { return true; }
    @Override public boolean isPasswordValid() { return true; }
    @Override public int getPasswordStrength() { return 80; }
    @Override public void resetPassword() { }
    @Override public void logout() { System.out.println("HR Logged out."); }

    // --- Implementation of IPayrollCalculations ---

    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; // Default monthly hours
    }

    @Override
    public double calculateGrossSalary() {
        return (this.hourlyRate * calculateTotalHoursWorked()) + 
               this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }

    @Override
    public double calculateSSSDeduction() {
        double salary = this.basicSalary; 
        if (salary < 4000) return 180.0;
        if (salary >= 29750) return 1350.0;
        return (salary * 0.045); // Example rate
    }

    @Override
    public double calculatePhilHealth() {
        double contribution = this.basicSalary * 0.05;
        return Math.min(contribution, 2500.0);
    }

    @Override
    public double calculatePagIBIG() {
        return 100.0;
    }

    @Override
    public double calculateWithholdingTax() {
        double taxableIncome = calculateGrossSalary() - 
                              (calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG());
        if (taxableIncome <= 20833) return 0.0;
        return (taxableIncome - 20833) * 0.20; // Simplified for example
    }

    @Override
    public double calculateNetPay() {
        return calculateGrossSalary() - computeDeductions();
    }

    // --- ADDED MISSING METHODS FROM EMPLOYEE PARENT (Required to clear red errors) ---

    @Override
    public double computeDeductions() {
        return calculateSSSDeduction() + calculatePhilHealth() + 
               calculatePagIBIG() + calculateWithholdingTax();
    }

    @Override
    public double computeNet() {
        return calculateNetPay();
    }

    @Override
    public void calculateSalary() {
        double net = calculateNetPay();
        System.out.println("HR Staff Net Pay: " + net);
    }
}