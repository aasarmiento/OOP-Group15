/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package model;

import java.io.File;

/**
 * @author abigail
 * IFinanceOperations: Defines the specialized financial contract for Accounting Staff.
 */
public interface IFinanceOperations {
    
    // Triggers the specialized payroll logic for the current pay cycle.
    public void batchProcessPayroll();

    // Produces a detailed breakdown of earnings and deductions.
    public Payslip generatePayslip(String empNo, String payPeriod);

    // Creates a summary of withholding taxes for statutory compliance.
    public File generateTaxReport(String quarter);

    // Reports total SSS, PhilHealth, and Pag-IBIG contributions.
    public SummaryData generateDeductionSummary(String month);
}