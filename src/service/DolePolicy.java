package service;

import model.Employee;
import model.PayrollBreakdown;

public class DolePolicy implements PayrollPolicy {

    // Keeping your earlier 2025 setup:
    // Rice subsidy non-taxable up to 2,000/month
    // Clothing allowance non-taxable up to 7,000/year = 583.33/month
    // Phone allowance remains taxable by default
    private static final double RICE_NON_TAXABLE_CAP = 2000.00;
    private static final double CLOTHING_MONTHLY_NON_TAXABLE_CAP = 7000.00 / 12.0;

    @Override
    public PayrollBreakdown compute(Employee employee, double hoursWorked) {
        if (employee == null) {
            return zero();
        }

        double safeHours = Math.max(0.0, hoursWorked);

        // FIXED MONTHLY SALARY:
        // basic pay should come from monthly salary, not hourly rate x hours
        double earnedBasicPay = round2(nvl(employee.getBasicSalary()));

        double rice = nvl(employee.getRiceSubsidy());
        double phone = nvl(employee.getPhoneAllowance());
        double clothing = nvl(employee.getClothingAllowance());

        double totalAllowances = round2(rice + phone + clothing);

        // Split allowances into non-taxable and taxable parts
        double nonTaxableRice = Math.min(rice, RICE_NON_TAXABLE_CAP);
        double nonTaxableClothing = Math.min(clothing, CLOTHING_MONTHLY_NON_TAXABLE_CAP);

        double taxableRice = round2(rice - nonTaxableRice);
        double taxableClothing = round2(clothing - nonTaxableClothing);
        double taxablePhone = phone; // taxable by default

        double nonTaxableAllowances = round2(nonTaxableRice + nonTaxableClothing);
        double taxableAllowances = round2(taxableRice + taxableClothing + taxablePhone);

        // Gross includes monthly salary + all allowances
        double grossPay = round2(earnedBasicPay + totalAllowances);

        // Contributions use monthly basic salary
        double contributionBase = Math.max(0.0, nvl(employee.getBasicSalary()));

        double sss = getSSSDeduction(contributionBase);
        double philhealth = getPhilHealthDeduction(contributionBase);
        double pagibig = getPagIBIGDeduction(contributionBase);

        // Taxable income excludes non-taxable rice/clothing portions
        double taxableIncome = round2(
                Math.max(0.0, earnedBasicPay + taxableAllowances - (sss + philhealth + pagibig))
        );

        double withholdingTax = getWithholdingTax(taxableIncome);
        double netPay = round2(grossPay - (sss + philhealth + pagibig + withholdingTax));

        return new PayrollBreakdown(
                safeHours,
                earnedBasicPay,
                totalAllowances,
                taxableAllowances,
                nonTaxableAllowances,
                grossPay,
                taxableIncome,
                sss,
                philhealth,
                pagibig,
                withholdingTax,
                netPay
        );
    }

    public static double getSSSDeduction(double monthlySalaryBase) {
        if (monthlySalaryBase <= 0) return 0.0;

        double msc = getSssMsc(monthlySalaryBase);
        return round2(msc * 0.05);
    }

    private static double getSssMsc(double salary) {
        double clamped = clamp(salary, 5000.0, 35000.0);

        if (clamped < 5250.0) {
            return 5000.0;
        }

        return Math.floor((clamped + 250.0) / 500.0) * 500.0;
    }

    public static double getPhilHealthDeduction(double monthlyBasicSalary) {
        if (monthlyBasicSalary <= 0) return 0.0;

        double mbs = clamp(monthlyBasicSalary, 10000.0, 100000.0);
        double totalPremium = mbs * 0.05;

        return round2(totalPremium / 2.0);
    }

    public static double getPagIBIGDeduction(double monthlySalaryBase) {
        if (monthlySalaryBase <= 0) return 0.0;

        double fundSalaryBase = Math.min(monthlySalaryBase, 10000.0);
        double rate = (monthlySalaryBase <= 1500.0) ? 0.01 : 0.02;

        return round2(fundSalaryBase * rate);
    }

    public static double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833.0) {
            return 0.0;
        } else if (taxableIncome <= 33332.0) {
            return round2((taxableIncome - 20833.0) * 0.15);
        } else if (taxableIncome <= 66666.0) {
            return round2(1875.00 + ((taxableIncome - 33333.0) * 0.20));
        } else if (taxableIncome <= 166666.0) {
            return round2(8541.80 + ((taxableIncome - 66667.0) * 0.25));
        } else if (taxableIncome <= 666666.0) {
            return round2(33541.80 + ((taxableIncome - 166667.0) * 0.30));
        } else {
            return round2(183541.80 + ((taxableIncome - 666667.0) * 0.35));
        }
    }

    private static PayrollBreakdown zero() {
        return new PayrollBreakdown(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private static double nvl(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}