package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.ITTicket;

public class ITTicketDAOImpl implements ITTicketDao {
    private static final String FILE_PATH = "resources/MotorPH_ITTickets.csv";

@Override
public List<ITTicket> getAllTickets() {
    List<ITTicket> tickets = new ArrayList<>();
    File file = new File(FILE_PATH);
    if (!file.exists()) return tickets;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        boolean skipHeader = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("timestamp,event") || line.contains("Ticket #")) {
                break; 
            }

            if (skipHeader) {
                skipHeader = false;
                continue;
            }

            tickets.add(mapLineToTicket(line));
        }
    } catch (IOException e) {
        System.err.println("Read Error: " + e.getMessage());
    }
    return tickets;
}

  @Override
public boolean saveTicket(ITTicket ticket) {
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("resources/MotorPH_ITTickets.csv", true)))) {
        out.println(ticket.toCSV()); 
        return true;
    } catch (IOException e) {
        return false;
    }
}

    @Override
    public void updateAllTickets(List<ITTicket> tickets) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (ITTicket t : tickets) {
                out.println(mapTicketToLine(t));
            }
        } catch (IOException e) {
            System.err.println("DAO Update Error: " + e.getMessage());
        }
    }

    private String mapTicketToLine(ITTicket t) {
        return String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
            t.getTicketId(), t.getEmployeeNo(), t.getUsername(), t.getFullName(),
            t.getIssueType(), t.getDescription(), t.getStatus(), t.getCreatedAt(),
            t.getResolvedAt(), t.getResolvedBy());
    }

    private ITTicket mapLineToTicket(String line) {
        String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].replace("\"", "").trim();
        }
        return new ITTicket(
            Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], p[3], 
            p[4], p[5], p[6], p[7], p[8], p[9]
        );
    }

@Override
public boolean addTicket(ITTicket ticket) {
    String csvLine = String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
        ticket.getTicketId(),
        ticket.getEmployeeNo(),
        ticket.getUsername(),
        ticket.getFullName(),
        ticket.getIssueType(),
        ticket.getDescription(),
        ticket.getStatus(),
        ticket.getCreatedAt(),
        ticket.getResolvedAt(),
        ticket.getResolvedBy()
    );

    try {
        java.nio.file.Files.write(
            java.nio.file.Paths.get(FILE_PATH), 
            csvLine.getBytes(), 
            java.nio.file.StandardOpenOption.APPEND
        );
        return true;
    } catch (IOException e) {
        System.err.println("Error appending ticket: " + e.getMessage());
        return false;
    }
}


}