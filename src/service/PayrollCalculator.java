package service;

import java.time.format.DateTimeFormatter;
import model.Employee;
import model.PeriodSummary;

/**
 * SERVICE LAYER: Business Logic & Calculations
 * This class contains the rules for Gross Income, SSS, PhilHealth, Pag-IBIG, and Tax.
 */
public class PayrollCalculator {
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

    /**
     * MAIN METHOD: Bridges the processed Gross Income to the final Deductions.
     * This creates the PeriodSummary model used by the MyPayslip UI.
     */
    /**
     * MAIN METHOD: Bridges the processed Gross Income to the final Deductions.
     * This creates the PeriodSummary model used by the MyPayslip UI.
     */
    public PeriodSummary calculateFullSummary(Employee emp, double grossIncome) {
        if (emp == null) {
            // Corrected: Providing 6 zeros to match the 6 parameters in PeriodSummary
            return new PeriodSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        // 1. Calculate statutory deductions based on Basic Salary
        double monthlySalary = emp.getBasicSalary();
        double sss = getSSSDeduction(monthlySalary);
        double ph = getPhilHealthDeduction(monthlySalary);
        double pagibig = getPagIBIGDeduction(monthlySalary);

        // 2. Tax Calculation (Taxable Income = Gross - Gov Deductions)
        double totalGov = sss + ph + pagibig;
        double taxableIncome = grossIncome - totalGov;
        double tax = getWithholdingTax(taxableIncome);

        // 3. Net Pay Calculation
        double net = grossIncome - (totalGov + tax);

        // 4. THE FIX: Match the constructor (gross, sss, ph, pagibig, tax, net)
        // Removed the extra leading '0' that was causing the error
        return new PeriodSummary(grossIncome, sss, ph, pagibig, tax, net);
    }

    /**
     * Business Logic: Gross = (Hourly * Hours) + Allowances
     */
    public double calculateGrossIncome(Employee emp, double hoursWorked) {
        if (emp == null) return 0.0;
        double basicPay = emp.getHourlyRate() * hoursWorked;
        double allowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        return basicPay + allowances;
    }

    // --- Statutory Formula Methods (Business Rules) ---

    public double getPagIBIGDeduction(double monthlySalary) {
        double contribution = (monthlySalary > 1500) ? monthlySalary * 0.02 : monthlySalary * 0.01;
        return Math.min(contribution, 100.00); 
    }

    public double getSSSDeduction(double monthlySalary) {
        if (monthlySalary < 3250) return 135.00;
        if (monthlySalary >= 24750) return 1125.00; 
        double excess = monthlySalary - 3250;
        int steps = (int) (excess / 500);
        return 135.00 + (steps * 22.50);
    }

    public double getPhilHealthDeduction(double monthlySalary) {
        double totalPremium;
        if (monthlySalary <= 10000) totalPremium = 300.00;
        else if (monthlySalary >= 60000) totalPremium = 1800.00;
        else totalPremium = monthlySalary * 0.03;
        return totalPremium / 2; 
    }

    public double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0.0;
        if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome < 66667) return 2500.00 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome < 166667) return 10833.33 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
}