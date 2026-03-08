package dao;

import model.Employee;
import model.Role;

public class UserLibrary {
    private final EmployeeDAO employeeDAO;

    private static Role userRole;
    private static Employee loggedInEmployee;

    private String lastLoginMessage = "";
    private boolean passwordChangeRequired = false;

    public UserLibrary(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            lastLoginMessage = "Please enter both username and password.";
            return false;
        }

        Employee emp = employeeDAO.findByUsername(username.trim());

        if (emp == null) {
            lastLoginMessage = "User not found.";
            return false;
        }

        if (employeeDAO.isLocked(emp.getEmpNo())) {
            lastLoginMessage = "Account is locked. Please use Forgot Password or contact IT Support.";
            return false;
        }

        if (emp.getPassword() != null && emp.getPassword().equals(password)) {
            employeeDAO.resetLoginState(emp.getEmpNo());

            userRole = (emp.getRole() != null) ? emp.getRole() : determineRole(emp.getPosition());
            emp.setRole(userRole);
            loggedInEmployee = emp;

            passwordChangeRequired = employeeDAO.mustChangePassword(emp.getEmpNo());
            lastLoginMessage = passwordChangeRequired
                    ? "Temporary password accepted. You must change your password now."
                    : "Login successful.";

            System.out.println("Session Started: " + emp.getFirstName() + " as " + userRole);
            return true;
        }

        int attempts = employeeDAO.getFailedAttempts(emp.getEmpNo()) + 1;
        boolean lockNow = attempts >= 3;

        employeeDAO.updateLoginState(emp.getEmpNo(), attempts, lockNow);

        if (lockNow) {
            lastLoginMessage = "Account locked after 3 failed attempts. Please use Forgot Password or IT Support.";
        } else {
            lastLoginMessage = "Invalid credentials. Attempts left: " + (3 - attempts);
        }

        passwordChangeRequired = false;
        return false;
    }

    public String getLastLoginMessage() {
        return lastLoginMessage;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void clearPasswordChangeRequired() {
        passwordChangeRequired = false;
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

        if (pos.contains("accounting") || pos.contains("account") || pos.contains("finance")) {
            return Role.ACCOUNTING;
        }

        if (pos.contains("it") || pos.contains("systems") || pos.contains("operations")) {
            return Role.IT_STAFF;
        }

        if (pos.contains("hr") || pos.contains("human resources")) {
            return Role.HR_STAFF;
        }

        if (pos.contains("admin") || pos.contains("chief") || pos.contains("executive") || pos.contains("manager")) {
            return Role.ADMIN;
        }

        return Role.REGULAR_STAFF;
    }
}