package dao;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.*;

public class CSVHandler implements EmployeeDAO {

    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<String, Employee> usernameCache = new HashMap<>();
    private final Map<Integer, String> usernameByIdCache = new HashMap<>();
    private final Map<Integer, Integer> failedAttemptsCache = new HashMap<>();
    private final Map<Integer, Boolean> lockedCache = new HashMap<>();
    private final Map<Integer, Boolean> mustChangePasswordCache = new HashMap<>();
    
    private static final String EMPLOYEE_DATA_CSV = "resources/MotorPH_EmployeeData.csv";
    private static final String LOGIN_DATA_CSV = "resources/MotorPH_EmployeeLogin.csv";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

    @Override
    public boolean addEmployee(Employee emp) {
    if (emp == null) return false;

    employeeCache.put(emp.getEmpNo(), emp);

    usernameByIdCache.put(emp.getEmpNo(), buildUsernameFromEmployee(emp));
    failedAttemptsCache.put(emp.getEmpNo(), 0);
    lockedCache.put(emp.getEmpNo(), false);
    mustChangePasswordCache.put(emp.getEmpNo(), false);

    if (emp.getPassword() == null || emp.getPassword().trim().isEmpty()) {
        emp.setPassword("1234");
    }

    boolean employeeSaved = saveAllToCSV();
    boolean loginSaved = saveLoginDataToCSV();
    return employeeSaved && loginSaved;
    }

   // --- DAO: File Streaming & Data Mapping ---

    private void loadAllIntoCache() {
        employeeCache.clear();
        usernameCache.clear();
        usernameByIdCache.clear();
        failedAttemptsCache.clear();
        lockedCache.clear();
        mustChangePasswordCache.clear();

    try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
        br.readLine(); 
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue; 
            
            // FIX: Replace CSVUtils.splitCSVLine(line) with the regex split
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            
            if (data.length > 0 && data[0] != null) data[0] = data[0].replace("\uFEFF", "").trim();
            if (data.length < 19) continue;
            
            LocalDate birthday = null;
            String bdayStr = CSVUtils.clean(data[3]);
            try { if (!bdayStr.isEmpty()) birthday = LocalDate.parse(bdayStr, dateFormatter); } catch (Exception e) {}

            Employee emp = mapToEmployee(data, birthday);
            if (emp != null) employeeCache.put(emp.getEmpNo(), emp);
        }
    } catch (IOException e) { System.err.println("DAO Error: " + e.getMessage()); }
    // 2. Load Login & Upgrade Roles (EmployeeLogin.csv) - updated this for the IT ticketing functionality
    try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
        br.readLine(); 
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] loginData = CSVUtils.splitCSVLine(line);

            if (loginData.length >= 6) {
                try {
                    int id = Integer.parseInt(loginData[0].replace("\uFEFF", "").trim());
                    String username = loginData[1].trim();
                    String password = loginData[4].trim();
                    String accessLevel = loginData[5].trim();

                    int failedAttempts = (loginData.length >= 7) ? parseIntSafe(loginData[6]) : 0;
                    boolean locked = (loginData.length >= 8) && Boolean.parseBoolean(loginData[7].trim());
                    boolean mustChange = (loginData.length >= 9) && Boolean.parseBoolean(loginData[8].trim());

                    usernameByIdCache.put(id, username);
                    failedAttemptsCache.put(id, failedAttempts);
                    lockedCache.put(id, locked);
                    mustChangePasswordCache.put(id, mustChange);

                    Employee baseEmp = employeeCache.get(id);
                    if (baseEmp != null) {
                        Employee upgraded = upgradeEmployeeRole(baseEmp, accessLevel);
                        upgraded.setPassword(password);

                        employeeCache.put(id, upgraded);
                        usernameCache.put(username.toLowerCase(), upgraded);
                    }
                } catch (Exception e) {
                    System.err.println("Login row parse error: " + e.getMessage());
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Login Error: " + e.getMessage());
    }
}

