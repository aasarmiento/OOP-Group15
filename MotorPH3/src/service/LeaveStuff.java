package service;

import dao.EmployeeDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.Employee;

public class LeaveStuff {
    // This is your "Source of Truth" variable name
    private final EmployeeDAO dao; 
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public LeaveStuff(EmployeeDAO dao) {
        this.dao = dao;
    }

    public void submitLeave(int empNo, String type, String startStr, String endStr, String reason) {
        LocalDate start = LocalDate.parse(startStr, formatter);
        LocalDate end = LocalDate.parse(endStr, formatter);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        dao.applyForLeave(empNo, type, startStr, endStr, reason);
    }

    public Object[][] getLeaveHistory(int empNo) {
        return dao.getLeaveStatusByEmpId(empNo);
    }

    public void updateStatus(int empId, String startDate, String newStatus) {
        dao.updateLeaveStatus(empId, startDate, newStatus);
    }

    public Object[][] getAllPendingLeaves() {
        return dao.getAllLeaveRequests(); 
    }

    public Object[][] getEmployeeAttendance(int empId) {
        return dao.getAttendanceById(empId);
    }

    /**
     * FIXED: Changed 'csvHandler' to 'dao' to match your class field.
     * This follows your Service Layer rule: Validation -> DAO call.
     */
   // Inside LeaveStuff.java
public boolean updateEmployeeProfile(Employee emp) {
    // 1. Business Logic / Validation
    if (emp == null || emp.getEmpNo() <= 0) return false;
    
    // 2. Protect the DAO: Only call save if data is valid
    try {
        dao.update(emp); 
        return true;
    } catch (Exception e) {
        return false;
    }
}

public Employee getFreshEmployeeData(int empId) {
    // Business Rule: Always pull from DAO to ensure salary is up to date
    return dao.findById(empId); 
}

// Inside LeaveStuff.java

public Employee getEmployeeSalaryInfo(int empId) {
    // Business Logic: Verify ID before calling DAO
    if (empId <= 0) return null;
    return dao.findById(empId); // Calls the DAO method
}
}