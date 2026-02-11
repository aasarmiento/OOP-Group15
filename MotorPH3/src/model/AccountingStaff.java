package model;


import java.io.File;

public class AccountingStaff extends Employee implements IFinanceOperations, IPayrollCalculations {

    public AccountingStaff() {
        super();
    }

    // --- Dictionary: viewSalaryData ---
    // Inherits protected access to money fields from the parent Employee.
    protected double viewSalaryData() {
        return this.basicSalary; 
    }

    // --- Dictionary: computeStatutoryDeductions ---
    /**
     * Aggregates SSS, PhilHealth, and Pag-IBIG for the payslip.
     * Security: Keeps the math internal to the Finance role.
     */
    private double computeStatutoryDeductions(double gross) {
        // In your dictionary, you specified this takes 'double gross' as a parameter.
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG();
    }

    // --- Dictionary: generateNetPay ---
    @Override
    public double calculateNetPay() {
        double gross = calculateGrossSalary();
        double statutory = computeStatutoryDeductions(gross);
        double tax = calculateWithholdingTax();
        
        double net = gross - (statutory + tax);
        
        // Firewall: Logic ensures net pay is never negative (Net > 0).
        return Math.max(0, net);
    }

    // --- Dictionary: batchProcessPayroll ---
    @Override
    public void batchProcessPayroll() {
        // Triggers the specialized payroll logic for the current pay cycle.
        System.out.println("Executing specialized batch processing...");
        this.calculateSalary();
    }

    // --- Fulfilling Remaining Interface Contracts ---
    
    @Override
    public Role getRole() {
    return Role.ACCOUNTING; // Match the ALL_CAPS name in your Role enum

    }

    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; // Placeholder for LogHandler
    }

    @Override
    public double calculateGrossSalary() {
        // Inheritance: No need to pass Employee; uses its own inherited data.
        return (this.hourlyRate * calculateTotalHoursWorked()) + 
               this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }

    // Statutory Methods (Placeholder for your specific CP2 Logic)
    @Override public double calculateSSSDeduction() { return 0.0; }
    @Override public double calculatePhilHealth() { return this.basicSalary * 0.05; }
    @Override public double calculatePagIBIG() { return 100.0; }
    @Override public double calculateWithholdingTax() { return 0.0; }

    @Override
    public void calculateSalary() {
        double net = calculateNetPay();
        System.out.println("Net Pay for " + this.firstName + ": " + net);
    }

    // IUserOperations placeholders
    @Override public boolean login() { return true; }
    @Override public boolean isPasswordValid() { return true; }
    @Override public int getPasswordStrength() { return 10; }
    @Override public void resetPassword() {}
    @Override public void logout() {}
    @Override public Payslip generatePayslip(String empNo, String payPeriod) { return new Payslip(); }
    @Override public File generateTaxReport(String quarter) { return null; }
    @Override public SummaryData generateDeductionSummary(String month) { return new SummaryData(); }
}