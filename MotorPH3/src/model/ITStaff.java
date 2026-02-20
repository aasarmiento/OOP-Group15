package model;

import java.time.LocalDate;

import service.PayrollCalculator;

/**
 * ITStaff Class (Concrete Child)
 * Focuses on System Support and Security.
 */
public class ITStaff extends Employee implements ISystemOperations {

    // 1. CONSTRUCTORS
    // These must exist to match the CSVHandler's "new ITStaff(...)" call
    // 1. No-argument constructor (Required by some frameworks)
    public ITStaff() {
        super();
    }

    // 2. The 19-argument constructor (Matches your CSVHandler call)
    public ITStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
                   String address, String phone, String sss, String philhealth, 
                   String tin, String pagibig, String status, String position, 
                   String supervisor, double basicSalary, double riceSubsidy, 
                   double phoneAllowance, double clothingAllowance, 
                   double grossRate, double hourlyRate) {
        
        // This 'super' call must have all 19 variables in this exact order
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, 
              tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, 
              phoneAllowance, clothingAllowance, grossRate, hourlyRate);
    }

    // 2. IDENTITY OVERRIDES
    @Override
    public Role getRole() {
        return Role.ADMIN; // Change to Role.IT once your Enum is updated
    }

    @Override
    public boolean isPasswordValid(String pass) {
        // Higher security requirement for IT
        return pass != null && pass.length() >= 12; 
    }

    // 3. PAYROLL "INTERFERENCE" (The technical bonus)
    @Override
    public double calculateGrossSalary() {
        double technicalPremium = 5000.00; 
        // We use super to get the standard (Rate * Hours + Standard Allowances)
        return super.calculateGrossSalary() + technicalPremium;
    }

  
    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        LeaveRequest request = new LeaveRequest(this.getEmpNo(), type, start, end);
        System.out.println("IT Dept: Checking server maintenance schedule for these dates...");
        
        // Use the parent's storage logic
        super.applyLeave(request); 
        return request;
    }

    // 5. ISystemOperations Implementation
    @Override
    public boolean resetCredentials(String empID, String adminToken) {
        if ("MASTER_TOKEN_2026".equals(adminToken)) {
            System.out.println("IT Security: Resetting credentials for " + empID);
            return true;
        }
        return false;
    }

    @Override
    public String checkSystemHealth() {
        return "System Status: Online - All Services Operational";
    }

    // --- IMPLEMENTING MISSING IPayrollCalculations METHODS ---

    @Override 
    public double calculateTotalHoursWorked() { 
        // Logic: For now, return a standard monthly average or link to attendance
        return 160.0; 
    }

    @Override
    public void calculateSalary() {
        // Trigger the sequence of calculations defined in the Parent/Interface
        this.calculateGrossSalary();
        this.computeDeductions();
        this.computeNet();
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
        // logic for BIR tax table
        return 0.0; 
    }

    @Override 
    public double computeDeductions() { 
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG() + calculateWithholdingTax(); 
    }

    @Override 
    public double computeNet() { 
        return calculateGrossSalary() - computeDeductions(); 
    }

    @Override 
    public double calculateNetPay() { 
        return computeNet(); 
    }

    public ITStaff(int id, String last, String first, LocalDate bday, double basic) {
    super(id, last, first, bday);
    this.basicSalary = basic;
}
}