package model;

import java.time.LocalDate;
import service.PayrollCalculator;



public class Admin extends Employee implements IAdminOperations, ILeaveOperationsSuperior {
    //superclass is Employee and implements the interface IHROperations & IPayrollCalculations

    // Constructors
    public Admin() {
        super();
    }

    // Full constructor to pass data to the Employee parent
    public Admin(int empNo, String lastName, String firstName, LocalDate birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, double basicSalary, double riceSubsidy, double phoneAllowance, double clothingAllowance, double grossRate, double hourlyRate) {
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossRate, hourlyRate);
    }
    
// --- Implementation of IPayrollCalculations ---
    
    @Override 
    public double calculateTotalHoursWorked() { 
        return 160.0; // Standard monthly hours
    }

    @Override
    public double calculateSSSDeduction() {
        return PayrollCalculator.getSSSDeduction(this.basicSalary);
    }

   @Override
    public double calculatePhilHealth() {
       
        return PayrollCalculator.getPhilHealthDeduction(this.basicSalary);
    }

    @Override
    public double calculatePagIBIG() {
       
        return PayrollCalculator.getPagIBIGDeduction();
    }

    @Override
    public double calculateWithholdingTax() {
        double taxableIncome = calculateGrossSalary() - computeDeductions();
        return PayrollCalculator.getWithholdingTax(taxableIncome);
    }

    @Override
    public double calculateGrossSalary() {
        // (Hourly Rate * Hours) + Rice + Phone + Clothing
        return (this.hourlyRate * calculateTotalHoursWorked()) + 
               this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }

    @Override
    public double calculateNetPay() {
        return calculateGrossSalary() - (computeDeductions() + calculateWithholdingTax());
    }

   /**
     * THE SYSTEM ENTRY POINT (The Waiter)
     * Stays consistent across all classes for the UI to call.
     */
    @Override
    public double calculateSalary() {
        // We can add a "Log" here in the parent if needed later
        return calculateSahod(); 
    }

    
    @Override
    public double calculateSahod() {
        double managementAllowance = 5000.0;
        
       
        return calculateNetPay() + managementAllowance;
    }

    // --- iimplement na si  IAdminOperations ---
    
    @Override
    public void createEmployee(Employee emp) {
       
        System.out.println("Admin is creating record for ID: " + emp.getEmpNo());
    }

    @Override
    public int generateNextEmpNo() {
      
        return 0; 
    }

    @Override
    public boolean isEmployeeValid(Employee emp) {
        return emp != null && emp.getEmpNo() > 0;
    }

    @Override
    public void updateEmployee(int empNo) { 
        System.out.println("Updating record for Employee #" + empNo);
    }

    @Override
    public void removeEmployee(int empNo) {
        System.out.println("Deleting record for Employee #" + empNo);
    }

    // --- Implementation of IUserOperations ---
    
    
    @Override 
    public boolean login(String user, String pass) {
        return super.login(user, pass); 
    }

    @Override 
    public void logout() {
        System.out.println("Admin #" + this.getEmpNo() + " logged out.");
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

   
    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 8; 
    }
    
    @Override public int getPasswordStrength() { return 100; }
    @Override public void resetPassword() { super.resetPassword(); }

    // --- Implementation of IPayrollCalculations ---
    
   

    
    
    // --- Implementation of ILeaveOperations ---

    // Implementation of the abstract method from Employee
   @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        LeaveRequest request = new LeaveRequest(this.empNo, type, start, end);
        System.out.println("Admin Note: Executive leave auto-logged.");
        super.applyLeave(request);
        return request;
    }


    @Override
    public void applyLeave(LeaveRequest request) {
        this.leaveRequests.add(request);
        System.out.println("The Admin Staff applying for leave: " + request.getRequestId());
    }

    // --- Implementation of ILeaveOperationsSuperior ---

    @Override
    public void updateLeaveStatus(LeaveRequest request, String newStatus) {
        // Admin OVERRIDES the blank parent method with ACTUAL power.
        request.setStatus(newStatus);
        request.setApprovedBy(String.valueOf(this.getEmpNo()));
        request.setApprovalDate(LocalDate.now());
        
        System.out.println("Admin AUTHORITY: Request " + request.getRequestId() + " updated to " + newStatus);
    }

   
    public Admin(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday); // Sends 4 params to Employee.java
        this.basicSalary = basic;     // Sets the 5th param in the protected field
    }
    
}