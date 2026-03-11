package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.ITTicket;

public class ITTicketCSVHandler implements ITTicketDao {
    private static final String FILE_PATH = "resources/MotorPH_ITTickets.csv";
    private static final String HEADER = "ID,EmpNo,User,Name,Type,Desc,Status,Created,ResolvedAt,ResolvedBy";

    @Override
    public List<ITTicket> getAllTickets() {
        List<ITTicket> tickets = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists()) return tickets;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip CSV Header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 10) {
                    tickets.add(new ITTicket(
                        Integer.parseInt(data[0].trim()), 
                        Integer.parseInt(data[1].trim()), 
                        clean(data[2]), 
                        clean(data[3]), 
                        clean(data[4]), 
                        clean(data[5]), 
                        clean(data[6]), 
                        clean(data[7]), 
                        clean(data[8]), 
                        clean(data[9])
                    ));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("DAO Read Error: " + e.getMessage());
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
            out.println(formatToCsv(ticket));
            return true;
        } catch (IOException e) {
            System.err.println("DAO Write Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void updateAllTickets(List<ITTicket> tickets) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, false)))) {
            out.println(HEADER);
            for (ITTicket t : tickets) {
                out.println(formatToCsv(t));
            }
        } catch (IOException e) {
            System.err.println("DAO Update Error: " + e.getMessage());
        }
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace("\"", "").trim();
    }

    private String formatToCsv(ITTicket t) {
        return String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
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
}