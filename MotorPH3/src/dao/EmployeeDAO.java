package dao;

import java.util.List;
import model.Employee;

public interface EmployeeDAO {
    Employee findById(int empNo);
    Employee findByUsername(String username);
    List<Employee> getAll();
    public boolean addEmployee(Employee newEmp);
    void update(Employee emp);
    
    public boolean deleteEmployee(int id);

    Object[][] getAttendanceByMonth(int empId, String month);
    void recordAttendance(int empId, String type); // type: "IN" or "OUT"

    // Update this line to include 'String type'
    void applyForLeave(int empId, String type, String startDate, String endDate, String reason);
    
    // Ensure this returns the 2D array for the table
    Object[][] getLeaveStatusByEmpId(int empId);

    Object[][] getAllLeaveRequests(); 
    void updateLeaveStatus(int empId, String startDate, String newStatus);
    // Add this inside the EmployeeDAO interface
Object[][] getAttendanceById(int empId);

public boolean updateEmployee(Employee updatedEmp);

}