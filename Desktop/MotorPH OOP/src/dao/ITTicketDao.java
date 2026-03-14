package dao;

import java.util.List;
import model.ITTicket;

public interface ITTicketDao {
    List<ITTicket> getAllTickets();
    ITTicket findById(int ticketId);
    List<ITTicket> findByEmployeeNo(int empNo);

    boolean addTicket(ITTicket ticket);
    boolean updateTicket(ITTicket ticket);
    boolean deleteTicket(int ticketId);

    int getNextTicketId();
}