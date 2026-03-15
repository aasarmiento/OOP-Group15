package motorph3;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.CSVHandler;
import dao.EmployeeDAO;
import dao.ITTicketDAOImpl;
import dao.ITTicketDao;
import dao.UserLibrary;
import service.EmployeeManagementService;
import service.ITSupportService;
import ui.LoginPanel;

public class Main {
    public static void main(String[] args) {
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler();

        EmployeeManagementService service = new EmployeeManagementService(employeeDao, attendanceDao);
        UserLibrary auth = new UserLibrary(employeeDao);

        ITTicketDao itTicketDao = new ITTicketDAOImpl();
        ITSupportService itSupportService = new ITSupportService(itTicketDao, employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            new LoginPanel(service, attendanceDao, auth, itSupportService).setVisible(true);
        });
    }
}