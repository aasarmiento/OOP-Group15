package dao;

import model.Employee;
import model.Role;

public class UserLibrary {
    private final EmployeeDAO employeeDAO;
    
  
    private static Role userRole;
    private static Employee loggedInEmployee;

    public UserLibrary(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public boolean authenticate(String username, String password) {
        
        Employee emp = employeeDAO.findByUsername(username);

        if (emp != null) {
            String storedPass = emp.getStoredPassword(); 
            
           
            if (storedPass != null && storedPass.equals(password)) {
                
               
                userRole = emp.getRole(); 
                loggedInEmployee = emp; 
                
                System.out.println("Login Success: Welcome " + emp.getFirstName() + " [" + userRole + "]");
                return true;
            } else {
                System.out.println("Login Failed: Password mismatch for " + username);
            }
        } else {
            System.out.println("Login Failed: Username [" + username + "] not found.");
        }
        return false;
    }

    
    public static Role getUserRole() {
        return userRole;
    }

    public static Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }
}