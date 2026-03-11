package dao;

import java.util.List;
import model.ITTicket;

public interface ITTicketDao {
    // These are the "Supertype" methods the implementation MUST match
    List<ITTicket> getAllTickets();
    boolean saveTicket(ITTicket ticket);
    void updateAllTickets(List<ITTicket> tickets);


    public boolean addTicket(ITTicket ticket);
}