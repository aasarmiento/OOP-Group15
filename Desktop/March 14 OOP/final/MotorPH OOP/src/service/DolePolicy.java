package service;

import model.Employee;
import model.PayrollBreakdown;

public class DolePolicy {

  
    public PayrollBreakdown compute(Employee emp, double hoursWorked) {
        return compute(emp, hoursWorked, 0.0);
    }

    
    public PayrollBreakdown compute(Employee emp, double hoursWorked, double lateDeduction) {
        double basicSalary = emp.getBasicSalary();
        double sss = getSSSDeduction(basicSalary);
        double ph = getPhilHealthDeduction(basicSalary);
        double pi = getPagIBIGDeduction(basicSalary);
        
        double allowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        
        double grossPay = (basicSalary + allowances) - lateDeduction;
        
        double totalStatutory = sss + ph + pi;
        double taxableIncome = Math.max(0, grossPay - totalStatutory);
        
        double tax = getWithholdingTax(taxableIncome);
        double netPay = grossPay - (totalStatutory + tax);

        return new PayrollBreakdown(
            hoursWorked, basicSalary, allowances, 0, 0, 
            grossPay, taxableIncome, sss, ph, pi, tax, netPay
        );
    }

   
    public static double getSSSDeduction(double basicSalary) {
        if (basicSalary >= 24750) return 1125.00;
        if (basicSalary < 3250) return 135.00;
        
        double factor = Math.floor((basicSalary - 3250) / 500);
        return 157.50 + (factor * 22.50);
    }

    
    public static double getPhilHealthDeduction(double basicSalary) {
        double salaryCap = Math.min(Math.max(basicSalary, 10000.0), 60000.0);
        double totalPremium = salaryCap * 0.03;
        return totalPremium / 2.0; 
    }

   
    public static double getPagIBIGDeduction(double basicSalary) {
        return (basicSalary > 1500) ? 100.00 : 0.0;
    }

    
    public static double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0.0;
        if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
}