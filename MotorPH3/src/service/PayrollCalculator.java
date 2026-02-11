/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author abigail
 */
public class PayrollCalculator {
   // Statutory Compliance: Centralized SSS Table
    public static double getSSSDeduction(double salary) {
        if (salary < 4000) return 180.0;
        if (salary >= 29750) return 1350.0;
        // ... rest of the table logic
        return 0.0;
    }

    // Centralized Tax Logic to prevent financial discrepancies
    public static double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0.0;
        // ... rest of the CP2 Tax Logic
        return 0.0;
    }
}
