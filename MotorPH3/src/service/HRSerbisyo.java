package service;

import dao.EmployeeDAO;
import model.Employee;


public class HRSerbisyo {
private final EmployeeDAO dao;

    public HRSerbisyo(EmployeeDAO dao) {
        this.dao = dao;
    }

    // Connects UI to DAO.addEmployee
    public boolean registerNewEmployee(Employee e) {
        // Business Rule: Validate data before hitting the file
        if (e.getEmpNo() <= 0 || e.getLastName().isEmpty()) {
            return false; 
        }
        return dao.addEmployee(e); 
    }

    // Connects UI to DAO.updateEmployee
    public boolean updateEmployeeRecord(Employee e) {
        return dao.updateEmployee(e);
    }

    // Connects UI to DAO.deleteEmployee
    public boolean removeEmployee(int id) {
        // Business Rule: Example - Protect the CEO from accidental deletion
        if (id == 10001) {
            System.out.println("Error: System Admin cannot be deleted.");
            return false;
        }
        return dao.deleteEmployee(id);
    }

    public Employee getEmployeeById(int id) {
    // Service Layer can add logic here if needed before calling DAO
    return dao.findById(id); 
}

public Object[][] getAllLeaveRequestsForTable() {
    // Service pulls all records from DAO
    return dao.getAllLeaveRequests(); 
}

public int getPendingLeaveCount() {
    Object[][] allLeaves = dao.getAllLeaveRequests();
    int count = 0;
    for (Object[] row : allLeaves) {
        
        if (row != null && row.length >= 8 && "Pending".equalsIgnoreCase(row[7].toString())) {
            count++;
        }
    }
    return count;
}

public boolean updateLeaveStatus(int empId, String startDate, String status) {
    try {
        dao.updateLeaveStatus(empId, startDate, status);
        return true;
    } catch (Exception e) {
        return false;
    }
}

public boolean updateEmployeeProfile(Employee emp) {
    
    if (emp.getPhone() == null || emp.getPhone().trim().isEmpty()) {
        return false; 
    }
    
    
    employeeDao.update(emp);
    return true;
}
}
