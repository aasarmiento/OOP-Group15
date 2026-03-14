package service;

import dao.EmployeeDAO;
import dao.ITTicketDao;
import java.util.List;
import java.util.stream.Collectors;
import model.Employee;
import model.ITTicket;

public class ITSupportService {
    private final ITTicketDao itTicketDao;
    private final EmployeeDAO employeeDao;

    public ITSupportService(ITTicketDao itTicketDao, EmployeeDAO employeeDao) {
        this.itTicketDao = itTicketDao;
        this.employeeDao = employeeDao;
    }

    public List<ITTicket> getAllTickets() {
        return itTicketDao.getAllTickets();
    }

    public List<ITTicket> getOpenTickets() {
        return itTicketDao.getAllTickets().stream()
                .filter(t -> !"RESOLVED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ITTicket> getTicketsByEmployee(int empNo) {
        return itTicketDao.findByEmployeeNo(empNo);
    }

    public ITTicket getTicketById(int ticketId) {
        return itTicketDao.findById(ticketId);
    }

    public boolean submitNewTicket(int empNo, String username, String fullName, String type, String desc) {
        String normalizedType = normalizeIssueType(type);
        ITTicket ticket = new ITTicket(
                itTicketDao.getNextTicketId(),
                empNo,
                username,
                fullName,
                normalizedType,
                desc
        );
        return itTicketDao.addTicket(ticket);
    }

    public boolean submitForgotPasswordTicket(String username, String description) {
        Employee emp = employeeDao.findByUsername(username);
        if (emp == null) return false;

        String desc = (description == null || description.trim().isEmpty())
                ? "Forgot password request."
                : description.trim();

        ITTicket ticket = new ITTicket(
                itTicketDao.getNextTicketId(),
                emp.getEmpNo(),
                emp.getUsername(),
                emp.getFullName(),
                "FORGOT_PASSWORD",
                desc
        );
        return itTicketDao.addTicket(ticket);
    }

    public boolean submitForgotPasswordTicketByEmployeeId(int empNo) {
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) {
            return false;
        }

        ITTicket ticket = new ITTicket(
                itTicketDao.getNextTicketId(),
                emp.getEmpNo(),
                emp.getUsername(),
                emp.getFullName(),
                "FORGOT_PASSWORD",
                "Forgot password request."
        );

        return itTicketDao.addTicket(ticket);
    }

    public void resolveTicket(int ticketId, Employee performer) {
        assertITOrAdmin(performer);

        ITTicket ticket = itTicketDao.findById(ticketId);
        if (ticket == null) return;

        ticket.markResolved(performer.getFullName());

        if ("ACCOUNT_LOCKED".equalsIgnoreCase(ticket.getIssueType())
                || "LOGIN_ISSUE".equalsIgnoreCase(ticket.getIssueType())
                || "FORGOT_PASSWORD".equalsIgnoreCase(ticket.getIssueType())) {
            employeeDao.unlockAccount(ticket.getEmployeeNo());
        }

        itTicketDao.updateTicket(ticket);
    }

    public void reopenTicket(int ticketId, Employee performer) {
        assertITOrAdmin(performer);

        ITTicket ticket = itTicketDao.findById(ticketId);
        if (ticket == null) return;

        ticket.reopen();
        itTicketDao.updateTicket(ticket);
    }

    public void deleteTicket(int ticketId, Employee performer) {
        assertITOrAdmin(performer);
        itTicketDao.deleteTicket(ticketId);
    }

    public boolean resetEmployeePassword(int empNo, String newPassword, Employee performer) {
        assertITOrAdmin(performer);

        if (newPassword == null || newPassword.trim().length() < 8) {
            return false;
        }

        employeeDao.saveNewPassword(empNo, newPassword.trim());
        employeeDao.unlockAccount(empNo);
        return true;
    }

    public String generateTemporaryPasswordForEmployee(int empNo, Employee performer) {
        assertITOrAdmin(performer);

        String tempPassword = generateTempPassword();

        boolean updated = employeeDao.setPasswordResetState(empNo, tempPassword, true, true);
        if (!updated) {
            return null;
        }

        employeeDao.unlockAccount(empNo);
        return tempPassword;
    }

    private void assertITOrAdmin(Employee performer) {
        String role = performer.getRole().name();
        if (!role.equalsIgnoreCase("IT_STAFF") && !role.equalsIgnoreCase("ADMIN")) {
            throw new SecurityException("Unauthorized: Only IT Staff/Admin can perform this action.");
        }
    }

    private String normalizeIssueType(String type) {
        if (type == null) return "TECHNICAL_SUPPORT";
        return type.trim().toUpperCase().replace(" ", "_");
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();

        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return sb.toString();
    }
}