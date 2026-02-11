/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

/**
 *
 * @author abigail
 */
public interface IAdminOperations {
    // Adds a new employee record to the system
    public void createEmployee(Employee emp); 
    
    // Auto-generates the next sequential ID (e.g., 10026)
    public int generateNextEmpNo(); 
    
    // Validation logic for record integrity
    public boolean isEmployeeValid(Employee emp); 
    
    // Updates existing records
    public void updateEmployee(String empNo); 
    
    // Deletes/Archives an employee record
    public void removeEmployee(String empNo);
}


