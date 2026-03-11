package model;

import java.time.LocalDate;

public class RegularStaff extends Employee {

    // --- CONSTRUCTORS ---
    
    public RegularStaff() {
        super();
    }

    // FIXED: Added Role.REGULAR_STAFF to the super call and setRole
    public RegularStaff(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday);
        this.basicSalary = basic;
        this.setRole(Role.REGULAR_STAFF); 
    }

    // FIXED: Added 'Role role' parameter to match the 20-parameter Employee constructor
    // Constructor for full CSV data
public RegularStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
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

    // --- PAYROLL LOGIC ---

    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; // Standard requirement for Regular Staff
    }

    @Override
    public double calculateGrossSalary() {
        // We use the variables from the parent Employee class
        double basePay = this.hourlyRate * calculateTotalHoursWorked();

        // Probationary staff usually don't get full allowances yet
        if ("Probationary".equalsIgnoreCase(this.getStatus())) {
            return basePay;
        } 
        
        return basePay + this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }
    
    @Override
    public double calculateSalary() {
        return calculateSahod(); 
    }

    @Override
    public double calculateSahod() {
        // Regular staff usually just get the standard Net Pay calculation
        return calculateNetPay();
    }

    // --- AUTH & ROLES ---

    @Override
    public Role getRole() {
        return Role.REGULAR_STAFF; 
    }

    @Override
    public boolean isPasswordValid(String pass) {
        // Standard 8-character security
        return pass != null && pass.length() >= 8;
    }

    // --- LEAVE MANAGEMENT ---
    
    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        // Create the request using parent getters
        LeaveRequest request = new LeaveRequest(this.getEmpNo(), type, start, end);
        
        // Add to the list in the parent class
        this.leaveRequests.add(request);
        
        System.out.println("Regular Staff leave request submitted for: " + this.getLastName());
        return request;
    }


          // for it 
 
}