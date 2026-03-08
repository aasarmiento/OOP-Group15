package service;

import dao.EmployeeDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import model.ITTicket;

public class ITTicketService {
    private static final String TICKETS_CSV = "resources/MotorPH_ITTickets.csv";
    private static final String AUDIT_CSV = "resources/MotorPH_AuditLog.csv";
    private static final DateTimeFormatter AUDIT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EmployeeDAO employeeDAO;

    public ITTicketService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
        ensureFilesExist();
    }

    public boolean submitTicket(Employee employee, String issueType, String description) {
        if (employee == null) return false;
        if (issueType == null || issueType.trim().isEmpty()) return false;
        if (description == null || description.trim().isEmpty()) return false;

        ITTicket ticket = new ITTicket(
                getNextTicketId(),
                employee.getEmpNo(),
                buildUsername(employee),
                employee.getFullName(),
                issueType.trim(),
                description.trim()
        );

        boolean saved = appendTicket(ticket);
        if (saved) {
            appendAudit("Ticket #" + ticket.getTicketId() + " created for Employee #" +
                    employee.getEmpNo() + " (" + employee.getFullName() + "), issue=" + issueType);
        }
        return saved;
    }

    public boolean submitTicketByUsername(String username, String issueType, String description) {
        if (username == null || username.trim().isEmpty()) return false;
        Employee employee = employeeDAO.findByUsername(username.trim());
        return submitTicket(employee, issueType, description);
    }

    public boolean submitTicketByEmployeeNo(int empNo, String issueType, String description) {
        Employee employee = employeeDAO.findById(empNo);
        return submitTicket(employee, issueType, description);
    }

    public List<ITTicket> getAllTickets() {
        ensureFilesExist();
        List<ITTicket> tickets = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(TICKETS_CSV))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    tickets.add(ITTicket.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Skipping invalid ticket row: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading IT tickets: " + e.getMessage());
        }

        return tickets;
    }

    public List<ITTicket> getTicketsByEmployee(int empNo) {
        List<ITTicket> filtered = new ArrayList<>();
        for (ITTicket ticket : getAllTickets()) {
            if (ticket.getEmployeeNo() == empNo) {
                filtered.add(ticket);
            }
        }
        return filtered;
    }

    public ITTicket getTicketById(int ticketId) {
        for (ITTicket ticket : getAllTickets()) {
            if (ticket.getTicketId() == ticketId) {
                return ticket;
            }
        }
        return null;
    }

    public boolean resolveTicket(int ticketId, String resolvedBy) {
        List<ITTicket> tickets = getAllTickets();
        boolean updated = false;

        for (ITTicket ticket : tickets) {
            if (ticket.getTicketId() == ticketId) {
                ticket.markResolved(resolvedBy);
                updated = true;
                break;
            }
        }

        if (!updated) return false;

        boolean saved = saveAllTickets(tickets);
        if (saved) {
            appendAudit("Ticket #" + ticketId + " resolved by " + resolvedBy);
        }
        return saved;
    }

    public int getNextTicketId() {
        int max = 0;
        for (ITTicket ticket : getAllTickets()) {
            if (ticket.getTicketId() > max) {
                max = ticket.getTicketId();
            }
        }
        return max + 1;
    }

    public void appendAudit(String event) {
        ensureFilesExist();
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_CSV, true))) {
            String timestamp = LocalDateTime.now().format(AUDIT_FORMAT);
            pw.println("\"" + timestamp + "\",\"" + event.replace("\"", "\"\"") + "\"");
        } catch (IOException e) {
            System.err.println("Error writing audit log: " + e.getMessage());
        }
    }

    private boolean appendTicket(ITTicket ticket) {
        ensureFilesExist();
        try (PrintWriter pw = new PrintWriter(new FileWriter(TICKETS_CSV, true))) {
            pw.println(ticket.toCSV());
            return true;
        } catch (IOException e) {
            System.err.println("Error writing IT ticket: " + e.getMessage());
            return false;
        }
    }

    private boolean saveAllTickets(List<ITTicket> tickets) {
        ensureFilesExist();
        try (PrintWriter pw = new PrintWriter(new FileWriter(TICKETS_CSV))) {
            pw.println("ticket_id,employee_no,username,full_name,issue_type,description,status,created_at,resolved_at,resolved_by");
            for (ITTicket ticket : tickets) {
                pw.println(ticket.toCSV());
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving IT tickets: " + e.getMessage());
            return false;
        }
    }

    private String buildUsername(Employee employee) {
        if (employee == null) return "";
        String first = employee.getFirstName() == null ? "" : employee.getFirstName().trim();
        String last = employee.getLastName() == null ? "" : employee.getLastName().trim();

        if (first.isEmpty() && last.isEmpty()) return "";
        if (first.isEmpty()) return last;

        return first.substring(0, 1).toUpperCase() + last.replace(" ", "");
    }

    private void ensureFilesExist() {
        try {
            File ticketFile = new File(TICKETS_CSV);
            if (!ticketFile.exists()) {
                ticketFile.getParentFile().mkdirs();
                try (PrintWriter pw = new PrintWriter(new FileWriter(ticketFile))) {
                    pw.println("ticket_id,employee_no,username,full_name,issue_type,description,status,created_at,resolved_at,resolved_by");
                }
            }

            File auditFile = new File(AUDIT_CSV);
            if (!auditFile.exists()) {
                auditFile.getParentFile().mkdirs();
                try (PrintWriter pw = new PrintWriter(new FileWriter(auditFile))) {
                    pw.println("timestamp,event");
                }
            }
        } catch (IOException e) {
            System.err.println("Error ensuring IT support files: " + e.getMessage());
        }
    }
}