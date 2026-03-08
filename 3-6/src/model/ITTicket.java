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

    /**
     * Constructor for NEW tickets created from the UI
     */
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

    /**
     * Constructor for LOADING tickets from CSV
     */
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

    // ---------- Business helper ----------
    public void markResolved(String resolverName) {
        this.status = "RESOLVED";
        this.resolvedBy = resolverName;
        this.resolvedAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    // ---------- CSV helpers ----------
    public String toCSV() {
        return ticketId + "," +
               employeeNo + "," +
               escape(username) + "," +
               escape(fullName) + "," +
               escape(issueType) + "," +
               escape(description) + "," +
               escape(status) + "," +
               escape(createdAt) + "," +
               escape(resolvedAt) + "," +
               escape(resolvedBy);
    }

    public static ITTicket fromCSV(String line) {
        String[] parts = splitCSV(line);

        if (parts.length < 10) {
            throw new IllegalArgumentException("Invalid ITTicket CSV row: " + line);
        }

        return new ITTicket(
                parseIntSafe(parts[0]),
                parseIntSafe(parts[1]),
                unescape(parts[2]),
                unescape(parts[3]),
                unescape(parts[4]),
                unescape(parts[5]),
                unescape(parts[6]),
                unescape(parts[7]),
                unescape(parts[8]),
                unescape(parts[9])
        );
    }

    private static String escape(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String unescape(String value) {
        if (value == null) return "";
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned.replace("\"\"", "\"");
    }

    private static String[] splitCSV(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---------- Getters and Setters ----------
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(int employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
}