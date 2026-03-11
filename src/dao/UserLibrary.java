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

// Inside UserLibrary.java (Service Layer)
public boolean authenticate(String username, String password) {
    Employee emp = employeeDAO.findByUsername(username);

    if (emp != null && emp.getPassword().equals(password)) {
        // SERVICE LOGIC: Convert the "Position" string into a "Role" Enum
        // This protects the DAO and handles the validation logic
        userRole = determineRole(emp.getPosition()); 
        
        emp.setRole(userRole); // Update the model
        loggedInEmployee = emp;
        
        System.out.println("Session Started: " + emp.getFirstName() + " as " + userRole);
        return true;
    }
    return false;
}


    public static void logout() {
        userRole = null;
        loggedInEmployee = null;
        System.out.println("Session Ended.");
    }

    public static Role getUserRole() {
        return userRole;
    }

    public static Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }
    
    public static boolean isLoggedIn() {
        return loggedInEmployee != null;
    }
public static void loginUser(Employee emp) {
    loggedInEmployee = emp;
    userRole = emp.getRole();
}
public Role determineRole(String positionText) {
    if (positionText == null) return Role.REGULAR_STAFF;
    
    String pos = positionText.toLowerCase();
    
    // 1. Check for Accounting/Finance FIRST 
    // This catches "Account Manager" before the generic "manager" check
    if (pos.contains("accounting") || pos.contains("account") || pos.contains("finance")) {
        return Role.ACCOUNTING;
    }
    
    // 2. Check for IT
    if (pos.contains("it") || pos.contains("systems") || pos.contains("operations")) {
        return Role.IT_STAFF;
    }
    
    // 3. Check for HR (Removed "manager" from here to prevent overlap)
    if (pos.contains("hr") || pos.contains("human resources")) {
        return Role.HR_STAFF;
    }

    // 4. Check for Admin/Leadership
    if (pos.contains("admin") || pos.contains("chief") || pos.contains("executive") || pos.contains("manager")) {
        return Role.ADMIN;
    }
    
    return Role.REGULAR_STAFF; 
}

}