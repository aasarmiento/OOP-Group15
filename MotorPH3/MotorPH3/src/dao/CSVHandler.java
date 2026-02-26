package dao;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;
import model.*;

public class CSVHandler implements EmployeeDAO {
    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<String, Employee> usernameCache = new HashMap<>();
    
    private static final String EMPLOYEE_DATA_CSV = "resources/MotorPH_EmployeeData.csv";
    private static final String LOGIN_DATA_CSV = "resources/MotorPH_EmployeeLogin.csv";
    private static final String ATTENDANCE_CSV = "resources/MotorPH_AttendanceRecord.csv";
    private static final String LEAVE_FILE = "resources/MotorPH_LeaveRequests.csv";

    
    private static final String FILE_PATH = EMPLOYEE_DATA_CSV;
    private static final String TEMP_PATH = "resources/MotorPH_EmployeeData_temp.csv";
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

    // --- loading ito ---
    private void loadAllIntoCache() {
        employeeCache.clear();
        usernameCache.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length < 19) continue;
                LocalDate birthday = null;
                String bdayStr = clean(data[3]);
                if (!bdayStr.isEmpty()) {
                    try { birthday = LocalDate.parse(bdayStr, dateFormatter); } catch (Exception e) {}
                }
                Employee emp = mapToEmployee(data, birthday);
                employeeCache.put(emp.getEmpNo(), emp);
            }
        } catch (IOException e) { showError("Data File Error: " + e.getMessage()); }

        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] loginData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (loginData.length >= 6) {
                    try {
                        int id = Integer.parseInt(loginData[0].trim());
                        String realUsername = loginData[1].trim().toLowerCase(); 
                        String accessLevel = loginData[5].trim();
                        Employee baseEmp = employeeCache.get(id);
                        if (baseEmp != null) {
                            Employee upgraded = upgradeEmployeeRole(baseEmp, accessLevel);
                            upgraded.setPassword(loginData[4].trim());
                            employeeCache.put(id, upgraded);
                            usernameCache.put(realUsername, upgraded);
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        } catch (IOException e) { System.err.println("Login File Error: " + e.getMessage()); }
    }

    private String formatIdNumber(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) return "0";
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(rawValue.trim().replace("\"", ""));
            return bd.toPlainString();
        } catch (Exception e) {
            return rawValue.trim();
        }
    }

    
