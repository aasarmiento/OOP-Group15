package model;

/**
 * Admin Class (Concrete Child)
 * Focuses on System and User Management with numeric IDs.
 */
public class Admin extends Employee implements IAdminOperations, IPayrollCalculations {
//superclass is Employee and implements the interface IHROperations & IPayrollCalculations

        // Constructors

    public Admin() {
        super();
    }

    // --- iimplement na si  IAdminOperations ---
    
    @Override
    public void createEmployee(Employee emp) {
        // Now that empNo is an int, it will likely default to 0 if not set
        System.out.println("Admin is creating record for ID: " + emp.getEmpNo());
    }

    @Override
    public int generateNextEmpNo() {
        // Standard Auto-increment logic
        // return highestExistingID + 1;
        return 0; 
    }

    @Override
    public boolean isEmployeeValid(Employee emp) {
        // UPDATE: Primitives (int) cannot be null. 
        // We check if it's greater than 0 instead of checking for null.
        return emp != null && emp.getEmpNo() > 0;
    }

    @Override
    public void updateEmployee(int empNo) { 
        // Changed parameter from String to int
        System.out.println("Updating record for Employee #" + empNo);
    }

    @Override
    public void removeEmployee(int empNo) {
        // Changed parameter from String to int
        System.out.println("Deleting record for Employee #" + empNo);
    }

    // --- Implementation of IUserOperations ---
    
    @Override 
    public boolean login() {
        return true; 
    }

    @Override 
    public void logout() {
        System.out.println("Admin #" + this.getEmpNo() + " logged out.");
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override public boolean isPasswordValid() { return true; }
    @Override public int getPasswordStrength() { return 100; }
    @Override public void resetPassword() { }

    // --- Implementation of IPayrollCalculations ---
    
    @Override
    public void calculateSalary() {
        // Logic for Admin payroll
    }

    @Override public double calculateTotalHoursWorked() { return 160.0; }
    @Override public double calculateGrossSalary() { return 0.0; }
    @Override public double calculateSSSDeduction() { return 0.0; }
    @Override public double calculatePhilHealth() { return 0.0; }
    @Override public double calculatePagIBIG() { return 0.0; }
    @Override public double calculateWithholdingTax() { return 0.0; }
    @Override public double calculateNetPay() { return 0.0; }
    @Override public double computeDeductions() { return 0.0; }
    @Override public double computeNet() { return 0.0; }
}