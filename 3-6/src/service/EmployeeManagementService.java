package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField; // Added this
import model.Employee;
import model.IAdminOperations;
import model.RegularStaff;

public class EmployeeManagementService {
    private final EmployeeDAO employeeDao;
    private AttendanceDAO attendanceDao; // Add this

  public EmployeeManagementService(EmployeeDAO employeeDao, AttendanceDAO attendanceDao) {
        this.employeeDao = employeeDao;
        this.attendanceDao = attendanceDao;
    }
    /**
     * N-TIER RULE: The UI calls this with raw data. 
     * The Service creates the Model and performs calculations.
     */
    public boolean processNewHire(Employee actor, String fName, String lName, String sss, double salary) {
        // 1. Permission Check
        if (!(actor instanceof IAdminOperations)) {
            showError("Access Denied: Only Admins can register employees.");
            return false;
        }

        // 2. Instantiate the Model (UI doesn't do this anymore)
        Employee newEmp = new RegularStaff();
        newEmp.setFirstName(fName);
        newEmp.setLastName(lName);
        newEmp.setSss(sss);
        newEmp.setBasicSalary(salary);
        
        // 3. Set Defaults (Rice, Phone, Clothing usually fixed per company policy)
        newEmp.setRiceSubsidy(1500);
        newEmp.setPhoneAllowance(500);
        newEmp.setClothingAllowance(1000);

        // 4. Pass to the existing registration logic for validation and math
        return registerEmployee((IAdminOperations)actor, newEmp);
    }

    public boolean registerEmployee(IAdminOperations actor, Employee emp) {
        if (actor == null || emp == null) return false;

        // Validation Logic
        if (emp.getFirstName().isEmpty() || emp.getLastName().isEmpty()) {
            showError("First and Last names are required!");
            return false;
        }

        // Calculation Logic
        emp.setEmpNo(employeeDao.getNextAvailableId());
        double hourly = emp.getBasicSalary() / 21 / 8;
        emp.setHourlyRate(hourly);

        double totalGross = emp.getBasicSalary() + emp.getRiceSubsidy() + 
                           emp.getPhoneAllowance() + emp.getClothingAllowance();
        emp.setGrossRate(totalGross);

        return employeeDao.addEmployee(emp);
    }

    //added for IT ticketing login and dashboard panel, getter para sa panel and DB
    public EmployeeDAO getEmployeeDao() {
    return employeeDao;
    
    }

    public List<Employee> getAll() { 
        return employeeDao.getAll();
    }

    public boolean deleteEmployee(Employee actor, int id) {
        if (!(actor instanceof IAdminOperations)) return false;
        if (id == 10001) return false; 
        return employeeDao.deleteEmployee(id);
    }

    // Add this inside EmployeeManagementService.java
public Employee findById(int id) {
    // Business Rule: We simply pass the request to the DAO to fetch the model
    return employeeDao.findById(id);
}


    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }


    public int generateNextEmployeeId() {
    // This calls the method we defined in your EmployeeDAO interface
    return employeeDao.getNextAvailableId();
}


// Inside EmployeeManagementService.java
// Inside EmployeeManagementService.java
public boolean updateEmployeeDetails(Employee actor, Employee updatedData) {
    // 1. Validation/Permission check
    if (!(actor instanceof IAdminOperations)) return false;

    // 2. Business Logic: Recalculate Rates
    double hourly = updatedData.getBasicSalary() / 21 / 8;
    updatedData.setHourlyRate(hourly);

    double totalGross = updatedData.getBasicSalary() + updatedData.getRiceSubsidy() + 
                       updatedData.getPhoneAllowance() + updatedData.getClothingAllowance();
    updatedData.setGrossRate(totalGross);

    // 3. Call DAO to update cache and save to CSV
    return employeeDao.update(updatedData);
}



// Inside EmployeeManagementService.java

/**
 * This method satisfies the UI's call to 'removeEmployee'
 * by redirecting it to your existing 'deleteEmployee' logic.
 */
public boolean removeEmployee(IAdminOperations actor, int id) {
    // We cast the IAdminOperations back to Employee to reuse your existing method
    return deleteEmployee((Employee) actor, id);
}

