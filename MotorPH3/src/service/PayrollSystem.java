/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.Employee;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author abigail
 */
public class PayrollSystem {
    // Controller owns the list of generic Employees
    private List<Employee> masterEmployeeList = new ArrayList<>();

    public void processFullPayroll() {
        System.out.println("--- STARTING SYSTEM-WIDE PAYROLL ---");
        
        for (Employee emp : masterEmployeeList) {
            // Polymorphism: The controller calls calculateSalary()
            // It doesn't care if 'emp' is an Admin, HR, or Intern.
            // Each subclass provides its own specific implementation.
            emp.calculateSalary(); 
        }
        
        System.out.println("--- PAYROLL COMPLETE ---");
    }
}
