package service;

import dao.EmployeeDAO;
import model.ISupportOperations;

public class ITSupportService implements ISupportOperations {
    private final EmployeeDAO employeeDAO;

    // We "inject" the DAO so the Service can talk to the database/CSV
    public ITSupportService(EmployeeDAO dao) {
        this.employeeDAO = dao;
    }

    @Override
    public void resolveTicket(int empNo) {
        // Business Rule: Resolving a ticket always sets status to ACTIVE
        employeeDAO.updateEmployeeStatus(empNo, "ACTIVE");
    }

    @Override
    public boolean resetCredentials(int empNo, String newPassword) {
        // Business Rule: Update password AND reactivate account
        employeeDAO.saveNewPassword(empNo, newPassword);
        employeeDAO.updateEmployeeStatus(empNo, "ACTIVE");
        return true;
    }
}