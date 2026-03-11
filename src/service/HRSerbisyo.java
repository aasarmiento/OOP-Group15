package service;

import dao.EmployeeDAO;
import dao.LeaveLibrary;
import java.util.List;
import model.Employee;
import model.HRStaff;
import model.LeaveRequest;


public class HRSerbisyo {
    private final EmployeeDAO employeeDao;
    private final LeaveLibrary leaveLibrary; 

    public HRSerbisyo(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
        this.leaveLibrary = new LeaveLibrary(); 
    }

    // --- EMPLOYEE MANAGEMENT LOGIC ---

    public boolean registerNewEmployee(Employee e) {
        if (e.getEmpNo() <= 0 || e.getLastName().isEmpty()) return false; 
        // FIXED: Renamed .save() to .addEmployee() to match your interface
        return employeeDao.addEmployee(e); 
    }

    public boolean removeEmployee(int id) {
        if (id == 10001) { 
            System.out.println("Error: System Admin cannot be deleted.");
            return false;
        }
        // FIXED: Renamed .delete() to .deleteEmployee() to match your interface
        return employeeDao.deleteEmployee(id);
    }

    // --- LEAVE MANAGEMENT LOGIC ---

    public boolean updateLeaveStatus(String requestId, String status) {
        if (requestId == null || requestId.trim().isEmpty()) return false;
        try {
            // This calls LeaveLibrary directly
            return leaveLibrary.updateLeaveStatus(requestId, status);
        } catch (Exception e) {
            System.err.println("Service Error: " + e.getMessage());
            return false;
        }
    }

    public void approveLeave(HRStaff actor, String leaveID) {
        leaveLibrary.updateLeaveStatus(leaveID, "Approved"); 
    }

    public void rejectLeave(HRStaff actor, String leaveID, String reason) {
        leaveLibrary.updateLeaveStatus(leaveID, "Rejected"); 
    }

  public Object[][] getAllLeaveRequestsForTable() {
    // 1. Fetch raw data from the DAO (LeaveLibrary)
    List<String[]> rawData = leaveLibrary.fetchAllLeaves();
    
    // 2. Prepare the 2D array for the JTable (matching your 9 columns)
    Object[][] tableData = new Object[rawData.size()][9];
    
    for (int i = 0; i < rawData.size(); i++) {
        // We take the full row directly from the CSV data
        tableData[i] = rawData.get(i);
    }
    return tableData;
}

    public int getPendingLeaveCount() {
        List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
        int count = 0;
        for (String[] row : allLeaves) {
            if (row != null && row.length >= 9 && "Pending".equalsIgnoreCase(row[8])) {
                count++;
            }
        }
        return count;
    }

    public Employee getEmployeeById(int id) {
        return employeeDao.findById(id); 
    }

    // Inside HRSerbisyo.java
public List<LeaveRequest> getAllLeaveRequestsList() {
    // This calls the method defined in your EmployeeDAO interface
    return employeeDao.getAllLeaveRequestsList();
}


// Add this to HRSerbisyo.java
public Object[][] getLeaveHistory(int empNo) {
    List<String[]> allLeaves = leaveLibrary.fetchAllLeaves();
    // Filter the list so only rows where row[1] (Emp ID) matches empNo are returned
    return allLeaves.stream()
        .filter(row -> row.length >= 2 && row[1].equals(String.valueOf(empNo)))
        .toArray(Object[][]::new);
}


}