// Inside EmployeeManagementService.java
public boolean updateEmployeeFromForm(Employee actor, JTextField[] fields) {
    try {
        // 1. Permission & Name Validation
        if (!(actor instanceof IAdminOperations)) {
            JOptionPane.showMessageDialog(null, "Access Denied.");
            return false;
        }
        if (fields[1].getText().trim().isEmpty() || fields[2].getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Last Name and First Name are required.");
            return false;
        }

        // 2. Date Validation (The fix for PickDate/Manual Input)
        String bdayText = fields[3].getText().trim();
        if (bdayText.isEmpty() || bdayText.equals("MM/dd/yyyy")) {
            JOptionPane.showMessageDialog(null, "Please select or enter a valid Birthday (MM/dd/yyyy).");
            return false;
        }

        LocalDate birthday;
        try {
            DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            if (bdayText.contains("-")) {
                birthday = LocalDate.parse(bdayText); // Handles yyyy-MM-dd
            } else {
                birthday = LocalDate.parse(bdayText, csvFormatter); // Handles MM/dd/yyyy
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Birthday format error. Please use MM/dd/yyyy.");
            return false;
        }

        // 3. Financial Validation (Stripping Decimals & Enforcing 4-digit Rules)
        long basic = (long) parseDouble(fields[13].getText());
        long rice = (long) parseDouble(fields[14].getText());
        long phone = (long) parseDouble(fields[15].getText());
        long cloth = (long) parseDouble(fields[16].getText());

        // Business Rule: Rice and Phone must be 4 digits (e.g., 1000-9999) 
        // OR as you requested: Phone between 500 and 5000
        if (rice < 1000 || rice > 9999) {
            JOptionPane.showMessageDialog(null, "Rice Subsidy must be a 4-digit amount.");
            return false;
        }
        if (phone < 500 || phone > 5000) {
            JOptionPane.showMessageDialog(null, "Phone Allowance must be between 500 and 5,000.");
            return false;
        }

        // 4. Pag-ibig Validation (12 Digits)
        String pagibigText = fields[9].getText().trim();
        if (pagibigText.length() != 12 || !pagibigText.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Pag-ibig number must be exactly 12 digits.");
            return false;
        }

        // 5. Map validated data to the Model
        Employee emp = new RegularStaff();
        emp.setEmpNo(Integer.parseInt(fields[0].getText().trim()));
        emp.setLastName(fields[1].getText().trim());
        emp.setFirstName(fields[2].getText().trim());
        emp.setBirthday(birthday);
        emp.setAddress(fields[4].getText().trim());
        emp.setPhone(fields[5].getText().trim());
        emp.setSss(fields[6].getText().trim());
        emp.setPhilhealth(fields[7].getText().trim());
        emp.setTin(fields[8].getText().trim());
        emp.setPagibig(pagibigText);
        emp.setStatus(fields[10].getText().trim());
        emp.setPosition(fields[11].getText().trim());
        emp.setSupervisor(fields[12].getText().trim());

        // 6. Final Calculations (Round to whole numbers for 'ichura')
        emp.setBasicSalary((double) basic);
        emp.setRiceSubsidy((double) rice);
        emp.setPhoneAllowance((double) phone);
        emp.setClothingAllowance((double) cloth);
        
        emp.setGrossRate((double) (basic + rice + phone + cloth));
        emp.setHourlyRate((double) Math.round((double) basic / 21 / 8));

        // 7. Push clean data to DAO
        return employeeDao.update(emp);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
        return false;
    }
}


public boolean saveOrUpdateFromUI(Employee actor, JTextField[] uiFields, boolean isNewHire) {
    try {
        // 1. BUSINESS RULE: Check permissions through the Service
        if (!(actor instanceof IAdminOperations)) {
            JOptionPane.showMessageDialog(null, "Access Denied: You do not have permission to modify employee records.");
            return false;
        }

        // 2. ID VALIDATION (Numbers only, specific lengths)
        String sssRaw = uiFields[6].getText().replaceAll("[^\\d]", "");
        String philRaw = uiFields[7].getText().replaceAll("[^\\d]", "");
        String tinRaw = uiFields[8].getText().replaceAll("[^\\d]", "");
        String pagibigRaw = uiFields[9].getText().replaceAll("[^\\d]", "");

        // Business Rule: Validate required lengths
        if (pagibigRaw.length() != 12 || philRaw.length() != 12) {
            JOptionPane.showMessageDialog(null, "Pag-ibig and Philhealth must be exactly 12 digits.");
            return false;
        }

        // 3. FINANCIAL NORMALIZATION (Strip decimals to save as whole numbers)
        long basic = Long.parseLong(uiFields[13].getText().replaceAll("[^\\d]", ""));
        long rice = Long.parseLong(uiFields[14].getText().replaceAll("[^\\d]", ""));
        long phone = Long.parseLong(uiFields[15].getText().replaceAll("[^\\d]", ""));
        long cloth = Long.parseLong(uiFields[16].getText().replaceAll("[^\\d]", ""));

        // 4. MODEL MAPPING
        Employee emp = new RegularStaff();
        
        // Handle Employee Number
        emp.setEmpNo(isNewHire ? employeeDao.getNextAvailableId() : Integer.parseInt(uiFields[0].getText().trim()));
        
        // Basic Info
        emp.setLastName(uiFields[1].getText().trim());
        emp.setFirstName(uiFields[2].getText().trim());
        emp.setAddress(uiFields[4].getText().trim());
        emp.setPhone(uiFields[5].getText().trim());
        
        // Identification Strings (using UI formatted versions for SSS/TIN to keep dashes)
        emp.setSss(uiFields[6].getText()); 
        emp.setPhilhealth(philRaw); 
        emp.setTin(uiFields[8].getText());
        emp.setPagibig(pagibigRaw);

        // Employment Details
        emp.setStatus(uiFields[10].getText().trim());
        emp.setPosition(uiFields[11].getText().trim());
        emp.setSupervisor(uiFields[12].getText().trim());

        // Date Parsing
        String bdayText = uiFields[3].getText().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            // Check if user left placeholder or empty
            if(bdayText.isEmpty() || bdayText.equals("MM/dd/yyyy")) {
                 JOptionPane.showMessageDialog(null, "Please enter or pick a valid Birthday.");
                 return false;
            }
            // Handles both DatePicker (/) and Standard ISO (-) formats
            emp.setBirthday(bdayText.contains("-") ? LocalDate.parse(bdayText) : LocalDate.parse(bdayText, formatter));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Birthday format error. Please use MM/dd/yyyy");
            return false;
        }

        // 5. CALCULATIONS (Protected logic before DAO)
        emp.setBasicSalary((double) basic);
        emp.setRiceSubsidy((double) rice);
        emp.setPhoneAllowance((double) phone);
        emp.setClothingAllowance((double) cloth);
        
        // Semi-monthly Gross calculation
        double totalGross = (double) (basic + rice + phone + cloth);
        emp.setGrossRate(totalGross);
        
        // Hourly rate calculation based on 21 days, 8 hours
        double hourly = (double) Math.round((double) basic / 21 / 8);
        emp.setHourlyRate(hourly);

        // 6. DAO CALL: Final persistence to CSV
        return isNewHire ? employeeDao.addEmployee(emp) : employeeDao.update(emp);

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Financial error: Please enter whole numbers in salary fields.");
        return false;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
        return false;
    }
}

