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
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

    private void loadAllIntoCache() {
        employeeCache.clear();
        usernameCache.clear();

        // 1. Load General Data
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
        } catch (IOException e) {
            showError("Data File Error: " + e.getMessage());
        }

        // 2. Load Login Data & OVERRIDE ROLES based on AccessLevel column
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] loginData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (loginData.length >= 6) { // Now checking for AccessLevel column
                    try {
                        int id = Integer.parseInt(loginData[0].trim());
                        String realUsername = loginData[1].trim().toLowerCase(); 
                        String realPass = loginData[4].trim();
                        String accessLevel = loginData[5].trim(); // New Column

                        Employee baseEmp = employeeCache.get(id);
                        if (baseEmp != null) {
                            // Re-cast the employee based on the Login CSV AccessLevel
                            Employee upgradedEmp = upgradeEmployeeRole(baseEmp, accessLevel);
                            upgradedEmp.setPassword(realPass);
                            
                            // Update caches
                            employeeCache.put(id, upgradedEmp);
                            usernameCache.put(realUsername, upgradedEmp);
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        } catch (IOException e) {
            System.err.println("Login File Error: " + e.getMessage());
        }
    }

    // Helper to change Employee type based on the new CSV AccessLevel column
    private Employee upgradeEmployeeRole(Employee old, String accessLevel) {
        Employee newEmp;
        if (accessLevel.equalsIgnoreCase("Admin")) {
            newEmp = new Admin(old.getEmpNo(), old.getLastName(), old.getFirstName(), old.getBirthday(), old.getBasicSalary());
        } else if (accessLevel.equalsIgnoreCase("HR")) {
            newEmp = new HRStaff(old.getEmpNo(), old.getLastName(), old.getFirstName(), old.getBirthday(), old.getBasicSalary());
        } else {
            newEmp = new RegularStaff(old.getEmpNo(), old.getLastName(), old.getFirstName(), old.getBirthday(), old.getBasicSalary());
        }
        
        // Copy all data from old to new
        newEmp.setAddress(old.getAddress());
        newEmp.setPhone(old.getPhone());
        newEmp.setPosition(old.getPosition());
        newEmp.setStatus(old.getStatus());
        newEmp.setSupervisor(old.getSupervisor());
        newEmp.setRiceSubsidy(old.getRiceSubsidy());
        newEmp.setPhoneAllowance(old.getPhoneAllowance());
        newEmp.setClothingAllowance(old.getClothingAllowance());
        newEmp.setGrossRate(old.getGrossRate());
        newEmp.setHourlyRate(old.getHourlyRate());
        return newEmp;
    }

    private Employee mapToEmployee(String[] data, LocalDate birthday) {
        int id = Integer.parseInt(clean(data[0]));
        String lastName = clean(data[1]);
        String firstName = clean(data[2]);
        double basic = parseCurrency(data[13]);

        // Defaulting to RegularStaff initially; upgradeEmployeeRole handles the rest
        Employee emp = new RegularStaff(id, lastName, firstName, birthday, basic);

        emp.setAddress(clean(data[4]));
        emp.setPhone(clean(data[5]));
        emp.setSss(clean(data[6]));
        emp.setPhilhealth(clean(data[7]));
        emp.setTin(clean(data[8]));
        emp.setPagibig(clean(data[9]));
        emp.setStatus(clean(data[10]));
        emp.setPosition(clean(data[11]));
        emp.setSupervisor(clean(data[12]));
        emp.setRiceSubsidy(parseCurrency(data[14]));
        emp.setPhoneAllowance(parseCurrency(data[15]));
        emp.setClothingAllowance(parseCurrency(data[16]));
        emp.setGrossRate(parseCurrency(data[17]));
        emp.setHourlyRate(parseCurrency(data[18]));
        
        return emp;
    }

    @Override public Employee findById(int empNo) { return employeeCache.get(empNo); }
    @Override public Employee findByUsername(String username) { return usernameCache.get(username.trim().toLowerCase()); }
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }
    @Override public void add(Employee emp) { employeeCache.put(emp.getEmpNo(), emp); saveAll(); }
    @Override public void update(Employee emp) { employeeCache.put(emp.getEmpNo(), emp); saveAll(); }
    @Override public void delete(int id) { employeeCache.remove(id); saveAll(); }

    public List<Employee> getSubordinates(String supervisorName) {
        List<Employee> subordinates = new ArrayList<>();
        for (Employee emp : employeeCache.values()) {
            if (emp.getSupervisor() != null && emp.getSupervisor().equalsIgnoreCase(supervisorName)) {
                subordinates.add(emp);
            }
        }
        return subordinates;
    }

    private void saveAll() { /* Implement file writing here */ }
    
    private String clean(String input) { return (input == null) ? "" : input.trim().replace("\"", ""); }
    
    private double parseCurrency(String input) {
        String cleaned = clean(input).replace(",", "");
        return cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
    }
    
    private void showError(String msg) { 
        JOptionPane.showMessageDialog(null, msg, "MotorPH Error", JOptionPane.ERROR_MESSAGE); 
    }
}