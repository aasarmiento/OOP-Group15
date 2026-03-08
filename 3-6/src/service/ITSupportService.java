package service;

import dao.EmployeeDAO;
import model.ISupportOperations;

public class ITSupportService implements ISupportOperations {
    private final EmployeeDAO employeeDAO;

    public ITSupportService(EmployeeDAO dao) {
        this.employeeDAO = dao;
    }

    @Override
    public void resolveTicket(int empNo) {
        employeeDAO.resetLoginState(empNo);
    }

    @Override
    public String issueTemporaryPassword(int empNo) {
        if (employeeDAO.findById(empNo) == null) {
            return null;
        }

        String tempPassword = generateTemporaryPassword(empNo);

        employeeDAO.saveNewPassword(empNo, tempPassword);
        employeeDAO.setMustChangePassword(empNo, true);
        employeeDAO.resetLoginState(empNo);

        return tempPassword;
    }

    @Override
    public boolean unlockAccount(int empNo) {
        if (employeeDAO.findById(empNo) == null) {
            return false;
        }

        employeeDAO.resetLoginState(empNo);
        return true;
    }

    private String generateTemporaryPassword(int empNo) {
        int random = 100 + (int) (Math.random() * 900);
        return "TMP" + empNo + random;
    }
}