private Employee mapToEmployee(String[] data, LocalDate birthday) {
    try {
        int id = Integer.parseInt(CSVUtils.clean(data[0])); 
        
        // --- FIXED INDICES TO MATCH CSV ---
        // Column 13: Basic Salary
        double basicSalary = CSVUtils.parseCurrency(data[13]); 
        
        Employee emp = new RegularStaff(id, CSVUtils.clean(data[1]), CSVUtils.clean(data[2]), birthday, basicSalary);
        
        emp.setAddress(CSVUtils.clean(data[4]));
        emp.setPhone(CSVUtils.clean(data[5]));
        emp.setSss(CSVUtils.clean(data[6]));
        emp.setPhilhealth(CSVUtils.clean(data[7]));
        emp.setTin(CSVUtils.clean(data[8]));
        emp.setPagibig(CSVUtils.clean(data[9]));
        emp.setStatus(CSVUtils.clean(data[10]));
        emp.setPosition(CSVUtils.clean(data[11]));    // Column 11: Position
        emp.setSupervisor(CSVUtils.clean(data[12]));  // Column 12: Supervisor
        // Explicitly set it again to be safe
        emp.setBasicSalary(basicSalary);
        
        // Financial Allowances
        emp.setRiceSubsidy(CSVUtils.parseCurrency(data[14]));      // Column 14
        emp.setPhoneAllowance(CSVUtils.parseCurrency(data[15]));   // Column 15
        emp.setClothingAllowance(CSVUtils.parseCurrency(data[16]));// Column 16
       
        // Rates
        emp.setGrossRate(CSVUtils.parseCurrency(data[17]));        // Column 17
        emp.setHourlyRate(CSVUtils.parseCurrency(data[18]));       // Column 18

        return emp;
    } catch (Exception e) { 
        System.err.println("DAO Mapping error for ID " + data[0] + ": " + e.getMessage());
        return null; 
    }
}

private void copyFinancials(Employee from, Employee to) {
    // Ensure all data is transferred to the new specialized role object
    to.setBasicSalary(from.getBasicSalary());
    to.setRiceSubsidy(from.getRiceSubsidy());
    to.setPhoneAllowance(from.getPhoneAllowance());
    to.setClothingAllowance(from.getClothingAllowance());
    to.setGrossRate(from.getGrossRate());
    to.setHourlyRate(from.getHourlyRate());
    to.setAddress(from.getAddress());
    to.setPhone(from.getPhone());
    to.setSss(from.getSss());
    to.setPhilhealth(from.getPhilhealth());
    to.setTin(from.getTin());
    to.setPagibig(from.getPagibig());
    to.setStatus(from.getStatus());
    to.setPosition(from.getPosition());
    to.setSupervisor(from.getSupervisor());
}
    private Employee upgradeEmployeeRole(Employee o, String roleName) {
        Role role = Role.fromString(roleName);
        Employee upgraded = switch (role) {
            case ADMIN -> new Admin(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case HR_STAFF -> new HRStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case IT_STAFF -> new ITStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case ACCOUNTING -> new AccountingStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            default -> o;
        };
        upgraded.setRole(role);
        copyFinancials(o, upgraded);
        return upgraded;
    }

    
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }
    @Override public Employee findById(int id) { return employeeCache.get(id); }
    @Override public Employee findByUsername(String u) { return usernameCache.get(u.trim().toLowerCase()); }

    @Override
    public boolean update(Employee emp) { 
        employeeCache.put(emp.getEmpNo(), emp); 
        return saveAllToCSV(); 
    }

    //updated for it functionality for new user login row
    @Override
    public boolean deleteEmployee(int id) {
    employeeCache.remove(id);
    usernameByIdCache.remove(id);
    failedAttemptsCache.remove(id);
    lockedCache.remove(id);
    mustChangePasswordCache.remove(id);

    boolean employeeSaved = saveAllToCSV();
    boolean loginSaved = saveLoginDataToCSV();
    return employeeSaved && loginSaved;
    }

    private boolean saveAllToCSV() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV)))) {
            writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate,Role");
            for (Employee e : employeeCache.values()) {
                String bdayStr = (e.getBirthday() != null) ? e.getBirthday().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
                String addr = (e.getAddress() != null) ? e.getAddress() : "";
                String superv = (e.getSupervisor() != null) ? e.getSupervisor() : "N/A";
                
                String safeAddress = addr.contains(",") ? "\"" + addr + "\"" : addr;
                String safeSupervisor = superv.contains(",") ? "\"" + superv + "\"" : superv;
                
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s%n",
                    e.getEmpNo(), e.getLastName(), e.getFirstName(), bdayStr,
                    safeAddress, e.getPhone(), e.getSss(), e.getPhilhealth(), e.getTin(), e.getPagibig(),
                    e.getStatus(), e.getPosition(), safeSupervisor, 
                    e.getBasicSalary(), e.getRiceSubsidy(), e.getPhoneAllowance(), 
                    e.getClothingAllowance(), e.getGrossRate(), e.getHourlyRate(), e.getRole());
            }
            return true;
        } catch (IOException e) { return false; }
    }

    private boolean saveLoginDataToCSV() {
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(LOGIN_DATA_CSV)))) {
        writer.println("EmployeeNo,Username,Last Name,First Name,Password,AccessLevel,FailedAttempts,Locked,MustChangePassword");

        List<Employee> employees = new ArrayList<>(employeeCache.values());
        employees.sort(Comparator.comparingInt(Employee::getEmpNo));

        for (Employee e : employees) {
            int id = e.getEmpNo();
            String username = usernameByIdCache.getOrDefault(id, buildUsernameFromEmployee(e));
            String password = (e.getPassword() == null || e.getPassword().trim().isEmpty())
                    ? "1234"
                    : e.getPassword().trim();

            int failedAttempts = failedAttemptsCache.getOrDefault(id, 0);
            boolean locked = lockedCache.getOrDefault(id, false);
            boolean mustChange = mustChangePasswordCache.getOrDefault(id, false);
            String accessLevel = roleToAccessLevel(e.getRole());

            writer.printf("%d,%s,%s,%s,%s,%s,%d,%s,%s%n",
                    id,
                    safeCsvValue(username),
                    safeCsvValue(e.getLastName()),
                    safeCsvValue(e.getFirstName()),
                    safeCsvValue(password),
                    safeCsvValue(accessLevel),
                    failedAttempts,
                    locked,
                    mustChange);
        }
        return true;
    } catch (IOException e) {
        System.err.println("Error saving login CSV: " + e.getMessage());
        return false;
    }
}

