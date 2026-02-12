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
  
    public void createEmployee(Employee emp); 
    
    
    public int generateNextEmpNo(); 
    
    
    public boolean isEmployeeValid(Employee emp); 
    
   
    
    
  
    public void removeEmployee(int empNo);
    
    public void updateEmployee(int empNo);
}


