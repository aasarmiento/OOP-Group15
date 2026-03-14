package service;

import dao.EmployeeDAO;
import dao.ITTicketDao; 
import java.util.List;
import java.util.stream.Collectors;
import model.Employee;
import model.ITStaff;
import model.ITTicket;
import model.Role;

public class ITSupportService {
    private final ITTicketDao itTicketDao; 
    private final EmployeeDAO employeeDao;

    public ITSupportService(ITTicketDao itTicketDao, EmployeeDAO employeeDao) {
        this.itTicketDao = itTicketDao;
        this.employeeDao = employeeDao;
    }

    public List<ITTicket> getOpenTickets() {
        return itTicketDao.getAllTickets().stream()
                .filter(t -> !"RESOLVED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ITTicket> getAllTickets() {
        return itTicketDao.getAllTickets();
    }

    public void resolveTicket(String ticketId, Employee performer) {
        // ABSTRACTION CHECK: Verify capability via the Model type
        boolean isAuthorized = false;

        if (performer instanceof ITStaff) {
            isAuthorized = ((ITStaff) performer).canApproveTechnicalTickets();
        } else if (performer.getRole() == Role.ADMIN) {
            isAuthorized = true;
        }

        if (!isAuthorized) {
            throw new SecurityException("Unauthorized: Only IT Staff can resolve tickets.");
        }

        List<ITTicket> all = itTicketDao.getAllTickets();
        boolean found = false;

        for (ITTicket t : all) {
            if (t.getTicketId().equals(ticketId)) {
                String adminName = performer.getFirstName() + " " + performer.getLastName();
                t.setStatus("RESOLVED");
                t.setResolvedBy(adminName);
                t.setResolvedAt(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                if ("ACCOUNT_LOCKED".equals(t.getIssueType()) || "LOGIN_ISSUE".equals(t.getIssueType())) {
                    employeeDao.unlockAccount(t.getEmployeeNo());
                }
                found = true;
                break;
            }
        }

        if (found) {
            itTicketDao.updateAllTickets(all); 
        }
    }

    public boolean submitNewTicket(int empNo, String user, String name, String type, String desc) {
        List<ITTicket> currentTickets = itTicketDao.getAllTickets();
        
        long employeeTicketCount = currentTickets.stream()
                .filter(t -> t.getEmployeeNo() == empNo)
                .count();

        String formattedId = String.format("IT-%d-%02d", empNo, (int)employeeTicketCount + 1);

        String now = java.time.LocalDateTime.now()
                     .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ITTicket newTicket = new ITTicket(
            formattedId, empNo, user, name, type, desc, 
            "OPEN", now, "N/A", "N/A"
        );

        return itTicketDao.addTicket(newTicket);
    }

    public List<ITTicket> getTicketsByEmployee(int empNo) {
        return itTicketDao.getAllTickets().stream()
                .filter(t -> t.getEmployeeNo() == empNo)
                .collect(Collectors.toList());
    }
}