// Helper methods to keep code clean
private double validateDouble(String input, String fieldName) throws Exception {
    try {
        return Double.parseDouble(input.trim().replace(",", "")); //
    } catch (NumberFormatException e) {
        throw new Exception(fieldName + " must be a valid number."); //
    }
}









private String cleanNull(String input) {
    return (input == null || input.trim().equalsIgnoreCase("null")) ? "" : input.trim(); //
}

private double parseDouble(String input) {
    try {
        return Double.parseDouble(input.trim().replace(",", ""));
    } catch (Exception e) {
        return 0.0;
    }
}
private String parseDate(String input) {
    String date = cleanNull(input);
    return date.isEmpty() ? "01/01/1900" : date; // Default date if missing
}





public boolean[] getTodaysStatus(int empNo) {
    // Business Logic: Determine if user can Check-in or Check-out
    Object[][] todayData = attendanceDao.getAttendanceByMonth(empNo, 
                            LocalDate.now().getMonth().name(), 
                            String.valueOf(LocalDate.now().getYear()));
    
    boolean checkedIn = false;
    boolean checkedOut = false;
    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

    for (Object[] row : todayData) {
        if (row[0].equals(todayStr)) {
            if (!row[1].equals("N/A")) checkedIn = true;
            if (!row[2].equals("N/A")) checkedOut = true;
        }
    }
    // Returns [CanCheckIn, CanCheckOut]
    return new boolean[]{!checkedIn, (checkedIn && !checkedOut)};
}




// Keep ONLY this version in EmployeeManagementService.java
public void recordTimeLog(int empNo, String type) {
    // 1. Fetch names so the DAO doesn't have to guess
    Employee emp = employeeDao.findById(empNo);
    String lName = (emp != null) ? emp.getLastName() : "Unknown";
    String fName = (emp != null) ? emp.getFirstName() : "Unknown";

    // 2. Standardize: convert "Check In" or "In" to "Check-in"
    String action = type.toLowerCase().contains("in") ? "Check-in" : "Check-out";

    // 3. Call the DAO that actually handles the File I/O
    attendanceDao.recordAttendance(empNo, lName, fName, action);
}





