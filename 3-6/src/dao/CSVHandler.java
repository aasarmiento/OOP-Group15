package dao;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.*;

public class CSVHandler implements EmployeeDAO {

    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<String, Employee> usernameCache = new HashMap<>();
    
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
        return saveAllToCSV();
    }

   // --- DAO: File Streaming & Data Mapping ---

private void loadAllIntoCache() {
    employeeCache.clear();
    usernameCache.clear();

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
    // 2. Load Login & Upgrade Roles (EmployeeLogin.csv)
    try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] loginData = CSVUtils.splitCSVLine(line);
            if (loginData.length >= 6) {
                try {
                    int id = Integer.parseInt(loginData[0].replace("\uFEFF", "").trim());
                    Employee baseEmp = employeeCache.get(id);
                    
                    if (baseEmp != null) {
                        String roleStr = loginData[5].trim();
                        // CRITICAL: Upgrade the role but KEEP the data
                        Employee upgraded = upgradeEmployeeRole(baseEmp, roleStr);
                        upgraded.setPassword(loginData[4].trim());
                        
                        // Save back to DAO Cache
                        employeeCache.put(id, upgraded);
                        usernameCache.put(loginData[1].trim().toLowerCase(), upgraded);
                    }
                } catch (Exception e) {}
            }
        }
    } catch (IOException e) { System.err.println("Login Error: " + e.getMessage()); }
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

    @Override public boolean deleteEmployee(int id) { 
        employeeCache.remove(id); 
        return saveAllToCSV(); 
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
    @Override public void updateEmployeeStatus(int id, String stat) { update(findById(id)); }
    @Override public void saveNewPassword(int id, String pass) {}
    @Override public List<LeaveRequest> getAllLeaveRequestsList() { return new ArrayList<>(); }

    

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