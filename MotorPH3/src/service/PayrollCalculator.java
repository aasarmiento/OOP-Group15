package service;

/**
 * PayrollCalculator Utility
 * Centralized Government Tables and Logic for MotorPH.
 */
public class PayrollCalculator {

    public static final double RICE_SUBSIDY = 1500.0;
    public static final double PAGIBIG_FIXED = 100.00;
    
    public static double getGrossIncome(double hourlyRate, double hours, double phone, double cloth) {
        return (hourlyRate * hours) + RICE_SUBSIDY + phone + cloth;
    }

    public static double getSSSDeduction(double salary) {
        if (salary < 3250) return 135.00;
        if (salary >= 24750) return 1125.00; 
        
        double[][] sssTable = {
            {3250, 3750, 157.50}, {3750, 4250, 180.00}, {4250, 4750, 202.50},
            {4750, 5250, 225.00}, {5250, 5750, 247.50}, {5750, 6250, 270.00}
        };

        for (double[] range : sssTable) {
            if (salary >= range[0] && salary < range[1]) return range[2];
        }
        return 135.00; 
    }
    
    public static double getPhilHealthDeduction(double basicSalary) {
        return (basicSalary * 0.04) / 2;
    }

    public static double getPagIBIGDeduction() {
        return PAGIBIG_FIXED;
    }

    public static double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0.0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return ((taxableIncome - 33333) * 0.25) + 2500.00;
        if (taxableIncome <= 166667) return ((taxableIncome - 66667) * 0.30) + 10833.33;
        if (taxableIncome <= 666667) return ((taxableIncome - 166667) * 0.32) + 40833.33;
        return ((taxableIncome - 666667) * 0.35) + 200833.33;
    }
}