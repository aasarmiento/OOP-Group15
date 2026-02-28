package service;

import dao.CSVHandler;
import dao.LogHandler;
import model.Employee;

public class PangVerify {
   
    private final LogHandler logDAO = new LogHandler();
    private final CSVHandler empDAO = new CSVHandler();
    
    
    private Employee currentUser;
    private String currentRole;

  
    public boolean login(String user, String pass) {
        try {
          
            int empId = Integer.parseInt(user);
            Employee emp = empDAO.findById(empId);
            
            if (emp != null && emp.getStoredPassword().equals(pass)) {
                this.currentUser = emp;
               
                this.currentRole = emp.getRole().toString(); 
                return true;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid Employee ID format: " + user);
        }
        return false;
    }

    public boolean isHR() {
        return "HR".equalsIgnoreCase(currentRole) || "Admin".equalsIgnoreCase(currentRole);
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public int calculatePasswordStrength(Employee emp) {
        if (emp == null || emp.getTin() == null) return 0;
        return Math.min(emp.getTin().length() * 10, 100);
    }

    public void performPasswordReset(Employee emp) {
        if (emp != null) {
            emp.resetPassword(); // Changes status in Model
            logDAO.logAction("ALERT: Password reset for Emp# " + emp.getEmpNo());
            System.out.println("System: Security flags updated for " + emp.getFirstName());
        }
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("System Log: Employee " + currentUser.getFirstName() + " logged out.");
            this.currentUser = null;
            this.currentRole = null;
        }
    }
} 