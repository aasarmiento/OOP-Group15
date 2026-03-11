package model;

import java.io.File;
import java.time.LocalDate;

/**
 * AccountingStaff Class (Concrete Child)
 * Focuses on Payroll Processing and Financial Reporting.
 */
public class AccountingStaff extends Employee implements IFinanceOperations {

    // --- CONSTRUCTORS ---

    public AccountingStaff() {
        super();
    }
    
    // FIXED: Added 'Role role' to match the 20-parameter Employee constructor
    // Constructor for full CSV data
public AccountingStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
             String address, String phone, String sss, String philhealth, 
             String tin, String pagibig, String status, String position, 
             String supervisor, double basicSalary, double riceSubsidy, 
             double phoneAllowance, double clothingAllowance, double grossRate, 
             double hourlyRate, Role role, String gender) { // <--- ADD String gender HERE
    
    // Pass gender to the super constructor
    super(empNo, lastName, firstName, birthday, address, phone, sss, 
          philhealth, tin, pagibig, status, position, supervisor, 
          basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, 
          grossRate, hourlyRate, role, gender); 
}

   public AccountingStaff(int id, String last, String first, LocalDate bday, double basic, String gender) {
    super(id, last, first, bday); 
    this.setBasicSalary(basic); 
    this.setGender(gender);
    this.setRole(Role.ACCOUNTING); 
}


    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; // Standard 21.67 days per month average
    }

    @Override
    public double calculateSalary() {
        return calculateSahod(); 
    }

    @Override
    public double calculateSahod() {
        // Accounting usually follows standard Net Pay rules
        return calculateNetPay();
    }

    @Override
    public double computeNet() {
        return calculateNetPay();
    }

    // --- IFinanceOperations Implementation ---

    @Override
    public void batchProcessPayroll() {
        System.out.println("Accounting: Triggering batch calculation for current pay period.");
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

    // --- ILeaveOperations Implementation ---

    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        // Corrected constructor to match LeaveRequest(int, String, LocalDate, LocalDate)
        LeaveRequest newRequest = new LeaveRequest(this.getEmpNo(), type, start, end);
        this.leaveRequests.add(newRequest);
        return newRequest;
    }

    // --- Auth & Role ---

    @Override 
    public Role getRole() { 
        return Role.ACCOUNTING; 
    }

    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 8; 
    }

    // Using parent implementations for deductions to keep it clean
    @Override public double calculateSSSDeduction() { return super.calculateSSSDeduction(); }
    @Override public double calculatePhilHealth() { return super.calculatePhilHealth(); }
    @Override public double calculatePagIBIG() { return super.calculatePagIBIG(); }
    @Override public double calculateWithholdingTax() { return super.calculateWithholdingTax(); }




}