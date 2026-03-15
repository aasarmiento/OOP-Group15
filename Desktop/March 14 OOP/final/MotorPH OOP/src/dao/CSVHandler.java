package dao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.*;

public class CSVHandler implements EmployeeDAO {

    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<String, Employee> usernameCache = new HashMap<>();
    
    private static final String EMPLOYEE_DATA_CSV = "./resources/MotorPH_EmployeeData.csv";
    private static final String LOGIN_DATA_CSV = "./resources/MotorPH_EmployeeLogin.csv";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

  
   public List<Employee> getAllEmployees() {
    return getAll(); 
}

    
    public void updateEmployeeFile(List<Employee> employees) {
        
        for (Employee emp : employees) {
            employeeCache.put(emp.getEmpNo(), emp);
        }
        saveAllToCSV();
    }

    
    @Override
    public void unlockAccount(int employeeNo) {
        Employee emp = findById(employeeNo);
        if (emp != null) {
           
            emp.setStatus("Active"); 
            saveAllToCSV();
        }
    }

  

    @Override
    public boolean addEmployee(Employee emp) {
        if (emp == null) return false;
        employeeCache.put(emp.getEmpNo(), emp);
        return saveAllToCSV();
    }

    private void loadAllIntoCache() {
        employeeCache.clear();
        usernameCache.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length > 0 && data[0] != null) {
                    data[0] = data[0].replace("\uFEFF", "").trim();
                }
                if (data.length < 19) continue;
                
                String bdayStr = (data[3] != null) ? data[3].trim() : "";
                LocalDate birthday = null;

                if (!bdayStr.isEmpty()) {
                    try {
                        if (bdayStr.contains("-")) {
                            birthday = LocalDate.parse(bdayStr); 
                        } else if (bdayStr.contains("/")) {
                            birthday = LocalDate.parse(bdayStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        }
                    } catch (Exception e) {
                        birthday = LocalDate.of(1900, 1, 1); 
                    }
                }

                Employee emp = mapToEmployee(data, birthday);
                if (emp != null) {
                    if (data.length > 20) {
                        emp.setGender(data[20].trim());
                    } else {
                        emp.setGender("Not Set"); 
                    }
                    employeeCache.put(emp.getEmpNo(), emp);
                }
            }
        } catch (IOException e) { 
            System.err.println("DAO Error: " + e.getMessage()); 
        }

        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] loginData = line.split(","); 
                if (loginData.length >= 6) {
                    try {
                        int id = Integer.parseInt(loginData[0].replace("\uFEFF", "").trim());
                        Employee baseEmp = employeeCache.get(id);
                        if (baseEmp != null) {
                            String roleStr = loginData[5].trim();
                            Employee upgraded = upgradeEmployeeRole(baseEmp, roleStr);
                            upgraded.setPassword(loginData[4].trim());
                            employeeCache.put(id, upgraded);
                            usernameCache.put(loginData[1].trim(), upgraded);
                        }
                    } catch (Exception e) { }
                }
            }
        } catch (IOException e) { 
            System.err.println("Login Error: " + e.getMessage()); 
        }
    }

    private Employee mapToEmployee(String[] data, LocalDate birthday) {
        try {
            int id = Integer.parseInt(CSVUtils.clean(data[0])); 
            double basicSalary = CSVUtils.parseCurrency(data[13]); 
            Employee emp = new RegularStaff(id, CSVUtils.clean(data[1]), CSVUtils.clean(data[2]), birthday, basicSalary);
            emp.setAddress(CSVUtils.clean(data[4]));
            emp.setPhone(CSVUtils.clean(data[5]));
            emp.setSss(CSVUtils.clean(data[6]));
            emp.setPhilhealth(CSVUtils.clean(data[7]));
            emp.setTin(CSVUtils.clean(data[8]));
            emp.setPagibig(CSVUtils.clean(data[9]));
            emp.setStatus(CSVUtils.clean(data[10]));
            emp.setPosition(CSVUtils.clean(data[11]));    
            emp.setSupervisor(CSVUtils.clean(data[12]));  
            emp.setRiceSubsidy(CSVUtils.parseCurrency(data[14]));      
            emp.setPhoneAllowance(CSVUtils.parseCurrency(data[15]));   
            emp.setClothingAllowance(CSVUtils.parseCurrency(data[16]));
            emp.setGrossRate(CSVUtils.parseCurrency(data[17]));        
            emp.setHourlyRate(CSVUtils.parseCurrency(data[18]));       

            if (data.length > 19) {
                try { emp.setRole(Role.valueOf(data[19].toUpperCase().trim())); } 
                catch(Exception e) { emp.setRole(Role.REGULAR_STAFF); }
            }
            if (data.length > 20) {
                String genderVal = CSVUtils.clean(data[20]);
                emp.setGender(genderVal.isEmpty() ? "Not Set" : genderVal);
            } else {
                emp.setGender("Not Set");
            }
            return emp;
        } catch (Exception e) { return null; }
    }

    private void copyFinancials(Employee from, Employee to) {
        to.setBasicSalary(from.getBasicSalary());
        to.setRiceSubsidy(from.getRiceSubsidy());
        to.setPhoneAllowance(from.getPhoneAllowance());
        to.setClothingAllowance(from.getClothingAllowance());
        to.setGrossRate(from.getGrossRate());
        to.setHourlyRate(from.getHourlyRate());
        to.setGender(from.getGender());
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
            case ADMIN -> new Admin(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
            case HR_STAFF -> new HRStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
            case IT_STAFF -> new ITStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
            case ACCOUNTING -> new AccountingStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
            default -> o;
        };
        upgraded.setRole(role);
        copyFinancials(o, upgraded);
        return upgraded;
    }
    
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }
    @Override public Employee findById(int id) { return employeeCache.get(id); }
    @Override public Employee findByUsername(String u) { return usernameCache.get(u.trim()); }

    @Override
    public boolean update(Employee emp) { 
        employeeCache.put(emp.getEmpNo(), emp); 
        return saveAllToCSV(); 
    }

   @Override 
