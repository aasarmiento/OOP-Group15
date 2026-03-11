package motorph3;

import model.PeriodSummary;
import model.RegularStaff;
import service.PayrollCalculator;

public class PayrollCalculatorTestDrive {
    public static void main(String[] args) {
        RegularStaff emp = new RegularStaff();

        emp.setEmpNo(10034);
        emp.setFirstName("Beatriz");
        emp.setLastName("Santos");

        // sample values
        emp.setBasicSalary(30000.00);
        emp.setHourlyRate(187.50);
        emp.setRiceSubsidy(1500.00);
        emp.setPhoneAllowance(1000.00);
        emp.setClothingAllowance(500.00);

        PayrollCalculator calc = new PayrollCalculator();

        double hoursWorked = 160.0;

        double gross = calc.calculateGrossIncome(emp, hoursWorked);
        PeriodSummary summary = calc.calculateFullSummary(emp, gross);

        System.out.println("=== PAYROLL TEST ===");
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Gross: " + gross);
        System.out.println("SSS: " + summary.getSss());
        System.out.println("PhilHealth: " + summary.getPhilhealth());
        System.out.println("Pag-IBIG: " + summary.getPagibig());
        System.out.println("Tax: " + summary.getTax());
        System.out.println("Net Pay: " + summary.getNetPay());
    }
}