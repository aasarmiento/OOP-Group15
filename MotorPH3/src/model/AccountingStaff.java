package model;

import java.io.File;
import java.time.LocalDate;

/**
 * AccountingStaff Class (Concrete Child)
 * SPECIAL ABILITY ay magcompute
 */
public class AccountingStaff extends Employee implements IFinanceOperations {

    //constructors

    public AccountingStaff() {
        super();
    }
    
    //para for loading
    public AccountingStaff(int empNo, String lastName, String firstName, LocalDate birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, double basicSalary, double riceSubsidy, double phoneAllowance, double clothingAllowance, double grossRate, double hourlyRate) {
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossRate, hourlyRate);
    }

    // for quick testing only
    public AccountingStaff(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday);
        this.basicSalary = basic;
    }

    // --- 2. iimplement na si IPayrollCalculations kasi lahat ng contract dapat maimplement  ---

    /**
     * 
     * 
     */
   /**
     * ioto and entry point
     * This is what the UI calls.
     */
    @Override
    public double calculateSalary() {
        return calculateSahod(); 
    }

    /**
     * THE ROLE-SPECIFIC MATH
     * Based on your data (e.g., Roderick Alvaro, Accounting Head), 
     * they follow the standard Net Pay rules.
     */
    @Override
    public double calculateSahod() {
        // walang bonus pag accounting kindly double check
        return calculateNetPay();
    }

    // --- Iimplement na ang special ability

    @Override
    public void batchProcessPayroll() {
        // Logic for triggering salary calculation for multiple people
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

    // IImplement na si ILeaveOperations lahat ng employee dapat maka apply ng leave

    @Override
    public LeaveRequest applyLeave(String leaveType, LocalDate startDate, LocalDate endDate) {
        // Matches the Employee abstract method signature
        LeaveRequest newRequest = new LeaveRequest(this.getEmpNo(), this.getFirstName() + " " + this.getLastName(), startDate, endDate);
        this.leaveRequests.add(newRequest);
        return newRequest;
    }

    // --- IImplement na si IUserOPerations laht ng employee dapat makalogin ---

    @Override 
    public Role getRole() { 
        return Role.ACCOUNTING; 
    }

    @Override 
    public boolean login(String user, String pass) { 
        return String.valueOf(this.empNo).equals(user) && isPasswordValid(pass);
    }

    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 8; 
    }

    
    
    @Override public double calculateSSSDeduction() { return super.calculateSSSDeduction(); }
    @Override public double calculatePhilHealth() { return super.calculatePhilHealth(); }
    @Override public double calculatePagIBIG() { return super.calculatePagIBIG(); }
    @Override public double calculateWithholdingTax() { return super.calculateWithholdingTax(); }
}