public boolean deleteEmployee(int id) { 
    Employee removed = employeeCache.remove(id); 
    
    if (removed != null) {
        usernameCache.entrySet().removeIf(entry -> entry.getValue().getEmpNo() == id);
    }

    boolean empSaved = saveAllToCSV(); 
    boolean loginSaved = saveLoginsToCSV(); // eto lang pala un htys
    
    return empSaved && loginSaved; 
}

private boolean saveLoginsToCSV() {
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(LOGIN_DATA_CSV)))) {
        writer.println("Employee #,Username,First Name,Last Name,Password,Role");
        
        for (Employee e : employeeCache.values()) {
            String password = (e.getPassword() != null) ? e.getPassword() : "1234";
            String roleStr = (e.getRole() != null) ? e.getRole().name() : "REGULAR_STAFF";
            
            String username = getSavedUsername(e);

            writer.printf("%d,%s,%s,%s,%s,%s%n",
                e.getEmpNo(),
                username,
                e.getFirstName(),
                e.getLastName(),
                password,
                roleStr
            );
        }
        writer.flush();
        return true;
    } catch (IOException e) {
        System.err.println("Error syncing login CSV: " + e.getMessage());
        return false;
    }
}

    private boolean saveAllToCSV() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV)))) {
            writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate,Role,Gender");
            for (Employee e : employeeCache.values()) {
                String bdayStr = (e.getBirthday() != null) ? e.getBirthday().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
                String addr = (e.getAddress() != null) ? e.getAddress() : "";
                String superv = (e.getSupervisor() != null) ? e.getSupervisor() : "N/A";
                String gender = (e.getGender() != null && !e.getGender().trim().isEmpty()) ? e.getGender() : "Not Set";
                String safeAddress = addr.contains(",") ? "\"" + addr + "\"" : addr;
                String safeSupervisor = superv.contains(",") ? "\"" + superv + "\"" : superv;
                String roleStr = (e.getRole() != null) ? e.getRole().name() : "REGULAR_STAFF";

                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s%n",
                    e.getEmpNo(), e.getLastName(), e.getFirstName(), bdayStr, safeAddress, e.getPhone(),
                    e.getSss(), e.getPhilhealth(), e.getTin(), e.getPagibig(), e.getStatus(), e.getPosition(),
                    safeSupervisor, e.getBasicSalary(), e.getRiceSubsidy(), e.getPhoneAllowance(),
                    e.getClothingAllowance(), e.getGrossRate(), e.getHourlyRate(), roleStr, gender
                );
            }
            writer.flush();
            return true;
        } catch (IOException e) { return false; }
    }

    @Override public Employee getById(int id) { return findById(id); }

    @Override
    public boolean create(Employee emp) {
        if (emp == null) return false;
        employeeCache.put(emp.getEmpNo(), emp);
        if (emp.getFirstName() != null) {
            usernameCache.put(emp.getFirstName().toLowerCase(), emp);
        }
        return saveAllToCSV();
    }

    @Override public void applyForLeave(int id, String type, String start, String end, String reason) {}
    @Override public Object[][] getLeaveStatusByEmpId(int empId) { return new Object[0][0]; }
    @Override public Object[][] getAllLeaveRequests() { return new Object[0][0]; }
    @Override public void updateLeaveStatus(String reqId, String stat) {}
    @Override
    public void updateEmployeeStatus(int id, String stat) {
        Employee emp = findById(id);
        if (emp != null) {
            emp.setStatus(stat);
            saveAllToCSV();
        }
    }
    @Override
    public void saveNewPassword(int id, String pass) {
        Employee emp = findById(id);
        if (emp != null) {
            emp.setPassword(pass);
            saveLoginsToCSV();
        }
    }
    @Override public List<LeaveRequest> getAllLeaveRequestsList() { return new ArrayList<>(); }

    @Override
    public int getLastEmployeeNumber() {
        if (employeeCache.isEmpty()) return 10000;
        return Collections.max(employeeCache.keySet());
    }

    @Override
    public int getNextAvailableId() {
        return getLastEmployeeNumber() + 1;
    }

    @Override
    public Object[][] getAttendanceByMonth(int empNo, String month, String year) {
        List<Object[]> matchingLogs = new ArrayList<>();
        String attendanceFile = "resources/MotorPH_AttendanceRecord.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(attendanceFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) continue;
                int id = Integer.parseInt(data[0].replace("\uFEFF", "").trim());
                if (id != empNo) continue;
                String[] dateParts = data[3].trim().split("/");
                if (dateParts.length < 3) continue;
                if ((convertMonthToNumber(month).equals("00") || dateParts[0].equals(convertMonthToNumber(month))) && 
                    (year.equals("All") || dateParts[2].equals(year))) {
                    matchingLogs.add(data); 
                }
            }
        } catch (IOException e) { }
        return matchingLogs.toArray(new Object[0][0]);
    }

    private String convertMonthToNumber(String monthName) {
        return switch (monthName.toLowerCase()) {
            case "january" -> "01"; case "february" -> "02"; case "march" -> "03";
            case "april" -> "04"; case "may" -> "05"; case "june" -> "06";
            case "july" -> "07"; case "august" -> "08"; case "september" -> "09";
            case "october" -> "10"; case "november" -> "11"; case "december" -> "12";
            default -> "00";
        };
    }

    @Override
    public File getEmployeePhotoFile(int empId) {
        String baseDir = "resources/profile_pics/";
        
        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();

        File pngFile = new File(baseDir + empId + ".png");
        if (pngFile.exists()) return pngFile;

        File jpgFile = new File(baseDir + empId + ".jpg");
        if (jpgFile.exists()) return jpgFile;

        File jpegFile = new File(baseDir + empId + ".jpeg");
        if (jpegFile.exists()) return jpegFile;

        File defaultFile = new File(baseDir + "default.png");
        return defaultFile.exists() ? defaultFile : null;
    }

  
