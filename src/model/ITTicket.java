package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ITTicket {
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int ticketId;
    private int employeeNo;
    private String username;
    private String fullName;
    private String issueType;
    private String description;
    private String status;
    private String createdAt;
    private String resolvedAt;
    private String resolvedBy;

    // Constructor for NEW tickets
    public ITTicket(int ticketId, int employeeNo, String username, String fullName,
                    String issueType, String description) {
        this.ticketId = ticketId;
        this.employeeNo = employeeNo;
        this.username = username;
        this.fullName = fullName;
        this.issueType = issueType;
        this.description = description;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        this.resolvedAt = "";
        this.resolvedBy = "";
    }

    // Constructor for LOADING from CSV
    public ITTicket(int ticketId, int employeeNo, String username, String fullName,
                    String issueType, String description, String status,
                    String createdAt, String resolvedAt, String resolvedBy) {
        this.ticketId = ticketId;
        this.employeeNo = employeeNo;
        this.username = username;
        this.fullName = fullName;
        this.issueType = issueType;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    public void markResolved(String resolverName) {
        this.status = "RESOLVED";
        this.resolvedBy = resolverName;
        this.resolvedAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    // Inside ITTicket.java
public void setStatus(String status) {
    this.status = status;
}

public void setResolvedBy(String resolvedBy) {
    this.resolvedBy = resolvedBy;
}

public void setResolvedAt(String resolvedAt) {
    this.resolvedAt = resolvedAt;
}

    // Getters and Setters
    public int getTicketId() { return ticketId; }
    public int getEmployeeNo() { return employeeNo; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getIssueType() { return issueType; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getResolvedAt() { return resolvedAt; }
    public String getResolvedBy() { return resolvedBy; }



    // Inside ITTicket.java
public String toCSV() {
    return String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
        ticketId, employeeNo, username, fullName, 
        issueType, description, status, createdAt, 
        resolvedAt, resolvedBy);
}
}