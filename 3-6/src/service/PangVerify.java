package service;

import dao.EmployeeDAO;
import dao.LogHandler;
import dao.UserLibrary; 
import model.Employee;

public class PangVerify { 

    private final EmployeeDAO empDAO;
    private final LogHandler logDAO;

    public PangVerify(EmployeeDAO empDAO, LogHandler logDAO) {
        this.empDAO = empDAO;
        this.logDAO = logDAO;
    }

    public boolean login(String input, String pass) {
        Employee emp; // Removed null assignment to avoid "assigned value never used"
        
        try {
            int empId = Integer.parseInt(input);
            emp = empDAO.findById(empId);
        } catch (NumberFormatException e) {
            emp = empDAO.findByUsername(input);
        }

        // Check if employee exists and passwords match
        if (emp != null && emp.getPassword() != null && emp.getPassword().equals(pass)) {
            
            // FIXED: Calling the static setter in UserLibrary to store the session
            UserLibrary.loginUser(emp); 
            
            logDAO.logAction("LOGIN_SUCCESS: ID " + emp.getEmpNo());
            return true;
        }

        logDAO.logAction("LOGIN_FAILED: User " + input);
        return false;
    }

    public void logout() {
        UserLibrary.logout(); 
        logDAO.logAction("LOGOUT_SUCCESS");
    }

    public String getDashboardType() {
        Employee current = UserLibrary.getLoggedInEmployee();
        if (current == null) return "LOGIN";
        
        // This switch uses the current user's role to tell the UI which frame to open
        return switch (current.getRole()) {
            case ADMIN -> "ADMIN";
            case HR_STAFF -> "HR";
            case ACCOUNTING -> "ACCOUNTING";
            case IT_STAFF -> "IT";
            default -> "REGULAR"; 
        };
    }
}