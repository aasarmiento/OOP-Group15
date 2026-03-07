package service;

import model.Deduction;
import model.Employee;

public class PayrollCalculator {



    // Standard Gross Calculation (Monthly)
    public double calculateGross(Employee emp) {
        if (emp == null) return 0.0;
        return emp.getBasicSalary() + 
               emp.getRiceSubsidy() + 
               emp.getPhoneAllowance() + 
               emp.getClothingAllowance();
    }



    // Overloaded for Hourly/Attendance based payroll
    public double calculateGrossFromHours(Employee emp, double hoursWorked) {
        double basicPay = emp.getHourlyRate() * hoursWorked;
        double allowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        return basicPay + allowances;
    }
    // FIXED: Added this version because Employee.java calls it with NO arguments
    public double getPagIBIGDeduction() {
        // Since no salary is passed, we return the standard 100.00 cap for MotorPH
        return 100.00; 
    }

    // Overloaded version if you ever want to pass a specific salary
    public double getPagIBIGDeduction(double monthlySalary) {
        double contribution = (monthlySalary > 1500) ? monthlySalary * 0.02 : monthlySalary * 0.01;
        return Math.min(contribution, 100.00); 
    }

    // FIXED: Renamed to match the "getWithholdingTax" call in your Employee class
    public double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0.0;
        if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome < 66667) return 2500.00 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome < 166667) return 10833.33 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
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

   // Inside service.PayrollCalculator
public Deduction calculateAllDeductions(Employee emp, int lateMinutes) {
    double salary = emp.getBasicSalary();
    double sss = getSSSDeduction(salary);
    double ph = getPhilHealthDeduction(salary);
    double pagibig = getPagIBIGDeduction(salary); 

    double hourlyRate = salary / 22 / 8;
    double lateAmount = (hourlyRate / 60) * lateMinutes;

    double totalGov = sss + ph + pagibig;
    double taxableIncome = (salary - lateAmount) - totalGov;
    double tax = getWithholdingTax(taxableIncome);
    
    // Pass lateMinutes as the 6th argument to match the new constructor
    return new Deduction(sss, ph, pagibig, tax, lateAmount, lateMinutes);
}

    /**
     * BUSINESS RULE: Gross Income Calculation.
     * Formula: (Hourly Rate * Hours Worked) + All Monthly Allowances
     */
    public double calculateGrossIncome(Employee emp, double hoursWorked) {
        if (emp == null) return 0.0;
        
        // Basic Pay based on actual hours worked
        double basicPay = emp.getHourlyRate() * hoursWorked;
        
        // Sum of all fixed monthly allowances
        double allowances = emp.getRiceSubsidy() + 
                           emp.getPhoneAllowance() + 
                           emp.getClothingAllowance();
                           
        return basicPay + allowances;
    }

    // Add this inside PayrollCalculator class
public double calculateFullNetPay(Employee emp, double hoursWorked, int lateMinutes) {
    // 1. Calculate Gross from hours
    double grossFromHours = emp.getHourlyRate() * hoursWorked;
    double allowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
    double totalGross = grossFromHours + allowances;

    // 2. Get all deductions (passing late minutes)
    Deduction d = calculateAllDeductions(emp, lateMinutes);
    
    // 3. Final Formula
    // Net = (Gross + Allowances) - (SSS + PhilHealth + PagIbig + Tax + LateDeduction)
    return totalGross - d.getTotal(); 
}

// Inside PayrollCalculator.java (Service Layer)
public double calculateTotalAllowances(Employee emp) {
    return emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
}

public double calculateNetPay(double gross, model.Deduction d) {
    // Net = Gross - (SSS + PhilHealth + PagIbig + Tax + Late)
    return gross - d.getTotal();
}



}