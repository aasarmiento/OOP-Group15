package model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import service.PayrollCalculator;


public abstract class Employee implements IUserOperations, IPayrollCalculations, ILeaveOperations {
   
    // attributes this is the parent class lahat ng andto dapat mayron ang lahat ng employee regarless anong role 
    
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
    
    // Protected financial fields 
    protected double basicSalary;
    protected double riceSubsidy;
    protected double phoneAllowance;
    protected double clothingAllowance;
    protected double grossRate;
    protected double hourlyRate;
    private double grossSemiMonthlyRate;

    

    protected List<LeaveRequest> leaveRequests = new ArrayList<>();

    public Employee () {}

    public Employee (int empNo, String lastName, String firstName, LocalDate birthday, 
                    String address, String phone, String sss, String philhealth, 
                    String tin, String pagibig, String status, String position, 
                    String supervisor, double basicSalary, double riceSubsidy, 
                    double phoneAllowance, double clothingAllowance, 
                    double grossRate, double hourlyRate) {
        // constructors
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
        this.grossRate = grossRate; // This is grossRate
        this.hourlyRate = hourlyRate;
        this.grossSemiMonthlyRate = grossRate; // Logic fix: Assign grossRate to the private field
    }

   
    public Employee(int empNo, String lastName, String firstName, LocalDate birthday) {
        this.empNo = empNo;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
    }
    // --- Getters ---

    public int getEmpNo() { 
        return this.empNo; 
    }

    public String getFirstName() {
         return this.firstName;
         }

    public String getLastName() {
         return this.lastName;
         }

    public LocalDate getBirthday() {
         return this.birthday;
         }

    public String getAddress() {
         return this.birthday != null ? this.address : ""; // Safe check
         }

    public String getPhone() {
         return this.phone; 
        }

    public String getSss() { 
        return this.sss; 
    }

    public String getPhilhealth() { 
        return this.philhealth; 
    }

    public String getTin() {
         return this.tin; 
        }

    public String getPagibig() {
         return this.pagibig; 
        }

    public String getStatus() {
         return this.status; 
        }

    public String getPosition() {
         return this.position; 
        }

    public String getSupervisor() { 
        return this.supervisor; 
    }

    public double getBasicSalary() { 
        return this.basicSalary; 
    }

    public double getRiceSubsidy() {
         return this.riceSubsidy; 
        }

    public double getPhoneAllowance() {
         return this.phoneAllowance; 
        }

    public double getClothingAllowance() {
         return this.clothingAllowance; 
        }

    public double getGrossRate() {
         return this.grossRate; 
        }

    public double getHourlyRate() {
         return this.hourlyRate; 
        }

       
        public String getStoredPassword() {
    return this.password; 

    
}
public double getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    // --- Setters ---

    public void setEmpNo(int empNo) {
         this.empNo = empNo; 
        }

    public void setFirstName(String firstName) {
         this.firstName = firstName;
         }

    public void setLastName(String lastName) {
         this.lastName = lastName;
         }

    public void setBirthday(LocalDate birthday) {
         this.birthday = birthday;
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

         public void setPassword(String password) {
    this.password = password;
}

    // --- IUserOperations ---


//
@Override
public boolean login(String user, String pass) {
  
    return isPasswordValid(pass);
}
     
    @Override public abstract Role getRole();
   

    @Override
   public int getPasswordStrength() {
    
    return 0; 
}

    @Override
    public void resetPassword() {
   
    this.status = "PASSWORD_RESET_REQUIRED";
}
    @Override
    public boolean isPasswordValid(String pass) {
    return this.password != null && this.password.equals(pass);
}
    @Override
    public void logout() {
    
    this.status = "OFFLINE";
}
    
    // --- IPayrollCalculations (Logic Delegated to service.PayrollCalculator) ---
    
  

    
   @Override
public double calculateSalary() {
    
    return calculateSahod(); 
}

   
    public abstract double calculateSahod();

    
    @Override
    public double computeDeductions() {
        
        return calculateSSSDeduction() + calculatePhilHealth() + calculatePagIBIG();
    }
   
    @Override
    public double calculateTotalHoursWorked() {
        return 160.0; // Standard month
    }
   
    @Override
    public double calculateGrossSalary() {
        return (this.hourlyRate * calculateTotalHoursWorked()) + 
               this.riceSubsidy + this.phoneAllowance + this.clothingAllowance;
    }
   
    @Override
    public double calculateSSSDeduction() {
       
        return PayrollCalculator.getSSSDeduction(this.basicSalary);
    }
    public double calculateSSS() {
    return PayrollCalculator.getSSSDeduction(this.basicSalary);
}
   // Standard PhilHealth (4%)
    @Override
    public double calculatePhilHealth() {
       
        return PayrollCalculator.getPhilHealthDeduction(this.basicSalary);
    }
   
    @Override
    public double calculatePagIBIG() {
        
        return PayrollCalculator.getPagIBIGDeduction();
    }
    
   @Override
public double calculateWithholdingTax() {
    
    return PayrollCalculator.calculateTaxFromParts(
        this.calculateGrossSalary(), 
        this.computeDeductions()
    );
}
    
    @Override
    public double calculateNetPay() {
        return calculateGrossSalary() - (computeDeductions() + calculateWithholdingTax());
    }
    
    @Override
    public double computeNet() {
        return calculateNetPay();
    }

    // --- ILeaveOperations ---
    @Override
public void applyLeave(LeaveRequest request) {
   
    if (request != null) {
        this.leaveRequests.add(request);
    }
}

    @Override
    public List<LeaveRequest> viewAllLeaveRequests() {
        return this.leaveRequests;
    }
  
    // Partial Abstraction forcing specialized subclasses to define how leaves are filed.
    public abstract LeaveRequest applyLeave(String type, LocalDate start, LocalDate end);


    
}