@Override
  public void saveProfilePicture(int empNo, File sourceFile) throws IOException {
   
    File storageDir = new File("resources/profile_pics/");
    if (!storageDir.exists()) storageDir.mkdirs();

    
    String extension = getFileExtension(sourceFile.getName());
    File destination = new File(storageDir, empNo + extension);

    
    Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
}

private String getFileExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf("."));
}

public boolean createLoginCredentials(int empId, String username, String password, String accessLevel) {
    File file = new File(LOGIN_DATA_CSV); 
    
    if (file.getParentFile() != null) {
        file.getParentFile().mkdirs();
    }

    boolean fileExists = file.exists();

    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
        
        if (!fileExists || file.length() == 0) {
            writer.println("Employee #,Username,First Name,Last Name,Password,Role");
        }

        Employee emp = employeeCache.get(empId);
        String firstName = (emp != null) ? emp.getFirstName() : "Unknown";
        String lastName = (emp != null) ? emp.getLastName() : "Unknown";

        writer.printf("%d,%s,%s,%s,%s,%s%n", 
            empId, 
            username, 
            firstName, 
            lastName, 
            password, 
            accessLevel
        );
        
        writer.flush();
        
        if (emp != null) {
            emp.setPassword(password);
            usernameCache.put(username.trim(), emp);
        }
        
        return true;
    } catch (IOException e) {
        System.err.println("Error saving login credentials: " + e.getMessage());
        return false;
    }
}

    private String getSavedUsername(Employee e) {
        for (Map.Entry<String, Employee> entry : usernameCache.entrySet()) {
            Employee value = entry.getValue();
            if (value != null && value.getEmpNo() == e.getEmpNo()) {
                return entry.getKey();
            }
        }
        return e.getUsername();
    }


    @Override
    public void saveNewPassword(int id, String pass) {
        Employee emp = findById(id);
        if (emp != null) {
            emp.setPassword(pass);
            saveLoginsToCSV();
        }
    }

    @Override
    public void updateEmployeeStatus(int id, String stat) {
        Employee emp = findById(id);
        if (emp != null) {
            emp.setStatus(stat);
            saveAllToCSV();
        }
    }

    private String getSavedUsername(Employee e) {
        for (Map.Entry<String, Employee> entry : usernameCache.entrySet()) {
            Employee value = entry.getValue();
            if (value != null && value.getEmpNo() == e.getEmpNo()) {
                return entry.getKey();
            }
        }
        return e.getUsername();
    }

}