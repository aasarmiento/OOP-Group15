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
    
    
    public void batchProcessPayroll();

    
    public Payslip generatePayslip(String empNo, String payPeriod);

   
    public File generateTaxReport(String quarter);

   
    public SummaryData generateDeductionSummary(String month);
}