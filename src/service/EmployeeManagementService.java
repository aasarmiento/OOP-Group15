package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import model.Employee;
import model.IAdminOperations;
import model.PeriodSummary;
import model.RegularStaff;

public class EmployeeManagementService {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;

    public EmployeeManagementService(EmployeeDAO employeeDao, AttendanceDAO attendanceDao) {
        this.employeeDao = employeeDao;
        this.attendanceDao = attendanceDao;
    }

    // --- DASHBOARD & UI MAPPING METHODS ---

    public String[] getFormattedDataForForm(Object[] raw) {
        String[] uiData = new String[21];
        if (raw == null) return uiData;

        try {
            uiData[0] = String.valueOf(raw[0]);  // ID
            uiData[1] = String.valueOf(raw[1]);  // Last Name
            uiData[2] = String.valueOf(raw[2]);  // First Name
            uiData[3] = (raw.length > 20 && raw[20] != null) ? String.valueOf(raw[20]) : "N/A"; 
            uiData[4] = String.valueOf(raw[3]);  // Birthday
            uiData[5] = String.valueOf(raw[4]);  // Address
            uiData[6] = String.valueOf(raw[5]);  // Phone
            uiData[7] = String.valueOf(raw[6]);  // SSS
            uiData[8] = String.valueOf(raw[7]);  // Philhealth
            uiData[9] = String.valueOf(raw[8]);  // TIN
            uiData[10] = String.valueOf(raw[9]); // Pag-ibig
            uiData[11] = String.valueOf(raw[10]); // Status
            uiData[12] = String.valueOf(raw[11]); // Position
            uiData[13] = String.valueOf(raw[12]); // Supervisor
            uiData[14] = String.valueOf(raw[13]); // Basic Salary
            uiData[15] = String.valueOf(raw[14]); // Rice Subsidy
            uiData[16] = String.valueOf(raw[15]); // Phone Allowance
            uiData[17] = String.valueOf(raw[16]); // Clothing Allowance
            uiData[18] = String.valueOf(raw[17]); // Gross Semi-monthly
            uiData[19] = String.valueOf(raw[18]); // Hourly Rate
            uiData[20] = String.valueOf(raw[19]); // Role
        } catch (Exception e) {
            System.err.println("Mapping Error at Service Layer: " + e.getMessage());
            for(int i=0; i<uiData.length; i++) if(uiData[i] == null) uiData[i] = "";
        }
        return uiData;
    }

    public List<Employee> getAllEmployees() {
        return getAll();
    }

    public Object[] getEmployeeDetailsForForm(int empId) {
        Employee emp = employeeDao.findById(empId);
        if (emp == null) return null;

        return new Object[] {
            emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getBirthday(),
            emp.getAddress(), emp.getPhone(), emp.getSss(), emp.getPhilhealth(),
            emp.getTin(), emp.getPagibig(), emp.getStatus(), emp.getPosition(),
            emp.getSupervisor(), String.format("%.0f", emp.getBasicSalary()),
            String.format("%.0f", emp.getRiceSubsidy()), String.format("%.0f", emp.getPhoneAllowance()),
            String.format("%.0f", emp.getClothingAllowance()), String.format("%.0f", emp.getGrossRate()),
            String.format("%.0f", emp.getHourlyRate()), emp.getRole(), emp.getGender()
        };
    }

    // --- EMPLOYEE OPERATIONS ---

    public boolean processNewHire(Employee actor, String fName, String lName, String sss, double salary) {
        if (!(actor instanceof IAdminOperations)) {
            showError("Access Denied: Only Admins can register employees.");
            return false;
        }
        Employee newEmp = new RegularStaff();
        newEmp.setFirstName(fName);
        newEmp.setLastName(lName);
        newEmp.setSss(sss);
        newEmp.setBasicSalary(salary);
        newEmp.setRiceSubsidy(1500);
        newEmp.setPhoneAllowance(500);
        newEmp.setClothingAllowance(1000);
        return registerEmployee((IAdminOperations)actor, newEmp);
    }

    public boolean registerEmployee(IAdminOperations actor, Employee emp) {
        if (actor == null || emp == null) return false;
        if (emp.getFirstName().isEmpty() || emp.getLastName().isEmpty()) {
            showError("First and Last names are required!");
            return false;
        }
        emp.setEmpNo(employeeDao.getNextAvailableId());
        double hourly = emp.getBasicSalary() / 21 / 8;
        emp.setHourlyRate(hourly);
        emp.setGrossRate(emp.getBasicSalary() + emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance());
        return employeeDao.addEmployee(emp);
    }

    public List<Employee> getAll() { 
        return employeeDao.getAll();
    }

