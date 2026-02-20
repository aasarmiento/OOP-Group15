package dao;

import model.*;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CSVHandler implements EmployeeDAO {

    public static final String EMPLOYEE_DATA_CSV = "src/MotorPH_EmployeeData.csv";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<Employee> getAll() {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
            
            br.readLine(); // Skip header line to avoid unused variable warning

            String line;
            while ((line = br.readLine()) != null) {
                // Regex to handle commas inside quotes (like in addresses)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length < 19) continue;

                LocalDate birthday = null;
                String bdayStr = clean(data[3]);
                if (!bdayStr.isEmpty()) {
                    try {
                        birthday = LocalDate.parse(bdayStr, dateFormatter);
                    } catch (Exception e) { /* Handle parse error */ }
                }
                employees.add(mapToEmployee(data, birthday));
            }
        } catch (IOException e) {
            showError("Error reading CSV: " + e.getMessage());
        }
        return employees;
    }

    @Override
    public Employee findById(int empNo) {
        // Business Logic protection: finds a specific model for Service Layer
        return getAll().stream()
                .filter(e -> e.getEmpNo() == empNo)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void add(Employee emp) {
        appendToCSV(EMPLOYEE_DATA_CSV, convertToCSVLine(emp));
    }

    @Override
    public void update(Employee updatedEmp) {
        List<Employee> employees = getAll();
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmpNo() == updatedEmp.getEmpNo()) {
                employees.set(i, updatedEmp);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(employees);
        }
    }

    @Override
    public void delete(int empNo) {
        List<Employee> employees = getAll();
        if (employees.removeIf(e -> e.getEmpNo() == empNo)) {
            saveAll(employees);
        }
    }

    // --- PRIVATE UTILITIES (Internal Engine) ---

    private void saveAll(List<Employee> employees) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV))) {
            // Write standard header
            bw.write("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate");
            bw.newLine();
            for (Employee emp : employees) {
                bw.write(convertToCSVLine(emp));
                bw.newLine();
            }
        } catch (IOException e) {
            showError("Save Error: " + e.getMessage());
        }
    }

    private String convertToCSVLine(Employee emp) {
        return String.format("%d,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
            emp.getEmpNo(), emp.getLastName(), emp.getFirstName(),
            (emp.getBirthday() != null ? emp.getBirthday().format(dateFormatter) : ""),
            emp.getAddress(), emp.getPhone(), emp.getSss(), emp.getPhilhealth(),
            emp.getTin(), emp.getPagibig(), emp.getStatus(), emp.getPosition(),
            emp.getSupervisor(), emp.getBasicSalary(), emp.getRiceSubsidy(),
            emp.getPhoneAllowance(), emp.getClothingAllowance(), emp.getGrossRate(), emp.getHourlyRate()
        );
    }

    private void appendToCSV(String filePath, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            showError("Write Error: " + e.getMessage());
        }
    }

    private Employee mapToEmployee(String[] data, LocalDate birthday) {
        int id = Integer.parseInt(clean(data[0]));
        String lastName = clean(data[1]);
        String firstName = clean(data[2]);
        String pos = clean(data[11]);
        double basic = parseCurrency(data[13]);

        Employee emp;
        if (pos.contains("Admin")) emp = new Admin(id, lastName, firstName, birthday, basic);
        else if (pos.contains("HR")) emp = new HRStaff(id, lastName, firstName, birthday, basic);
        else if (pos.contains("IT")) emp = new ITStaff(id, lastName, firstName, birthday, basic);
        else if (pos.contains("Accounting")) emp = new AccountingStaff(id, lastName, firstName, birthday, basic);
        else emp = new RegularStaff(id, lastName, firstName, birthday, basic);

        // Populate fields from Model
        emp.setAddress(clean(data[4]));
        emp.setPhone(clean(data[5]));
        emp.setSss(clean(data[6]));
        emp.setPhilhealth(clean(data[7]));
        emp.setTin(clean(data[8]));
        emp.setPagibig(clean(data[9]));
        emp.setStatus(clean(data[10]));
        emp.setPosition(pos);
        emp.setSupervisor(clean(data[12]));
        emp.setRiceSubsidy(parseCurrency(data[14]));
        emp.setPhoneAllowance(parseCurrency(data[15]));
        emp.setClothingAllowance(parseCurrency(data[16]));
        emp.setGrossRate(parseCurrency(data[17]));
        emp.setHourlyRate(parseCurrency(data[18]));
        return emp;
    }

    private String clean(String input) { return (input == null) ? "" : input.trim().replace("\"", ""); }

    private double parseCurrency(String input) {
        String cleaned = clean(input).replace(",", "");
        return cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(null, msg); }
}