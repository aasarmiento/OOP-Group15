package model;

import java.io.File;
import java.time.LocalDate;


public class AccountingStaff extends Employee implements IFinanceOperations {

    // constructor childclass ito ni employee class! ang special ability ay ang accounting and finance panel sya lang may access aside sa admin 

    public AccountingStaff() {
        super();
    }
    
   // ang add ako ng gender para gagamiting mamaya
public AccountingStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
             String address, String phone, String sss, String philhealth, 
             String tin, String pagibig, String status, String position, 
             String supervisor, double basicSalary, double riceSubsidy, 
             double phoneAllowance, double clothingAllowance, double grossRate, 
             double hourlyRate, Role role, String gender) {  
    
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
        return 160.0; 
    }

    @Override
    public double calculateSalary() {
        return calculateSahod(); 
    }

    @Override
    public double calculateSahod() {
        
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

   // impplement na natin si Ileaveoperations kaya na nila magleave dahil dto 
    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        // ito correct parameter para sa anong klase leave dati nilagay natin dto start end lang walang type so inupdate natin 
        LeaveRequest newRequest = new LeaveRequest(this.getEmpNo(), type, start, end);
        this.leaveRequests.add(newRequest);
        return newRequest;
    }

    // para sa verfication important to 

    @Override 
    public Role getRole() { 
        return Role.ACCOUNTING; 
    }

    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 8; 
    }

    // ginamit natin ung parent class inoveride na lang natin wala namn din sila piangkaiba unlike sa regualr staff and admin na may iba computation
    @Override public double calculateSSSDeduction() { return super.calculateSSSDeduction(); }
    @Override public double calculatePhilHealth() { return super.calculatePhilHealth(); }
    @Override public double calculatePagIBIG() { return super.calculatePagIBIG(); }
    @Override public double calculateWithholdingTax() { return super.calculateWithholdingTax(); }




}