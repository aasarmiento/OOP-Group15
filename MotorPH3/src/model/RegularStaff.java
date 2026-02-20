package model;

import java.time.LocalDate;



public class RegularStaff extends Employee {

    // --- Constructors ---
    public RegularStaff() {
        super();
    }

    public RegularStaff(int empNo, String lastName, String firstName, LocalDate birthday, 
                        String address, String phone, String sss, String philhealth, 
                        String tin, String pagibig, String status, String position, 
                        String supervisor, double basicSalary, double riceSubsidy, 
                        double phoneAllowance, double clothingAllowance, 
                        double grossRate, double hourlyRate) {
        super(empNo, lastName, firstName, birthday, address, phone, sss, philhealth, 
              tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, 
              phoneAllowance, clothingAllowance, grossRate, hourlyRate);
    }

    
    @Override
    public double calculateGrossSalary() {
        double basePay = this.hourlyRate * calculateTotalHoursWorked();

        // Polymorphic Logic: Probationary staff only get base pay (no allowances)
        if ("Probationary".equalsIgnoreCase(this.getStatus())) {
            return basePay;
        } 
        // Regular staff get the full package (Inherited from Parent variables)
        return basePay + this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }

    // --- Implementation of Required IUserOperations ---

    @Override
    public Role getRole() {
        return Role.REGULAR_STAFF; 
    }

    @Override
    public boolean isPasswordValid(String pass) {
        // Validation Rule: password must be longer than 8 characters
        return pass != null && pass.length() >= 8;
    }

    // --- ILeaveOperations (Concrete Implementation) ---

    @Override
    public LeaveRequest applyLeave(String type, LocalDate start, LocalDate end) {
        // CRC: Auto-initializes status to PENDING
        LeaveRequest request = new LeaveRequest(this.getEmpNo(), 
                               this.getFirstName() + " " + this.getLastName(), 
                               start, end);
        this.leaveRequests.add(request);
        return request;
    }

    // --- Cleanup: Inheriting from Parent ---
    // Note: We DELETE calculatePhilHealth(), calculateSSSDeduction(), etc. 
    // because the Parent (Employee) now handles them using the static PayrollCalculator!

    public RegularStaff(int id, String last, String first, LocalDate bday, double basic) {
    super(id, last, first, bday);
    this.basicSalary = basic;
}
}