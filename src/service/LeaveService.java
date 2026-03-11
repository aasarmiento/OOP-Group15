package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveLibrary; // Import your Leave DAO
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import model.Employee;

public class LeaveService {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final LeaveLibrary leaveLibrary; // Added for N-Tier compliance
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    private static final int MAX_VACATION = 15;
    private static final int MAX_SICK = 15; // Increased to match industry standard
    private static final int MAX_EMERGENCY = 5; 
    private static final int MAX_MATERNITY = 105; // Statutory Minimum
    private static final int MAX_PATERNITY = 7;   // Statutory Minimum

    public LeaveService(EmployeeDAO employeeDao, AttendanceDAO attendanceDao) {
        this.employeeDao = employeeDao;
        this.attendanceDao = attendanceDao;
        this.leaveLibrary = new LeaveLibrary(); // Initialize the Leave DAO
    }

    // --- ATTENDANCE LOGIC ---
    public Object[][] getAttendanceByMonth(int empNo, String month, String year) {
    // FIX 1: Use 'empNo' instead of the undefined 'empId'
    // FIX 2: Ensure your DAO method is equipped to handle BOTH month and year
    return attendanceDao.getAttendanceByMonth(empNo, month, year);
}

   public void submitLeave(int empNo, String type, LocalDate start, LocalDate end, String reason) {
    // 1. Basic Date Validation
    if (end.isBefore(start)) {
        throw new IllegalArgumentException("Error: End date cannot be before start date.");
    }

    // 2. LEAVE CREDIT VALIDATION (Corporate Logic)
    // Calculate how many days the user is asking for
    long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    
    // Get their current balance for this specific type
    int currentBalance = getRemainingBalance(empNo, type);

    if (requestedDays > currentBalance) {
        throw new IllegalArgumentException("Insufficient Balance! You requested " + requestedDays + 
                                           " days, but you only have " + currentBalance + " days left for " + type + ".");
    }

    // 3. Get Employee details
    Employee emp = employeeDao.findById(empNo);
    if (emp == null) throw new RuntimeException("Employee not found.");

    // 4. GENERATE SEQUENTIAL ID
    List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
    int count = 1; 
    for (String[] row : allLeaves) {
        if (row.length > 1 && row[1].equals(String.valueOf(empNo))) {
            count++;
        }
    }
    
    String sequence = String.format("%02d", count);
    String generatedID = "LR-" + empNo + "-" + sequence;

    // 5. Format Data
    String startStr = start.format(formatter);
    String endStr = end.format(formatter);
    // Wrap reason in quotes to prevent CSV breakage if user uses commas
    String cleanReason = (reason == null || reason.trim().isEmpty()) ? "No reason" : "\"" + reason.replace("\"", "'") + "\"";

    // 6. Prepare CSV line (Matches: LeaveID,ID,LastName,FirstName,Type,Start,End,Reason,Status)
    String csvLine = String.format("%s,%d,%s,%s,%s,%s,%s,%s,PENDING",
            generatedID, empNo, emp.getLastName(), emp.getFirstName(), 
            type, startStr, endStr, cleanReason);

    // 7. Save
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



public int getRemainingBalance(int empNo, String leaveType) {
   int totalAllowed = switch (leaveType) {
        case "Vacation Leave" -> MAX_VACATION;
        case "Sick Leave" -> MAX_SICK;
        case "Emergency Leave" -> MAX_EMERGENCY;
        case "Maternity Leave" -> MAX_MATERNITY;
        case "Paternity Leave" -> MAX_PATERNITY;
        default -> 0;
    };

    // Sum up all APPROVED leaves of this type for this employee
    List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
    double used = 0;

    for (String[] row : allLeaves) {
        // row[1]=EmpID, row[4]=Type, row[5]=Start, row[6]=End, row[8]=Status
        // Updated Logic: Deduct both Approved AND Pending requests
    if (row.length >= 9 && 
    row[1].equals(String.valueOf(empNo)) && 
    row[4].equalsIgnoreCase(leaveType) && 
    (row[8].equalsIgnoreCase("APPROVED") || row[8].equalsIgnoreCase("PENDING"))) {
    
    LocalDate start = LocalDate.parse(row[5], formatter);
    LocalDate end = LocalDate.parse(row[6], formatter);
    used += (ChronoUnit.DAYS.between(start, end) + 1);
}
    }
    return (int) (totalAllowed - used);
}


// Add these to your LeaveService.java class

/**
 * For Admin View: Fetches every single leave record in the CSV.
 */
public Object[][] getAllLeaveRequests() {
    List<String[]> rawData = leaveLibrary.fetchAllLeaves();
    Object[][] tableData = new Object[rawData.size()][9];
    
    for (int i = 0; i < rawData.size(); i++) {
        tableData[i] = rawData.get(i);
    }
    return tableData;
}

/**
 * Updates the status in the CSV (Approved/Rejected)
 */
public void updateLeaveStatus(String requestId, String status) {
    leaveLibrary.updateLeaveStatus(requestId, status);
}



}