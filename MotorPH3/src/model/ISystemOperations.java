package model;

/**
 * Interface for System Operations
 * Focuses on system health and credential management.
 */
public interface ISystemOperations {
    
    // Method Name: resetCredentials (1)
    boolean resetCredentials(String empID, String newPassword);
    
   
    
    // Method Name: checkSystemHealth
    String checkSystemHealth();
}