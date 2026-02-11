package model;

import java.util.List;
import java.util.ArrayList;

public class HRStaff extends Employee implements IHROperations, IPayrollCalculations {

    public HRStaff() {
        super();
    }

    // This MUST match the IHROperations dictionary exactly
    @Override
    public void updateEmployeeDetails(String empID, Employee updatedData) {
        // Logic for updating records
    }

    @Override
    public Employee viewEmployeeProfile(String empID) {
        return this; // Placeholder for logic
    }

    @Override
    public void approveLeaveRequest(String leaveID) {
        // Logic for approval
    }

@Override
public void rejectLeaveRequest(String leaveID, String reason) { 
    // code
}

    /**
     *
     * @return
     */
    @Override
    public List<LeaveRequest> viewAllLeaveRequests() {
        return new ArrayList<>();
    }
    
    @Override
    public Role getRole() {
    return Role.HR_STAFF; // Match the ALL_CAPS name in your Role enum

}
    // REQUIRED methods inherited from Employee/IUserOperations
    @Override 
    public boolean login()
    { 
        return true; 
    }
    @Override
    public boolean isPasswordValid()
    { 
        return true; 
    }
    @Override
    public int getPasswordStrength()
    {
        return 10; 
    }
    @Override
    public void resetPassword() 
    { 
    
    }
    @Override
    public void logout()
    { 
    
    }
    

// --- Implementation of IPayrollCalculations ---

    @Override
    public double calculateTotalHoursWorked() {
        // Attendance Aggregation: In Week 5, this will call LogHandler logic
        return 160.0; // Placeholder for nowLogic: Attendance Aggregation
    }

   @Override
    public double calculateGrossSalary() {
    // 1. Get the hours (Logic: Attendance Aggregation)
    double hoursWorked = calculateTotalHoursWorked();

    // 2. Access protected fields from Employee parent
    // Note: Use 'this' to refer to the current HRStaff object's data
    return (this.hourlyRate * hoursWorked) + 
           this.riceSubsidy + 
           this.phoneAllowance + 
           this.clothingAllowance;
}

    @Override
    public double calculateSSSDeduction() {
        // Statutory Compliance: Uses protected basicSalary from Parent
        double salary = this.basicSalary; 
        if (salary < 4000) return 180.0; // Example SSS_RATE_MIN
        if (salary >= 29750) return 1350.0; // Example SSS_RATE_MAX
        
        // Loop through your SSS_TABLE logic here as defined in Week 3 math contract
        return 0.0;
    }

    @Override
    public double calculatePhilHealth() {
        // Maintainability: One formula for all roles
        double contribution = this.basicSalary * 0.05; // 5% rate
        return Math.min(contribution, 2500.0); // 2500 Cap
    }

    @Override
    public double calculatePagIBIG() {
        return 100.0; // Fixed contribution
    }

    @Override
    public double calculateWithholdingTax() {
        // 1. Declare and calculate Taxable Income
        double taxableIncome = calculateGrossSalary() - 
                              (calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG());

        // 2. Apply Tax Table Logic (From your CP2 logic)
        double tax = 0.0;
        
        if (taxableIncome <= 20833) {
            tax = 0.0;
        } else if (taxableIncome <= 33332) {
            tax = (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66666) {
            tax = (taxableIncome - 33333) * 0.25 + 2500;
        } else if (taxableIncome <= 166666) {
            tax = (taxableIncome - 66667) * 0.30 + 10833;
        } else if (taxableIncome <= 666666) {
            tax = (taxableIncome - 166667) * 0.32 + 40833.33;
        } else {
            tax = (taxableIncome - 666667) * 0.35 + 200833.33;
        }

        // 3. Return the final value to satisfy the interface contract
        return tax;
    
    }

    @Override
    public double calculateNetPay() {
        // Data Integrity: Gross - Deductions
        double gross = calculateGrossSalary();
        double totalDeductions = calculateSSSDeduction() + calculatePhilHealth() + 
                                 calculatePagIBIG() + calculateWithholdingTax();
        return gross - totalDeductions;
    }
    
    
    
    @Override
public void calculateSalary() {
    // 1. Gather all individual components
    double gross = calculateGrossSalary();
    double sss = calculateSSSDeduction();
    double philHealth = calculatePhilHealth();
    double pagIBIG = calculatePagIBIG();
    double tax = calculateWithholdingTax();
    
    // 2. Add HR-Specific "Personnel Incentives"
    double hrIncentive = 500.00; 
    double finalGross = gross + hrIncentive; // Combining base pay and allowances
    
    // 3. Compute final result using the Net Pay logic
    double totalDeductions = sss + philHealth + pagIBIG + tax;
    double netPay = finalGross - totalDeductions;

    // 4. Output the summary for the Payroll System
    System.out.println("----- PAYROLL SUMMARY FOR HR -----");
    System.out.println("Gross Salary (inc. Incentive): " + finalGross);
    System.out.println("SSS Deduction:                 " + sss);
    System.out.println("PhilHealth Deduction:          " + philHealth);
    System.out.println("Withholding Tax:               " + tax);
    System.out.println("----------------------------------");
    System.out.println("NET PAY:                       " + netPay);
}
    }