private Employee mapToEmployee(String[] data, LocalDate birthday) {
    //  DAO handles the raw data extraction from the "Database" (CSV)
    int id = Integer.parseInt(clean(data[0])); 
    String lastName = clean(data[1]);
    String firstName = clean(data[2]);
    
    //  Extract Currency - Basic Salary is at Index 13
    double basicSalary = parseCurrency(data[13]); 

    // Create the Model object
    Employee emp = new RegularStaff(id, lastName, firstName, birthday, basicSalary);
    
    
    emp.setAddress(clean(data[4]));       // Index 4: Address
    emp.setPhone(clean(data[5]));         // Index 5: Phone
    emp.setSss(clean(data[6]));           // Index 6: SSS
    emp.setPhilhealth(formatLargeId(data[7])); 
    emp.setTin(clean(data[8]));
    emp.setPagibig(formatLargeId(data[9]));
    emp.setStatus(clean(data[10]));
    emp.setPosition(clean(data[11]));
    emp.setSupervisor(clean(data[12]));
    
    // Financials
    emp.setRiceSubsidy(parseCurrency(data[14]));
    emp.setPhoneAllowance(parseCurrency(data[15]));
    emp.setClothingAllowance(parseCurrency(data[16]));
    emp.setGrossRate(parseCurrency(data[17]));
    emp.setHourlyRate(parseCurrency(data[18]));
    
    return emp;
}

    private String formatLargeId(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        try {
            return new java.math.BigDecimal(value.trim().replace("\"", ""))
                       .toPlainString();
        } catch (Exception e) {
            return value.replace("\"", "").trim();
        }
    }

    // --- LEAVE MANAGEMENT ---
    @Override
    public void applyForLeave(int empId, String type, String startDate, String endDate, String reason) {
        File file = new File(LEAVE_FILE);
        boolean exists = file.exists();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LEAVE_FILE, true)))) {
            if (!exists || file.length() == 0) {
                out.println("Employee #,Last Name,First Name,Leave Type,Start Date,End Date,Reason,Status");
            }
            Employee emp = findById(empId);
            String ln = (emp != null) ? emp.getLastName() : "Unknown";
            String fn = (emp != null) ? emp.getFirstName() : "Unknown";
            out.printf("%d,%s,%s,%s,%s,%s,\"%s\",Pending\n", empId, ln, fn, type, startDate, endDate, reason);
        } catch (IOException e) { showError("Leave Save Error: " + e.getMessage()); }
    }

    @Override
    public Object[][] getLeaveStatusByEmpId(int empId) {
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 8 && Integer.parseInt(clean(data[0])) == empId) {
                    records.add(new Object[]{ clean(data[3]), clean(data[4]), clean(data[5]), clean(data[6]), clean(data[7]) });
                }
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
        return records.toArray(new Object[0][]);
    }

    @Override
    public Object[][] getAllLeaveRequests() {
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 8) {
                    records.add(new Object[]{ data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7] });
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return records.toArray(new Object[0][]);
    }

    @Override
    public void updateLeaveStatus(int empId, String startDate, String newStatus) {
        File inputFile = new File(LEAVE_FILE);
        File tempFile = new File("resources/temp_leave.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line = reader.readLine();
            writer.println(line); 
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 8 && clean(data[0]).equals(String.valueOf(empId)) && clean(data[4]).equals(startDate)) {
                    data[7] = newStatus;
                    writer.println(String.join(",", data));
                } else { writer.println(line); }
            }
        } catch (IOException e) { e.printStackTrace(); }
        inputFile.delete();
        tempFile.renameTo(inputFile);
    }

    // --- ATTENDANCE ---
    @Override
    public void recordAttendance(int empId, String type) {
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
        List<String> lines = new ArrayList<>();
        boolean recordUpdated = false;

        try {
            File file = new File(ATTENDANCE_CSV);
            if (!file.exists()) {
                if(file.getParentFile() != null) file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (type.equalsIgnoreCase("Check-out") && data.length >= 2 &&
                        data[0].equals(String.valueOf(empId)) && data[1].equals(date)) {
                        line = data[0] + "," + data[1] + "," + (data.length > 2 ? data[2] : "00:00") + "," + time;
                        recordUpdated = true;
                    }
                    lines.add(line);
                }
            }

            if (type.equalsIgnoreCase("Check-in")) {
                lines.add(empId + "," + date + "," + time + ",00:00");
                recordUpdated = true;
            }

            if (recordUpdated) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(ATTENDANCE_CSV))) {
                    for (String l : lines) pw.println(l);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public Object[][] getAttendanceById(int empId) {
        return getAttendanceByMonth(empId, "All");
    }

    @Override
    public Object[][] getAttendanceByMonth(int empId, String month) {
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 6) {
                    try {
                        String rawId = data[0].replace("\"", "").trim();
                        if (rawId.matches("\\d+") && Integer.parseInt(rawId) == empId) {
                            String dateStr = data[3].replace("\"", "").trim(); 
                            String logIn = data[4].replace("\"", "").trim();
                            String logOut = data[5].replace("\"", "").trim();
                            if (month.equalsIgnoreCase("All") || isMonthMatch(dateStr, month)) {
                                records.add(new Object[]{ dateStr, logIn, logOut });
                            }
                        }
                    } catch (Exception e) { continue; }
                }
            }
        } catch (IOException e) { System.err.println("Attendance File Error: " + e.getMessage()); }
        return records.toArray(new Object[0][]);
    }

    private boolean isMonthMatch(String dateStr, String monthName) {
        if (dateStr == null || dateStr.isEmpty() || monthName.equalsIgnoreCase("All")) return true;
        try {
            String[] parts = dateStr.replace("\"", "").trim().split("/");
            if (parts.length < 3) return false;
            int monthNum = Integer.parseInt(parts[0]);
            String[] months = {"", "January", "February", "March", "April", "May", "June", 
                               "July", "August", "September", "October", "November", "December"};
            return months[monthNum].equalsIgnoreCase(monthName.trim());
        } catch (Exception e) { return false; }
    }

    
    @Override
    public boolean updateEmployee(Employee updatedEmp) {
        
        update(updatedEmp);
        return true; 
    }

   private boolean saveAllToCSV(List<Employee> employees) {
    
    employees.sort(Comparator.comparingInt(Employee::getEmpNo));

    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV)))) {
        // Header
        writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate");
        
        for (Employee e : employees) {
            String bdayStr = (e.getBirthday() != null) ? e.getBirthday().format(dateFormatter) : "";
            
            // Note the \"%s\" for Address to handle commas safely
            writer.printf("%d,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                e.getEmpNo(), 
                e.getLastName(), 
                e.getFirstName(), 
                bdayStr, 
                e.getAddress().replace("\"", ""), // Remove internal quotes to avoid CSV corruption
                e.getPhone(), 
                e.getSss(), 
                e.getPhilhealth(),
                e.getTin(), 
                e.getPagibig(), 
                e.getStatus(), 
                e.getPosition(),
                e.getSupervisor(), 
                e.getBasicSalary(), 
                e.getRiceSubsidy(),
                e.getPhoneAllowance(), 
                e.getClothingAllowance(), 
                e.getGrossRate(), 
                e.getHourlyRate());
        }
        return true;
    } catch (IOException e) {
        showError("File Access Error: Ensure the CSV is not open in Excel.");
        return false;
    }
}

    @Override
    public void update(Employee emp) {
        
        employeeCache.put(emp.getEmpNo(), emp);
        
       
        for (String userKey : usernameCache.keySet()) {
            if (usernameCache.get(userKey).getEmpNo() == emp.getEmpNo()) {
                usernameCache.put(userKey, emp);
            }
        }

       
        List<Employee> allEmployees = new ArrayList<>(employeeCache.values());
        if (saveAllToCSV(allEmployees)) {
            System.out.println("CSV Update Successful for Employee: " + emp.getEmpNo());
        } else {
            showError("Could not save updates to CSV. Check if file is open in Excel.");
        }
    }

    private Employee upgradeEmployeeRole(Employee o, String level) {
        Employee upgraded;
        if (level.equalsIgnoreCase("Admin")) {
            upgraded = new Admin(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
        } else if (level.equalsIgnoreCase("HR")) {
            upgraded = new HRStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
        } else {
            return o; 
        }

        upgraded.setAddress(o.getAddress());
        upgraded.setPhone(o.getPhone());
        upgraded.setSss(o.getSss());
        upgraded.setPhilhealth(o.getPhilhealth());
        upgraded.setTin(o.getTin());
        upgraded.setPagibig(o.getPagibig());
        upgraded.setStatus(o.getStatus());
        upgraded.setPosition(o.getPosition());
        upgraded.setSupervisor(o.getSupervisor());
        upgraded.setRiceSubsidy(o.getRiceSubsidy());
        upgraded.setPhoneAllowance(o.getPhoneAllowance());
        upgraded.setClothingAllowance(o.getClothingAllowance());
        upgraded.setGrossRate(o.getGrossRate());
        upgraded.setHourlyRate(o.getHourlyRate());
        
        return upgraded;
    }

    @Override
    public boolean addEmployee(Employee newEmp) {
        List<Employee> allEmployees = getAll(); 
        allEmployees.add(newEmp); 
        boolean success = saveAllToCSV(allEmployees);
        if (success) loadAllIntoCache(); // Refresh cache
        return success;
    }

    @Override
    public boolean deleteEmployee(int id) {
        List<Employee> allEmployees = getAll(); 
        boolean found = allEmployees.removeIf(e -> e.getEmpNo() == id);
        if (found) {
            boolean success = saveAllToCSV(allEmployees);
            if (success) loadAllIntoCache();
            return success;
        }
        return false;
    }

    @Override public Employee findById(int id) { return employeeCache.get(id); }
    @Override public Employee findByUsername(String u) { return usernameCache.get(u.trim().toLowerCase()); }
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }

    private String clean(String s) { return (s == null) ? "" : s.trim().replace("\"", ""); }

private double parseCurrency(String s) {
    String c = clean(s).replace(",", ""); 
    if (c.isEmpty() || c.equalsIgnoreCase("null")) return 0.0;
    try {
        return Double.parseDouble(c);
    } catch (NumberFormatException e) {
        // If it accidentally hits a name like "Manuel III", return 0.0 and keep going
        System.err.println("DAO Error: Expected number but found text: " + s);
        return 0.0; 
    }
}
    private void showError(String m) { JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE); }
}