private String roleToAccessLevel(Role role) {
    if (role == null) return "Regular";
    switch (role) {
        case ADMIN:
            return "Admin";
        case HR_STAFF:
            return "Hr";
        case IT_STAFF:
            return "IT";
        case ACCOUNTING:
            return "Accounting";
        default:
            return "Regular";
    }
}

private String buildUsernameFromEmployee(Employee e) {
    if (e == null) return "";
    String first = (e.getFirstName() == null) ? "" : e.getFirstName().trim();
    String last = (e.getLastName() == null) ? "" : e.getLastName().trim();

    if (first.isEmpty() && last.isEmpty()) return "";
    if (first.isEmpty()) return last.replace(" ", "");
    return first.substring(0, 1).toUpperCase() + last.replace(" ", "");
}

private String safeCsvValue(String value) {
    if (value == null) return "";
    return value.replace(",", " ");
}

private int parseIntSafe(String value) {
    try {
        return Integer.parseInt(value.trim());
    } catch (Exception e) {
        return 0;
    }
}

    private boolean saveLoginDataToCSV() {
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(LOGIN_DATA_CSV)))) {
        writer.println("EmployeeNo,Username,Last Name,First Name,Password,AccessLevel,FailedAttempts,Locked");

        List<Employee> employees = new ArrayList<>(employeeCache.values());
        employees.sort(Comparator.comparingInt(Employee::getEmpNo));

        for (Employee e : employees) {
            int id = e.getEmpNo();
            String username = usernameByIdCache.getOrDefault(id, buildUsernameFromEmployee(e));
            String password = (e.getPassword() == null || e.getPassword().trim().isEmpty())
                    ? "1234"
                    : e.getPassword().trim();

            int failedAttempts = failedAttemptsCache.getOrDefault(id, 0);
            boolean locked = lockedCache.getOrDefault(id, false);
            String accessLevel = roleToAccessLevel(e.getRole());

            writer.printf("%d,%s,%s,%s,%s,%s,%d,%s%n",
                    id,
                    username,
                    safeCsvValue(e.getLastName()),
                    safeCsvValue(e.getFirstName()),
                    safeCsvValue(password),
                    safeCsvValue(accessLevel),
                    failedAttempts,
                    locked);
        }
        return true;
    } catch (IOException e) {
        System.err.println("Error saving login CSV: " + e.getMessage());
        return false;
    }
}

