package dao;

import java.util.List;
import model.Employee;

public interface EmployeeDAO {
    // READ (All)
    List<Employee> getAll();
    
    // READ (Single) - Useful for logging in or finding specific records
    Employee findById(int empNo);
    
    // CREATE
    void add(Employee emp);
    
    // UPDATE - Necessary for changing employee details or salary info
    void update(Employee emp);
    
    // DELETE - Necessary for removing records
    void delete(int empNo);
}