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
            br.readLine();
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
        } catch (Exception e) {
            System.err.println("ITTicketCSVHandler read error: " + e.getMessage());
        }

        return tickets;
    }

    @Override
    public ITTicket findById(int ticketId) {
        return getAllTickets().stream()
                .filter(t -> t.getTicketId() == ticketId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ITTicket> findByEmployeeNo(int empNo) {
        List<ITTicket> result = new ArrayList<>();
        for (ITTicket t : getAllTickets()) {
            if (t.getEmployeeNo() == empNo) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public boolean addTicket(ITTicket ticket) {
        File file = new File(FILE_PATH);
        boolean isNew = !file.exists() || file.length() == 0;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (isNew) pw.println(HEADER);
            pw.println(ticket.toCSV());
            return true;
        } catch (IOException e) {
            System.err.println("ITTicketCSVHandler add error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateTicket(ITTicket updatedTicket) {
        List<ITTicket> tickets = getAllTickets();
        boolean found = false;

        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getTicketId() == updatedTicket.getTicketId()) {
                tickets.set(i, updatedTicket);
                found = true;
                break;
            }
        }

        return found && writeAll(tickets);
    }

    @Override
    public boolean deleteTicket(int ticketId) {
        List<ITTicket> tickets = getAllTickets();
        boolean removed = tickets.removeIf(t -> t.getTicketId() == ticketId);
        return removed && writeAll(tickets);
    }

    @Override
    public int getNextTicketId() {
        return getAllTickets().stream()
                .mapToInt(ITTicket::getTicketId)
                .max()
                .orElse(0) + 1;
    }

    private boolean writeAll(List<ITTicket> tickets) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
            pw.println(HEADER);
            for (ITTicket t : tickets) {
                pw.println(t.toCSV());
            }
            return true;
        } catch (IOException e) {
            System.err.println("ITTicketCSVHandler writeAll error: " + e.getMessage());
            return false;
        }
    }

    private String clean(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }
}