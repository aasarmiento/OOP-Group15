package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.ITTicket;

public class ITTicketDAOImpl implements ITTicketDao {
    private static final String FILE_PATH = "./resources/MotorPH_ITTickets.csv";
    private static final String HEADER = "ID,EmpNo,User,Name,Type,Desc,Status,Created,ResolvedAt,ResolvedBy";

    @Override
    public List<ITTicket> getAllTickets() {
        List<ITTicket> tickets = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return tickets;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip the header line
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                tickets.add(mapLineToTicket(line));
            }
        } catch (IOException e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return tickets;
    }

    @Override
    public boolean saveTicket(ITTicket ticket) {
        return addTicket(ticket);
    }

    @Override
    public boolean addTicket(ITTicket ticket) {
        File file = new File(FILE_PATH);
        boolean isNew = !file.exists() || file.length() == 0;

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            if (isNew) {
                out.println(HEADER);
            }
            out.println(mapTicketToLine(ticket));
            return true;
        } catch (IOException e) {
            System.err.println("Write Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void updateAllTickets(List<ITTicket> tickets) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, false)))) {
            out.println(HEADER);
            for (ITTicket t : tickets) {
                out.println(mapTicketToLine(t));
            }
        } catch (IOException e) {
            System.err.println("DAO Update Error: " + e.getMessage());
        }
    }

    private String mapTicketToLine(ITTicket t) {
        return String.format("%s,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
            t.getTicketId(), 
            t.getEmployeeNo(), 
            t.getUsername(), 
            t.getFullName(),
            t.getIssueType(), 
            t.getDescription().replace("\"", "'"), 
            t.getStatus(), 
            t.getCreatedAt(), 
            t.getResolvedAt(), 
            t.getResolvedBy()
        );
    }

    private ITTicket mapLineToTicket(String line) {
        String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].replace("\"", "").trim();
        }
        return new ITTicket(
            p[0], 
            Integer.parseInt(p[1]), 
            p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9]
        );
    }
}