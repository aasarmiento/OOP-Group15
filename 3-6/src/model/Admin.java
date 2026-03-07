package model;

import java.time.LocalDate;

/**
 * Admin Class - Handles Management and Payroll.
 * Does NOT implement ISupportOperations.
 */
public class Admin extends Employee implements IAdminOperations, ILeaveOperationsSuperior {

    public Admin() {
        super();
    }

    public Admin(int id, String last, String first, LocalDate bday, double basic) {
        super(id, last, first, bday); 
        this.setBasicSalary(basic); 
        this.setRole(Role.ADMIN); 
    }

    // Constructor for full CSV data
    public Admin(int empNo, String lastName, String firstName, LocalDate birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, double basicSalary, double riceSubsidy, double phoneAllowance, double clothingAllowance, double grossRate, double hourlyRate, Role role) {
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossRate, hourlyRate, role);
    }

    // --- Specialized Computation ---
    @Override
    public double calculateSahod() {
        double managementAllowance = 5000.0;
        return calculateNetPay() + managementAllowance;
    }

    // --- Interface Requirements ---
    @Override public double calculateTotalHoursWorked() { return 160.0; }
    @Override public double computeNet() { return calculateNetPay(); }
    @Override public double calculateSalary() { return calculateSahod(); }
    @Override public Role getRole() { return Role.ADMIN; }

    // --- IAdminOperations ---
    @Override public void createEmployee(Employee emp) { System.out.println("Admin creating ID: " + emp.getEmpNo()); }
    @Override public int generateNextEmpNo() { return 0; }
    @Override public boolean isEmployeeValid(Employee emp) { return emp != null; }
    @Override public void updateEmployee(int empNo) { }
    @Override public void removeEmployee(int empNo) {System.out.println("Admin " + this.getFirstName() + " is authorizing deletion of " + empNo); }

    // --- ILeaveOperationsSuperior ---
    @Override
    public void updateLeaveStatus(LeaveRequest request, String newStatus) {
        request.setStatus(newStatus);
    }

    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        LeaveRequest request = new LeaveRequest(this.getEmpNo(), type, start, end);
        super.applyLeave(request);
        return request;
    }

   

    
}