public Object[] getEmployeeDetailsForForm(int empId) {
    Employee emp = employeeDao.findById(empId);
    if (emp == null) return null;

    // Business Rule: Format financial data as whole numbers (no decimals) for the UI
    return new Object[] {
        emp.getEmpNo(), 
        emp.getLastName(), 
        emp.getFirstName(), 
        emp.getBirthday(), 
        emp.getAddress(), 
        emp.getPhone(),
        emp.getSss(), 
        emp.getPhilhealth(), 
        emp.getTin(), 
        emp.getPagibig(),
        emp.getStatus(), 
        emp.getPosition(), 
        emp.getSupervisor(),
        formatWholeNumber(emp.getBasicSalary()),      // Index 13
        formatWholeNumber(emp.getRiceSubsidy()),      // Index 14
        formatWholeNumber(emp.getPhoneAllowance()),   // Index 15
        formatWholeNumber(emp.getClothingAllowance()),// Index 16
        formatWholeNumber(emp.getGrossRate()),        // Index 17
        formatWholeNumber(emp.getHourlyRate()),       // Index 18
        emp.getRole()
    };
}

/**
 * Helper to ensure the UI receives a clean string without ".0"
 */
private String formatWholeNumber(double value) {
    return String.format("%.0f", value);
}





// Inside EmployeeManagementService.java
public String[] getSupervisorsForPosition(String position) {
    if (position == null) return new String[]{"N/A"};

    switch (position) {
        case "Chief Operating Officer":
        case "Chief Finance Officer":
        case "Chief Marketing Officer":
        case "Account Manager":
            return new String[]{"Garcia, Manuel III"};

        case "IT Operations and Systems":
        case "HR Manager":
            return new String[]{"Lim, Antonio"};

        case "HR Team Leader":
            return new String[]{"Villanueva, Andrea Mae"};

        case "HR Rank and File":
            return new String[]{"San Jose, Brad"};

        case "Accounting Head":
            return new String[]{"Aquino, Bianca Sofia"};

        case "Payroll Manager":
            return new String[]{"Alvaro, Roderick"};

        case "Payroll Team Leader":
        case "Payroll Rank and File":
            return new String[]{"Salcedo, Anthony"};

        case "Account Team Leader":
            return new String[]{"Romualdez, Fredrick"};

        case "Account Rank and File":
            return new String[]{"Mata, Christian", "De Leon, Selena"};

        // Mapping for the specific positions you requested:
        case "Sales & Marketing":
        case "Supply Chain and Logistics":
        case "Customer Service and Relations":
            return new String[]{"Reyes, Isabella"};

        case "Chief Executive Officer":
            return new String[]{"N/A"};

        default:
            return new String[]{"N/A"};
    }
}


// Inside EmployeeManagementService.java




// Inside EmployeeManagementService.java

// Inside EmployeeManagementService.java

public Object[][] getAttendanceLogs(int empNo, String month, String year) {
    // Pass all three arguments to the DAO as required by your CSVHandler
    return attendanceDao.getAttendanceByMonth(empNo, month, year);
}

// Inside EmployeeManagementService.java

/**
 * FIX 3: Improved Button State Logic
 * This determines if the Check-In or Check-Out buttons should be greyed out.
 */

public boolean[] getButtonStates(int empNo) {
    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    // Get ALL logs to avoid filter issues
    Object[][] data = attendanceDao.getAttendanceByMonth(empNo, "All", "All");

    boolean hasIn = false;
    boolean hasOut = false;

    if (data != null) {
        for (Object[] row : data) {
            String csvDate = row[0].toString().trim();
            if (csvDate.equals(todayStr)) {
                // If it's today, check the In/Out columns
                String timeIn = row[1].toString();
                String timeOut = row[2].toString();

                if (!timeIn.equalsIgnoreCase("N/A") && !timeIn.isEmpty()) {
                    hasIn = true; 
                }
                if (!timeOut.equalsIgnoreCase("N/A") && !timeOut.isEmpty() && !timeOut.equals("00:00")) {
                    hasOut = true;
                }
            }
        }
    }
    // Debug result
    System.out.println("Logic Check -> HasIn: " + hasIn + " | HasOut: " + hasOut);
    
    // Return: [Enable In?, Enable Out?]
    return new boolean[]{!hasIn, (hasIn && !hasOut)};
}

}