/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;


/**
 *
 * @author abigail
 */
public interface IPayrollCalculations {
    public abstract double calculateTotalHoursWorked();
    public abstract double calculateGrossSalary();
    public abstract double calculateSSSDeduction();
    public abstract double calculatePhilHealth();
    public abstract double calculatePagIBIG();
    public abstract double calculateWithholdingTax();
    public abstract double calculateNetPay();
    public abstract void calculateSalary();
    public abstract double computeDeductions();
    public abstract double computeNet();
    

    
    
}
    