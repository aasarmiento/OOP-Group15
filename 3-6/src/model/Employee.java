package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import service.PayrollCalculator;

public abstract class Employee implements IAuthenticatable, IPayrollCalculations, ILeaveOperations {
   
    // --- Attributes ---
    protected int empNo;
    private String password;
    protected String lastName;
    protected String firstName;
    protected LocalDate birthday;
    protected String address;
    protected String phone;
    protected String sss;
    protected String philhealth;
    protected String tin;
    protected String pagibig;
    protected String status;
    protected String position;
    protected String supervisor;
    
    protected double basicSalary;
    protected double riceSubsidy;
    protected double phoneAllowance;
    protected double clothingAllowance;
    protected double grossRate;
    protected double hourlyRate;
    private double grossSemiMonthlyRate;
    private String photoPath;

    private Role role;
    protected List<LeaveRequest> leaveRequests = new ArrayList<>();

    // NEW: Instance of the calculator to avoid static reference errors
    private final PayrollCalculator calc = new PayrollCalculator();

    // --- Constructors ---

    public Employee() {}

    public Employee(int empNo, String lastName, String firstName, LocalDate birthday) {
        this.empNo = empNo;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
    }

    public Employee(int empNo, String lastName, String firstName, LocalDate birthday, 
                    String address, String phone, String sss, String philhealth, 
                    String tin, String pagibig, String status, String position, 
                    String supervisor, double basicSalary, double riceSubsidy, 
                    double phoneAllowance, double clothingAllowance, 
                    double grossRate, double hourlyRate, Role role) {
        this.empNo = empNo;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phone = phone;
        this.sss = sss;
        this.philhealth = philhealth;
        this.tin = tin;
        this.pagibig = pagibig;
        this.status = status;
        this.position = position;
        this.supervisor = supervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossRate = grossRate;
        this.hourlyRate = hourlyRate;
        this.grossSemiMonthlyRate = grossRate / 2;
        this.role = role;
    }

    // Inside your Employee.java (Parent Class)
   // Inside model/Employee.java
    public String toCSVString() {
    return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
        this.getEmpNo(), this.getLastName(), this.getFirstName(), this.getBirthday(),
        this.getAddress(), this.getPhone(), this.getSss(), this.getPhilhealth(),
        this.getTin(), this.getPagibig(), this.getStatus(), this.getPosition(),
        this.getBasicSalary(), this.getRiceSubsidy(), this.getPhoneAllowance(),
        this.getClothingAllowance(), this.getGrossRate(), this.getHourlyRate());
}

    // --- Getters ---

    public int getEmpNo() { return this.empNo; }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public LocalDate getBirthday() { return this.birthday; }
    public String getAddress() { return this.address; }
    public String getPhone() { return this.phone; }
    public String getSss() { return this.sss; }
    public String getPhilhealth() { return this.philhealth; }
    public String getTin() { return this.tin; }
    public String getPagibig() { return this.pagibig; }
    public String getStatus() { return this.status; }
    public String getPosition() { return this.position; }
    public String getSupervisor() { return this.supervisor; }
    public double getBasicSalary() { return this.basicSalary; }
    public double getRiceSubsidy() { return this.riceSubsidy; }
    public double getPhoneAllowance() { return this.phoneAllowance; }
    public double getClothingAllowance() { return this.clothingAllowance; }
    public double getGrossRate() { return this.grossRate; }
    public double getHourlyRate() { return this.hourlyRate; }
    public String getStoredPassword() { return this.password; }
    public double getGrossSemiMonthlyRate() { return this.grossSemiMonthlyRate; }
    public String getPhotoPath() { return this.photoPath; }
    public String getPassword() { return this.password; }
    
    @Override
    public Role getRole() { return this.role; }

    // --- Setters ---

    public void setEmpNo(int empNo) { this.empNo = empNo; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSss(String sss) { this.sss = sss; }
    public void setPhilhealth(String philhealth) { this.philhealth = philhealth; }
    public void setTin(String tin) { this.tin = tin; }
    public void setPagibig(String pagibig) { this.pagibig = pagibig; }
    public void setStatus(String status) { this.status = status; }
    public void setPosition(String position) { this.position = position; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public void setGrossRate(double grossRate) { this.grossRate = grossRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public void setPassword(String password) { this.password = password; }
    public final void setRole(Role role) { this.role = role; }
    
    // --- IAuthenticatable Implementation ---

    @Override
    public boolean isPasswordValid(String pass) {
        // Enforces strict privacy by using the private 'password' field
        return this.password != null && this.password.equals(pass);
    }

    @Override
    public void resetPassword() {
    // State management: changes the internal state to alert the System Controller
    this.status = "PASSWORD_RESET_REQUIRED";
    
    // FIXED: Use getEmpNo() to match your existing getter method
    System.out.println("ALERT: IT Support ticket generated for Employee ID: " + this.getEmpNo());
}

    // --- IPayrollCalculations (Fixed: Using the 'calc' instance) ---

    @Override
    public double calculateSSSDeduction() {
        return calc.getSSSDeduction(this.basicSalary);
    }

    @Override
    public double calculatePhilHealth() {
        return calc.getPhilHealthDeduction(this.basicSalary);
    }

    @Override
    public double calculatePagIBIG() {
        return calc.getPagIBIGDeduction();
    }

    @Override
    public double computeDeductions() {
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG();
    }

    @Override
    public double calculateWithholdingTax() {
        // FIXED: calculateTaxFromParts logic using internal calc
        double taxableIncome = calculateGrossSalary() - computeDeductions();
        return calc.getWithholdingTax(taxableIncome);
    }

    @Override
    public double calculateGrossSalary() {
        return (this.hourlyRate * 160) + riceSubsidy + phoneAllowance + clothingAllowance;
    }

    @Override
    public double calculateNetPay() {
        return calculateGrossSalary() - (computeDeductions() + calculateWithholdingTax());
    }

    @Override
    public double calculateSalary() {
        return calculateSahod();
    }

    @Override
    public double computeNet() {
        return calculateNetPay();
    }

    

    // Abstract method to be defined by Admin/RegularStaff
    public abstract double calculateSahod();

    // --- ILeaveOperations ---

    @Override
    public void applyLeave(LeaveRequest request) {
        if (request != null) this.leaveRequests.add(request);
    }

    @Override
    public List<LeaveRequest> viewAllLeaveRequests() {
        return this.leaveRequests;
    }

    // This forces Admin/Regular classes to define the leave application logic
    public abstract LeaveRequest applyLeave(String type, LocalDate start, LocalDate end);

    public void requestTechnicalSupport(String issueCategory) {
        System.out.println("Employee " + this.empNo + " is requesting IT support for: " + issueCategory);
        this.status = "SUPPORT_REQUESTED"; // Updates internal state for IT to see
    }
}