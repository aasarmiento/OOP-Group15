package model;

import java.io.File;

/**
 * AccountingStaff Class (Concrete Child)
 * Focus: Financial Integrity, Batch Processing, and Statutory Compliance.
 */
public class AccountingStaff extends Employee implements IFinanceOperations, IPayrollCalculations {

    public AccountingStaff() {
        super();
    }

    // --- Specialized Accounting Logic ---

    protected double viewSalaryData() {
        return this.basicSalary; 
    }

    private double computeStatutoryDeductions(double gross) {
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
        double net = computeNet();
        System.out.println("Payroll processed for: " + this.firstName + " " + this.lastName);
        System.out.println("Final Net: " + net);
    }

    // iimplement na si IUserOperations ---

    @Override public Role getRole() { return Role.ACCOUNTING; }
    @Override public boolean login() { return true; }
    @Override public boolean isPasswordValid() { return true; }
    @Override public int getPasswordStrength() { return 10; }
    @Override public void resetPassword() {}
    @Override public void logout() {}
}