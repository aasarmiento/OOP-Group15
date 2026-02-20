package model;

import java.io.File;
import java.time.LocalDate;

/**
 * AccountingStaff Class (Concrete Child)
 * Focus: Financial Integrity, Batch Processing, and Statutory Compliance.
 */
public class AccountingStaff extends Employee implements IFinanceOperations {

    public AccountingStaff() {
        super();
    }
    
    // Added full constructor to allow object initialization from data
    public AccountingStaff(int empNo, String lastName, String firstName, LocalDate birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, double basicSalary, double riceSubsidy, double phoneAllowance, double clothingAllowance, double grossRate, double hourlyRate) {
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossRate, hourlyRate);
    }

    // --- Specialized Accounting Logic ---

    protected double viewSalaryData() {
        return this.basicSalary; 
    }

    private double computeStatutoryDeductions(double gross) {
        // Use 'gross' to verify there is pay to deduct from, resolving the "never read" error
        if (gross <= 0) {
            return 0.0;
        }
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG();
    }

    // --- IFinanceOperations Implementation ---

    @Override
    public void batchProcessPayroll() {
        System.out.println("Executing specialized batch processing...");
        this.calculateSalary();
    }

    @Override 
    public Payslip generatePayslip(String empNo, String payPeriod) { 
        return new Payslip(); 
    }

    @Override 
    public File generateTaxReport(String quarter) { 
        return null; 
    }

    @Override 
    public SummaryData generateDeductionSummary(String month) { 
        return new SummaryData(); 
    }

    // --- IPayrollCalculations Implementation ---

    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; 
    }

    @Override
    public double calculateGrossSalary() {
        return (this.hourlyRate * calculateTotalHoursWorked()) + 
               this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }

    @Override public double calculateSSSDeduction() { return 0.0; }
    @Override public double calculatePhilHealth() { return this.basicSalary * 0.05; }
    @Override public double calculatePagIBIG() { return 100.0; }
    @Override public double calculateWithholdingTax() { return 0.0; }

    @Override
    public double calculateNetPay() {
        double gross = calculateGrossSalary();
        double statutory = computeStatutoryDeductions(gross);
        double tax = calculateWithholdingTax();
        double net = gross - (statutory + tax);
        return Math.max(0, net); // Firewall: No negative pay
    }

    

    @Override
    public double computeDeductions() {
        // Reuses the internal private logic to satisfy the Employee contract
        return computeStatutoryDeductions(calculateGrossSalary()) + calculateWithholdingTax();
    }

    @Override
    public double computeNet() {
        // Redirects to the required interface method
        return calculateNetPay();
    }

    @Override
public void calculateSalary() {
    // 1. Accounting-specific logic
    System.out.println("Syncing with Financial Ledger for " + this.lastName);
    
    // 2. Run the standard payroll math defined in Parent
    super.calculateSalary(); 
    
    // 3. Post-process logic
    System.out.println("Batch record updated.");
}

    // iimplement na si IUserOperations ---

    @Override public Role getRole() { return Role.ACCOUNTING; }

 @Override 
    public boolean login(String user, String pass) { 
        // FIXED: Removed redundant 'if' statement
        return String.valueOf(this.empNo).equals(user) && isPasswordValid(pass);
    }


    // Gemini: Fixed - Updated to match the String parameter in Employee and IUserOperations
    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() > 8; 
    }

    @Override public int getPasswordStrength() { return 10; }
    @Override public void resetPassword() {}
    @Override public void logout() {}


    @Override
    public LeaveRequest applyLeave(String leaveType, LocalDate startDate, LocalDate endDate) {
        // Logic fixed: Created the object with 4 arguments to match constructor
        LeaveRequest newRequest = new LeaveRequest(this.getEmpNo(), this.getFirstName() + " " + this.getLastName(), startDate, endDate);
        
        // Added to list so 'newRequest' is read/used
        this.leaveRequests.add(newRequest);
        
        // Your logic here
        System.out.println("Accounting staff applying for " + leaveType);
        // e.g., check if the audit season is active before approving
        
        return newRequest; // Returned object to match Employee signature
    }

    @Override
    public void applyLeave(LeaveRequest request) {
        this.leaveRequests.add(request);
    }
    public AccountingStaff(int id, String last, String first, LocalDate bday, double basic) {
    super(id, last, first, bday);
    this.basicSalary = basic;
}
}