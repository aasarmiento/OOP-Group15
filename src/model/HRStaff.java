package model;

import java.time.LocalDate;
import java.util.List;

public class HRStaff extends Employee implements IHROperations, ILeaveOperationsSuperior {

    public HRStaff() { super(); }

    // Full constructor for CSV/Database loading
    // Constructor for full CSV data
public HRStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
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
    /**
     * ADDED: Small constructor for Role Upgrading in CSVHandler.
     * This fixes the "no suitable constructor found" error.
     */
    // Add this 6-parameter constructor to HRStaff.java
public HRStaff(int id, String last, String first, LocalDate bday, double basic, String gender) {
    // 1. Call the basic super constructor
    super(id, last, first, bday); 
    
    // 2. Set the specific fields
    this.setBasicSalary(basic); 
    this.setGender(gender);
    this.setRole(Role.HR_STAFF); 
}

    // --- METHOD DICTIONARY LOGIC ---

    @Override
    public void updateEmployeeDetails(int empID, Employee datavoid) {
        // Business Rule: Record the synchronization event
        System.out.println("HR Staff #" + this.getEmpNo() + " is updating record #" + empID);
    }

    @Override
    public Employee viewEmployeeProfile(int empID) {
        // Logic: Checks permission level (EMPLOYEE_READ)
        return null; // Actual data comes from Service -> DAO
    }

    @Override
    public void approveLeaveRequest(String leaveID) {
        System.out.println("HR Permission: Approving " + leaveID);
    }

    @Override
    public void rejectLeaveRequest(String leaveID, String reason) {
        System.out.println("HR Permission: Rejecting " + leaveID + " | Reason: " + reason);
    }

    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        return new LeaveRequest(this.getEmpNo(), this.getLastName(), this.getFirstName(), type, start, end, "N/A");
    }

    // --- OTHER REQUIRED OVERRIDES ---
    @Override public double calculateSahod() { return calculateNetPay(); }
    @Override public double calculateTotalHoursWorked() { return 160.0; }
    @Override public Role getRole() { return Role.HR_STAFF; }
    
    @Override
    public void updateLeaveStatus(String leaveID, String newStatus) {
        System.out.println("Status for " + leaveID + " set to " + newStatus);
    }

    @Override
    public void updateLeaveStatus(LeaveRequest request, String newStatus) {
        request.setStatus(newStatus);
        request.setApprovedBy(String.valueOf(this.getEmpNo()));
        request.setApprovalDate(LocalDate.now());
    }

    public List<LeaveRequest> viewAllLeaveRequests(service.HRSerbisyo hrService) {
    // Business Logic: Only an HRStaff role can trigger this request
    // FIXED: Changed fetchAllLeavesAsList() to getAllLeaveRequestsList()
    return hrService.getAllLeaveRequestsList();
}
  
}