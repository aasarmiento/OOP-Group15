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
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler(); 
        
        
        EmployeeManagementService service = new EmployeeManagementService(employeeDao, attendanceDao);
        
        UserLibrary auth = new UserLibrary(employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            
            new LoginPanel(service, attendanceDao, auth).setVisible(true);
        });
    }
}