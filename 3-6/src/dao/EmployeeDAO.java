package dao;

import java.util.List;
import model.Employee;
import model.LeaveRequest;

public interface EmployeeDAO {

    Employee findById(int empNo);
    Employee findByUsername(String username);
    List<Employee> getAll();
    boolean addEmployee(Employee newEmp);
    boolean update(Employee emp);
    boolean deleteEmployee(int id);
    boolean create(Employee emp);

    void applyForLeave(int id, String type, String start, String end, String reason);
    Object[][] getLeaveStatusByEmpId(int empId);
    Object[][] getAllLeaveRequests();
    void updateLeaveStatus(String reqId, String stat);
    List<LeaveRequest> getAllLeaveRequestsList();

    void updateEmployeeStatus(int id, String stat);
    void saveNewPassword(int id, String pass);

    int getFailedAttempts(int id);
    boolean isLocked(int id);
    void updateLoginState(int id, int failedAttempts, boolean locked);
    void resetLoginState(int id);

    boolean mustChangePassword(int id);
    void setMustChangePassword(int id, boolean required);

    default int getNextAvailableId() {
        List<Employee> all = getAll();
        if (all == null || all.isEmpty()) return 10001;
        return all.stream()
                  .mapToInt(Employee::getEmpNo)
                  .max()
                  .getAsInt() + 1;
    }

    Employee getById(int id);
    int getLastEmployeeNumber();
}