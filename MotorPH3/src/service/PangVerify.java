package service;

import dao.LogHandler;
import dao.CSVHandler;
import model.Employee;

public class PangVerify {
    private LogHandler logDAO = new LogHandler();
    private CSVHandler empDAO = new CSVHandler();
    
    // Dito naka-save kung sino ang kasalukuyang gumagamit ng system
    private static Employee currentUser;
    private static String currentRole;

    public boolean login(String user, String pass) {
        // Tatawag sa LogHandler (DAO) para tignan ang credentials sa CSV
        String[] credentials = logDAO.verifyCredentials(user, pass);
        
        if (credentials != null) {
            try {
                int empId = Integer.parseInt(credentials[0].trim());
                currentRole = credentials[3].trim(); 
                
                // Kukunin ang buong detalye ng Employee mula sa kabilang DAO
                currentUser = empDAO.findById(empId);
                return true;
            } catch (Exception e) {
                System.err.println("Error parsing login data: " + e.getMessage());
            }
        }
        return false;
    }

    // Business Rule: Sinisiguro kung may access ang user (Proteksyon sa DAO)
    public static boolean isHR() {
        return "HR".equalsIgnoreCase(currentRole) || "Admin".equalsIgnoreCase(currentRole);
    }

    public static Employee getCurrentUser() {
        return currentUser;
    }
    
    public static void logout() {
        currentUser = null;
        currentRole = null;
    } 
}