    public boolean deleteEmployee(Employee actor, int id) {
        if (!(actor instanceof IAdminOperations)) return false;
        if (id == 10001) return false; 
        return employeeDao.deleteEmployee(id);
    }

    public Employee findById(int id) {
        return employeeDao.findById(id);
    }

    // FIXED: Removed extra 'p' from ppublic
    public boolean updateEmployeeFromForm(Employee actor, JTextField[] fields) {
        try {
            if (!(actor instanceof IAdminOperations)) { 
                showError("Access Denied."); 
                return false; 
            }

            for (int i = 1; i < fields.length; i++) {
                String text = fields[i].getText().trim();
                if (text.isEmpty() || isPlaceholder(text)) {
                    showError("Validation Error: All fields must be filled out.");
                    return false; 
                }
            }

            String lastName = fields[1].getText().trim();
            String firstName = fields[2].getText().trim();
            if (!isValidName(lastName) || !isValidName(firstName)) {
                showError("Validation Error: Names must only contain letters and spaces.");
                return false;
            }

            double hourlyRate = parseDouble(fields[14].getText());
            double clothing = parseDouble(fields[17].getText());
            if (hourlyRate <= 0 || clothing <= 0) {
                showError("Validation Error: Financial fields (Hourly Rate/Clothing) must be greater than 0.");
                return false;
            }

            String bdayText = fields[4].getText().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthday = bdayText.contains("-") ? LocalDate.parse(bdayText) : LocalDate.parse(bdayText, formatter);

            Employee emp = new RegularStaff();
            int idFromForm = Integer.parseInt(fields[0].getText().trim());
            emp.setEmpNo(idFromForm == 0 ? employeeDao.getNextAvailableId() : idFromForm);
            
            emp.setLastName(lastName);
            emp.setFirstName(firstName);
            emp.setGender(fields[3].getText().trim()); 
            emp.setBirthday(birthday);
            emp.setAddress(fields[5].getText().trim());
            emp.setPhone(fields[6].getText().trim());
            emp.setSss(fields[7].getText().trim());
            emp.setPhilhealth(fields[8].getText().trim());
            emp.setTin(fields[9].getText().trim());
            emp.setPagibig(fields[10].getText().trim());
            emp.setStatus(fields[11].getText().trim());
            emp.setPosition(fields[12].getText().trim());
            emp.setSupervisor(fields[13].getText().trim());

            double calculatedMonthlyBasic = hourlyRate * 8 * 21;
            emp.setHourlyRate(hourlyRate);
            emp.setBasicSalary(calculatedMonthlyBasic); 
            emp.setRiceSubsidy(parseDouble(fields[15].getText()));
            emp.setPhoneAllowance(parseDouble(fields[16].getText()));
            emp.setClothingAllowance(clothing);

            double totalGross = emp.getBasicSalary() + emp.getRiceSubsidy() + 
                                emp.getPhoneAllowance() + emp.getClothingAllowance();
            emp.setGrossRate(totalGross);

            return (idFromForm == 0) ? employeeDao.addEmployee(emp) : employeeDao.update(emp);

        } catch (Exception e) {
            showError("System Error: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z\\s\\.]+$");
    }

    private boolean isPlaceholder(String text) {
        List<String> placeholders = Arrays.asList(
            "Juan", "Delacruz", "MM/dd/yyyy", "123 Street, City", "000-000-000",
            "00-0000000-0", "000000000000", "000-000-000-000"
        );
        return placeholders.contains(text);
    }

    private void setEmployeeDetails(Employee emp, JTextField[] fields, LocalDate birthday) {
        emp.setLastName(fields[1].getText().trim());
        emp.setFirstName(fields[2].getText().trim());
        emp.setGender(fields[3].getText().trim()); 
        emp.setBirthday(birthday);
        emp.setAddress(fields[5].getText().trim());
        emp.setPhone(fields[6].getText().trim());
        emp.setSss(fields[7].getText().trim());
        emp.setPhilhealth(fields[8].getText().trim());
        emp.setTin(fields[9].getText().trim());
        emp.setPagibig(fields[10].getText().trim());
        emp.setStatus(fields[11].getText().trim());
        emp.setPosition(fields[12].getText().trim());
        emp.setSupervisor(fields[13].getText().trim());

        double basic = parseDouble(fields[14].getText());
        emp.setBasicSalary(basic);
        emp.setRiceSubsidy(parseDouble(fields[15].getText()));
        emp.setPhoneAllowance(parseDouble(fields[16].getText()));
        emp.setClothingAllowance(parseDouble(fields[17].getText()));
        emp.setGrossRate(emp.getBasicSalary() + emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance());
        emp.setHourlyRate(Math.round(basic / 21 / 8));
    }

    // --- PAYROLL & ATTENDANCE ---

    public PeriodSummary getPayrollForEmployee(int empNo, String month, String year) {
        Employee emp = employeeDao.findById(empNo);
        Object[][] logs = attendanceDao.getAttendanceByMonth(empNo, month, year);
        service.PayrollCalculator calc = new service.PayrollCalculator();
        PayrollService payrollService = new PayrollService(employeeDao, calc, this);
        DateTimeFormatter csvDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        double totalHours = 0;

        for (Object[] row : logs) {
            try {
                model.Attendance record = new model.Attendance(
                    empNo, LocalDate.parse(row[0].toString().trim(), csvDateFormatter),
                    row[1].equals("N/A") ? null : java.time.LocalTime.parse(row[1].toString().trim()),
                    row[2].equals("N/A") ? null : java.time.LocalTime.parse(row[2].toString().trim())
                );
                payrollService.processAttendance(record); 
                totalHours += record.getHoursWorked();
            } catch (Exception e) { System.err.println("Attendance Record Error: " + e.getMessage()); }
        }
        double grossIncome = calc.calculateGrossIncome(emp, totalHours);
        return calc.calculateFullSummary(emp, grossIncome);
    }

    public void recordTimeLog(int empNo, String type) {
        Employee emp = employeeDao.findById(empNo);
        String action = type.toLowerCase().contains("in") ? "Check-in" : "Check-out";
        attendanceDao.recordAttendance(empNo, (emp != null ? emp.getLastName() : "Unknown"), (emp != null ? emp.getFirstName() : "Unknown"), action);
    }

    public Object[][] getAttendanceLogs(int empNo, String month, String year) {
        return attendanceDao.getAttendanceByMonth(empNo, month, year);
    }

    // --- UTILITIES ---

    public String[] getSupervisorsForPosition(String position) {
        if (position == null) return new String[]{"N/A"};
        switch (position) {
            case "Chief Operating Officer": case "Chief Finance Officer": case "Chief Marketing Officer": case "Account Manager": return new String[]{"Garcia, Manuel III"};
            case "IT Operations and Systems": case "HR Manager": return new String[]{"Lim, Antonio"};
            case "HR Team Leader": return new String[]{"Villanueva, Andrea Mae"};
            case "HR Rank and File": return new String[]{"San Jose, Brad"};
            case "Accounting Head": return new String[]{"Aquino, Bianca Sofia"};
            case "Payroll Manager": return new String[]{"Alvaro, Roderick"};
            case "Payroll Team Leader": case "Payroll Rank and File": return new String[]{"Salcedo, Anthony"};
            case "Account Team Leader": return new String[]{"Romualdez, Fredrick"};
            case "Account Rank and File": return new String[]{"Mata, Christian", "De Leon, Selena"};
            case "Sales & Marketing": case "Supply Chain and Logistics": case "Customer Service and Relations": return new String[]{"Reyes, Isabella"};
            default: return new String[]{"N/A"};
        }
    }

    private double parseDouble(String input) {
        try { return Double.parseDouble(input.trim().replace(",", "")); } catch (Exception e) { return 0.0; }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean removeEmployee(IAdminOperations actor, int id) {
        return deleteEmployee((Employee) actor, id);
    }

    public boolean[] getButtonStates(int empNo) {
        boolean[] states = {true, false};

        try {
            String status = attendanceDao.getLastStatus(empNo);
            System.out.println("Last status for emp " + empNo + ": " + status);

            if ("CHECKED_IN".equalsIgnoreCase(status)) {
                states[0] = false; // Check In disabled
                states[1] = true;  // Check Out enabled
            } else {
                states[0] = true;  // ready for next Check In
                states[1] = false; // no open session
            }
        } catch (Exception e) {
            System.err.println("Error fetching button states: " + e.getMessage());
        }

        return states;
    }

    public EmployeeDAO getEmployeeDao() {
        return this.employeeDao;
    }

    public int generateNextEmployeeId() {
        return employeeDao.getNextAvailableId();
    }

    public void updateEmployeePhoto(Employee emp, File selectedFile) {
        try {
            if (!selectedFile.getName().toLowerCase().endsWith(".png") && 
                !selectedFile.getName().toLowerCase().endsWith(".jpg")) {
                throw new IllegalArgumentException("Only PNG or JPG allowed.");
            }
            employeeDao.saveProfilePicture(emp.getEmpNo(), selectedFile);
            String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
            emp.setPhotoPath(emp.getEmpNo() + extension);
            employeeDao.update(emp);
        } catch (IOException e) {
            showError("Failed to save image: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
}