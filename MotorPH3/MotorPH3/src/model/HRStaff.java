package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import service.PayrollCalculator; // Imported for streamlined payroll calls


/**
 * HRStaff Class (Concrete Child)

 */
public class HRStaff extends Employee implements IHROperations, ILeaveOperationsSuperior{
//superclass is Employee and implements the interface IHROperations 
    // Constructors
    public HRStaff() {
        super(); 
    }

    public HRStaff(int empNo, String lastName, String firstName, java.time.LocalDate birthday, 
               String address, String phone, String sss, String philhealth, String tin, 
               String pagibig, String status, String position, String supervisor, 
               double basicSalary, double riceSubsidy, double phoneAllowance, 
               double clothingAllowance, double grossRate, double hourlyRate) {
    
    super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, 
          tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, 
          phoneAllowance, clothingAllowance, grossRate, hourlyRate);
}

    public HRStaff(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday);
        this.basicSalary = basic;
    }
    // --- Implementation of IHROperations ---
@Override
public Employee viewEmployeeProfile(int empID) {
    System.out.println("HR Internal Firewall: Accessing Profile for ID #" + empID);
    
   
    return null; 
}
@Override
public void approveLeaveRequest(String leaveID) {
   
    System.out.println("HR Internal Firewall: Approving Leave ID " + leaveID);
    
}

@Override
public void rejectLeaveRequest(String leaveID, String reason) {
    System.out.println("HR Internal Firewall: Rejecting Leave ID " + leaveID + ". Reason: " + reason);
}

@Override
public void updateLeaveStatus(String leaveID, String newStatus) {
   
    System.out.println("Updating status of " + leaveID + " to " + newStatus);

    
}
   
    @Override
    public void applyLeave(LeaveRequest request) {
       
        this.leaveRequests.add(request);
        System.out.println("HR Staff applying for leave: " + request.getRequestId());
    }
@Override
public void updateEmployeeDetails(int empID, Employee updatedData) {
    
    if (updatedData == null) {
        System.out.println("Error: No data provided for update.");
        return;
    }
    

    
    System.out.println("HR Authority: Updating Employee #" + empID);

    
    
    System.out.println("Success: Record for " + updatedData.getFirstName() + " has been synchronized.");
}

    
    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
    
    LeaveRequest newRequest = new LeaveRequest(this.getEmpNo(), this.getFirstName() + " " + this.getLastName(), start, end);
    
    
    this.leaveRequests.add(newRequest);
    
    System.out.println("HR Staff initiated a " + type + " leave request.");
    
  
    return newRequest; 
}

    
    @Override
    public List<LeaveRequest> viewAllLeaveRequests() {
    
    return new ArrayList<>(); 
}

    
    @Override
    public void updateLeaveStatus(LeaveRequest request, String newStatus) {
       
    request.setStatus(newStatus);
    
    request.setApprovedBy(String.valueOf(this.getEmpNo()));
    request.setApprovalDate(LocalDate.now());
    
    
    System.out.println("HR AUTHORITY: Request " + request.getRequestId() + " updated to " + newStatus);
}
    // --- Implementation of IUserOperations ---
    @Override
    public Role getRole() {
        return Role.HR_STAFF; 
    }
    

    // --- IUserOperations Implementation ---

    @Override
    public boolean login(String user, String pass) {
       
        if (String.valueOf(this.empNo).equals(user) && isPasswordValid(pass)) {
            System.out.println("HR Access Granted for: " + this.firstName);
            return true;
        }
        return false;
    }

    
    @Override 
    public boolean isPasswordValid(String pass) { 
        return pass != null && pass.length() >= 6; 
    }
    
    @Override public int getPasswordStrength() { return 80; }
    @Override public void resetPassword() { super.resetPassword(); }
    @Override public void logout() { System.out.println("HR Logged out."); }

    // --- Implementation of IPayrollCalculations ---

   
    @Override
    public double calculateSahod() {
        // This triggers the parent's protected net pay logic
        return calculateNetPay(); 
    }

    // --- The UI Entry Point ---
    @Override
    public double calculateSalary() {
        
        return calculateSahod();
    }

    
    @Override
    public double calculateTotalHoursWorked() {
        
        return 160.0; 
    }

    
    
    @Override
    public double calculateSSSDeduction() {
        return PayrollCalculator.getSSSDeduction(this.basicSalary);
    }

    @Override
    public double calculatePhilHealth() {
        return PayrollCalculator.getPhilHealthDeduction(this.basicSalary);
    }

    @Override
    public double calculatePagIBIG() {
        return PayrollCalculator.getPagIBIGDeduction();
    }
    
    @Override
    public double computeDeductions() {
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG();
    }
    
}