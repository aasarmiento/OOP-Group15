package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveLibrary; // Import your Leave DAO
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Employee;

public class LeaveService {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final LeaveLibrary leaveLibrary; // Added for N-Tier compliance
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public LeaveService(EmployeeDAO employeeDao, AttendanceDAO attendanceDao) {
        this.employeeDao = employeeDao;
        this.attendanceDao = attendanceDao;
        this.leaveLibrary = new LeaveLibrary(); // Initialize the Leave DAO
    }

    // --- ATTENDANCE LOGIC ---
    public Object[][] getEmployeeAttendance(int empId, String month) {
        return attendanceDao.getAttendanceByMonth(empId, month);
    }

   public void submitLeave(int empNo, String type, LocalDate start, LocalDate end, String reason) {
    if (end.isBefore(start)) {
        throw new IllegalArgumentException("Error: End date cannot be before start date.");
    }

    // 1. Get Employee details
    Employee emp = employeeDao.findById(empNo);
    if (emp == null) throw new RuntimeException("Employee not found.");

    // 2. GENERATE SEQUENTIAL ID (e.g., LR-10001-01)
    List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
    int count = 1; // Start at 1 if no records found
    
    for (String[] row : allLeaves) {
        if (row.length > 1 && row[1].equals(String.valueOf(empNo))) {
            count++; // Increment for every existing request found for this ID
        }
    }
    
    // Format count as 2 digits (01, 02, etc.)
    String sequence = String.format("%02d", count);
    String generatedID = "LR-" + empNo + "-" + sequence;

    // 3. Format Data
    String startStr = start.format(formatter);
    String endStr = end.format(formatter);
    String cleanReason = (reason == null || reason.trim().isEmpty()) ? "No reason" : reason.replace(",", ";");

    // 4. Prepare CSV line
    String csvLine = String.format("%s,%d,%s,%s,%s,%s,%s,%s,PENDING",
            generatedID, empNo, emp.getLastName(), emp.getFirstName(), 
            type, startStr, endStr, cleanReason);

    // 5. Save
    leaveLibrary.saveLeave(csvLine);
}

    // --- LEAVE HISTORY LOGIC ---
    public Object[][] getLeaveHistory(int targetEmpId) {
        // N-Tier: Call the DAO instead of reading the file here
        List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
        List<Object[]> filtered = new ArrayList<>();

        for (String[] row : allLeaves) {
            try {
                // Column index 1 is EmployeeID
                int csvEmpId = Integer.parseInt(row[1].trim());
                if (csvEmpId == targetEmpId) {
                    filtered.add(row);
                }
            } catch (Exception e) {
                // Skip rows with bad ID formats
            }
        }
        return filtered.toArray(new Object[0][]);
    }

    // --- OTHER METHODS ---
    public void updateStatus(String requestId, String newStatus) {
        leaveLibrary.updateLeaveStatus(requestId, newStatus);
    }

    public Object[][] getAllPendingLeaves() {
        List<String[]> all = leaveLibrary.fetchAllLeaves();
        List<Object[]> pending = new ArrayList<>();
        for (String[] row : all) {
            if (row.length > 8 && row[8].equalsIgnoreCase("PENDING")) {
                pending.add(row);
            }
        }
        return pending.toArray(new Object[0][]);
    }

    public Employee getEmployeeSalaryInfo(int empId) {
        return employeeDao.findById(empId);
    }


    public boolean updateEmployeeProfile(Employee emp) {
    if (emp == null) return false;
    
    // Call the existing update method in your EmployeeDAO (CSVHandler)
    return employeeDao.update(emp);
}
}