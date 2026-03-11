package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.ITTicket;
import service.ITSupportService;

public class ITApprovalPanel extends JPanel {
    private final ITSupportService itService;
    private final Employee currentUser;
    private JTable table;
    private DefaultTableModel model;

    public ITApprovalPanel(ITSupportService service, Employee user) {
        this.itService = service;
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JLabel lblTitle = new JLabel("IT Ticket Approval & Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(128, 0, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"ID", "Emp No", "User", "Issue Type", "Status", "Created At"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnResolve = new JButton("Mark as Resolved");
        btnResolve.setBackground(new Color(0, 102, 51));
        btnResolve.setForeground(Color.WHITE);

        btnResolve.addActionListener(e -> handleResolve());
        buttonPanel.add(btnResolve);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshUI();
    }

    // Inside ITApprovalPanel.java refreshUI method
public void refreshUI() {
    model.setRowCount(0);
    // Suggestion: Use getAllTickets() if you want to see the ones from your CSV 
    // because they are all already "RESOLVED"
    List<ITTicket> tickets = itService.getAllTickets(); 
    
    for (ITTicket t : tickets) {
        model.addRow(new Object[]{
            t.getTicketId(),
            t.getEmployeeNo(),
            t.getFullName(),
            t.getIssueType(),
            t.getStatus(), // This will show "RESOLVED" based on your CSV
            t.getCreatedAt()
        });
    }
}

    private void handleResolve() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ticket to resolve.");
            return;
        }

        int ticketId = (int) table.getValueAt(row, 0);
        String adminName = currentUser.getFirstName() + " " + currentUser.getLastName();
        
        // Service handles the business rules (like unlocking accounts)
        itService.resolveTicket(ticketId, adminName); 
        JOptionPane.showMessageDialog(this, "Ticket #" + ticketId + " Resolved.");
        refreshUI();
    }
}