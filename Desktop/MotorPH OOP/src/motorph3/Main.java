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
        //  Initialize DAOs
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler(); 
        
        
        //  Initialize Service 
        EmployeeManagementService service = new EmployeeManagementService(employeeDao, attendanceDao);
        
        //  Initialize Auth
        UserLibrary auth = new UserLibrary(employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            
            new LoginPanel(service, attendanceDao, auth).setVisible(true);
        });
    }
}