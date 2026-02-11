/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;

/**
 *
 * @author abigail
 */
public abstract class Employee implements IUserOperations, IPayrollCalculations {
   
    protected String empNo;
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

    public Employee () {
    }
public LocalDate getBirthday() {
    return birthday;
}
    public Employee (String empNo, String lastName, String firstName, LocalDate birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, double basicSalary, double riceSubsidy, double phoneAllowance, double clothingAllowance, double grossRate, double hourlyRate) {
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
    }

    public String getEmpNo() {
        return empNo;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getSss() {
        return sss;
    }

    public String getPhilhealth() {
        return philhealth;
    }

    public String getTin() {
        return tin;
    }

    public String getPagibig() {
        return pagibig;
    }

    public String getStatus() {
        return status;
    }

    public String getPosition() {
        return position;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public double getGrossRate() {
        return grossRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    
    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSss(String sss) {
        this.sss = sss;
    }

    public void setPhilhealth(String philhealth) {
        this.philhealth = philhealth;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public void setPagibig(String pagibig) {
        this.pagibig = pagibig;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public void setBasicSalary(double basicSalary) {
        if (basicSalary < 0) {
        throw new IllegalArgumentException("Salary must be greater than or equal to 0");
    }
    this.basicSalary = basicSalary;
}

    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
    }

    public void setGrossRate(double grossRate) {
        this.grossRate = grossRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    public void setBirthday(LocalDate birthday) {
    // Rule 1: birthday must not be null
    if (birthday == null) {
        throw new IllegalArgumentException("Birthday cannot be null.");
    }
    
    // Rule 2: birthday must not be a future date
    if (birthday.isAfter(LocalDate.now())) {
        throw new IllegalArgumentException("Birthday cannot be in the future.");
    }
    
    this.birthday = birthday;
}
   // --- Methods from IUserOperations (Auth Contract) ---
    @Override
    public abstract boolean login();
     
    @Override
    public abstract Role getRole(); // This matches your Role.java enum
     
    @Override
    public abstract boolean isPasswordValid();

    @Override
    public abstract int getPasswordStrength();
 
    @Override
    public abstract void resetPassword();
    
    @Override
    public abstract void logout();
    
    // --- Method for Employee Hierarchy (Base Rule) ---
    // Note: No @Override here because it's not in your interfaces
    @Override
    public abstract void calculateSalary();
    
   
    
    
    
}


