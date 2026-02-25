package service;


public class PayrollCalculator {

    public static final double PAGIBIG_FIXED = 100.00;

   
    public static double getGrossIncome(double hourlyRate, double hours, double rice, double phone, double cloth) {
        return (hourlyRate * hours) + rice + phone + cloth;
    }

    
    public static double getSSSDeduction(double salary) {
       
        if (salary < 3250) return 135.00;
      
        if (salary >= 24750) return 1125.00; 

        
        double baseRate = 135.00;
        double rangeStart = 3250;
        
      
        int steps = (int)((salary - rangeStart) / 500);
        return baseRate + ((steps + 1) * 22.50);
    }
    
   
    public static double getPhilHealthDeduction(double basicSalary) {
        
        double totalContribution = basicSalary * 0.04;
        
     
        if (basicSalary <= 10000) return 200.00; 
        if (basicSalary >= 60000) return 1200.00;
        
        return totalContribution / 2;
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
    
   
    public static double getWithholdingTax(double taxableIncome, boolean isAnnual) {
        if (!isAnnual) return getWithholdingTax(taxableIncome);
        
        return getWithholdingTax(taxableIncome / 12) * 12;
    }

   
    public static double calculateTaxFromParts(double gross, double deductions) {
       
        double taxableIncome = gross - deductions;

       
        if (taxableIncome <= 20833) {
            return 0; 
        } else if (taxableIncome <= 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else {
            return (taxableIncome - 33333) * 0.25 + 2500;
        }
    }
}