private String roleToAccessLevel(Role role) {
    if (role == null) return "Regular";
    switch (role) {
        case ADMIN:
            return "Admin";
        case HR_STAFF:
            return "Hr";
        case IT_STAFF:
            return "IT";
        case ACCOUNTING:
            return "Accounting";
        default:
            return "Regular";
    }
}

// added this to help with with the ticketing / helper method
private String buildUsernameFromEmployee(Employee e) {
    if (e == null) return "";
    String first = (e.getFirstName() == null) ? "" : e.getFirstName().trim();
    String last = (e.getLastName() == null) ? "" : e.getLastName().trim();

    if (first.isEmpty() && last.isEmpty()) return "";
    if (first.isEmpty()) return last.replace(" ", "");
    return first.substring(0, 1).toUpperCase() + last.replace(" ", "");
}

private String safeCsvValue(String value) {
    if (value == null) return "";
    return value.replace(",", " ");
}

private int parseIntSafe(String value) {
    try {
        return Integer.parseInt(value.trim());
    } catch (Exception e) {
        return 0;
    }
}

    public Object[][] getAttendanceByMonth(int empNo, String month) {
    return new Object[0][3]; 
}


@Override
public Employee getById(int id) {
    // This assumes you already have a method named findById 
    // or a list named 'employees'
    return findById(id); 
}



@Override
public boolean create(Employee emp) {
    if (emp == null) return false;

    try {
        // 1. Add to the local cache so the UI updates immediately
        employeeCache.put(emp.getEmpNo(), emp);
        
        // 2. Add to the username cache if a username/password exists
        // (Assuming you might need this for login later)
        if (emp.getFirstName() != null) {
            usernameCache.put(emp.getFirstName().toLowerCase(), emp);
        }

        // 3. Use your existing private method to write the entire cache to the CSV
        // This returns a boolean, which matches your Interface requirement!
        return saveAllToCSV();

    } catch (Exception e) {
        System.err.println("Error creating employee: " + e.getMessage());
        return false;
    }
}





@Override public void applyForLeave(int id, String type, String start, String end, String reason) {}
@Override public Object[][] getLeaveStatusByEmpId(int empId) { return new Object[0][0]; }
@Override public Object[][] getAllLeaveRequests() { return new Object[0][0]; }
@Override public void updateLeaveStatus(String reqId, String stat) {}
@Override public List<LeaveRequest> getAllLeaveRequestsList() { return new ArrayList<>(); }

    //updated and added additonal methods for IT ticketing
@Override
public void updateEmployeeStatus(int id, String stat) {
    Employee emp = findById(id);
    if (emp != null) {
        emp.setStatus(stat);
        update(emp);
    }
}

@Override
public void saveNewPassword(int id, String pass) {
    Employee emp = findById(id);
    if (emp != null) {
        emp.setPassword(pass);
        employeeCache.put(id, emp);
        saveLoginDataToCSV();
    }
}

@Override
public int getFailedAttempts(int id) {
    return failedAttemptsCache.getOrDefault(id, 0);
}

@Override
public boolean isLocked(int id) {
    return lockedCache.getOrDefault(id, false);
}

@Override
public void updateLoginState(int id, int failedAttempts, boolean locked) {
    failedAttemptsCache.put(id, failedAttempts);
    lockedCache.put(id, locked);
    saveLoginDataToCSV();
}

@Override
public void resetLoginState(int id) {
    failedAttemptsCache.put(id, 0);
    lockedCache.put(id, false);
    saveLoginDataToCSV();
}

@Override
public boolean mustChangePassword(int id) {
    return mustChangePasswordCache.getOrDefault(id, false);
}

@Override
public void setMustChangePassword(int id, boolean required) {
    mustChangePasswordCache.put(id, required);
    saveLoginDataToCSV();
}

    

// Correct DAO Implementation
@Override
public int getLastEmployeeNumber() {
    if (employeeCache.isEmpty()) {
        return 10000;
    }
    return Collections.max(employeeCache.keySet());
}

@Override
public int getNextAvailableId() {
    // In the DAO, we just return the next number based on the cache
    return getLastEmployeeNumber() + 1;
}


}