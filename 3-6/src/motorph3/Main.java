package motorph3;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.CSVHandler;
import dao.EmployeeDAO; 
import dao.UserLibrary;
import service.EmployeeManagementService;
import ui.LoginPanel;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize DAOs
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler(); 
        
        // 2. Initialize Service (FIXED: Added variable declaration 'service')
        EmployeeManagementService service = new EmployeeManagementService(employeeDao, attendanceDao);
        
        // 3. Initialize Auth
        UserLibrary auth = new UserLibrary(employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            // FIXED: Passed 'service' instead of 'employeeService'
            // Ensure LoginPanel constructor accepts (EmployeeManagementService, AttendanceDAO, UserLibrary)
            new LoginPanel(service, attendanceDao, auth).setVisible(true);
        });
    }
}