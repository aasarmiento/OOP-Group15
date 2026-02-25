package dao;

import java.util.List;
import model.Employee;

public interface EmployeeDAO {
    Employee findById(int empNo);
    Employee findByUsername(String username);
    List<Employee> getAll();
    void add(Employee emp);
    void update(Employee emp);
    void delete(int id);
}