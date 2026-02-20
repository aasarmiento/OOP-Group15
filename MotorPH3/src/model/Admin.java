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
        // This will now work perfectly because getPhilHealthDeduction is STATIC
        return PayrollCalculator.getPhilHealthDeduction(this.basicSalary);
    }

    @Override
    public double calculatePagIBIG() {
        // Static call - no 'new' needed
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

    @Override
    public void calculateSalary() {
        // Displays the final result for the Admin/Executive
        System.out.println("Processing Executive Payroll: " + this.firstName + " " + this.lastName);
        System.out.println("Net Pay: " + calculateNetPay());
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

    // Gemini: Fixed - Added (String pass) to match Parent/Interface
    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 8; 
    }
    
    @Override public int getPasswordStrength() { return 100; }
    @Override public void resetPassword() { super.resetPassword(); }

    // --- Implementation of IPayrollCalculations ---
    
    // Gemini: These are now handled by the Employee parent class using PayrollCalculator.
    // You can remove these overrides if you want them to use the standard logic, 
    // or keep them if Admin has a very specific unique calculation.

    
    
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

    // 2. ADD THIS 5-PARAMETER CONSTRUCTOR:
    public Admin(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday); // Sends 4 params to Employee.java
        this.basicSalary = basic;     // Sets the 5th param in the protected field
    }
    
}