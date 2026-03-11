package service;

import dao.EmployeeDAO;
import dao.ITTicketDao; // Ensure this import matches your package structure
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import model.ITTicket;

public class ITSupportService {
    private final ITTicketDao itTicketDao; // This is the name we will use
    private final EmployeeDAO employeeDao;

    public ITSupportService(ITTicketDao itTicketDao, EmployeeDAO employeeDao) {
        this.itTicketDao = itTicketDao;
        this.employeeDao = employeeDao;
    }

    public List<ITTicket> getOpenTickets() {
        // Use itTicketDao, NOT ticketDao
        return itTicketDao.getAllTickets().stream()
                .filter(t -> !"RESOLVED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ITTicket> getAllTickets() {
        return itTicketDao.getAllTickets();
    }

    public void resolveTicket(int ticketId, String adminName) {
        List<ITTicket> all = itTicketDao.getAllTickets();
        for (ITTicket t : all) {
            if (t.getTicketId() == ticketId) {
                t.setStatus("RESOLVED");
                t.setResolvedBy(adminName);
                t.setResolvedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // If it was a lock issue, tell EmployeeDAO to fix the account
                if ("ACCOUNT_LOCKED".equals(t.getIssueType()) || "LOGIN_ISSUE".equals(t.getIssueType())) {
                    employeeDao.unlockAccount(t.getEmployeeNo());
                }
            }
        }
        itTicketDao.updateAllTickets(all);
    }

public boolean submitNewTicket(int empNo, String user, String name, String type, String desc) {
    // 1. Logic: Get current tickets to determine the next ID
    List<ITTicket> currentTickets = itTicketDao.getAllTickets();
    int nextId = currentTickets.isEmpty() ? 1 : 
                 currentTickets.get(currentTickets.size() - 1).getTicketId() + 1;

    // 2. Logic: Set current timestamp
    String now = java.time.LocalDateTime.now()
                 .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // 3. OOP: Wrap data into a Model object
    ITTicket newTicket = new ITTicket(
        nextId, empNo, user, name, type, desc, 
        "OPEN", now, "N/A", "N/A"
    );

    // 4. N-Tier: Pass the object to the DAO
    return itTicketDao.addTicket(newTicket);
}

// Inside ITSupportService.java

public List<ITTicket> getTicketsByEmployee(int empNo) {
    // N-Tier: Service asks DAO for all, then filters by the specific Employee No
    return itTicketDao.getAllTickets().stream()
            .filter(t -> t.getEmployeeNo() == empNo)
            .collect(Collectors.toList());
}

}