package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.ITTicket;
import model.Role;
import service.ITSupportService;
import service.ITTicketService;
import java.awt.Dimension;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class ITSupportPanel extends JPanel {
    private final Employee currentUser;
    private final ITSupportService supportService;
    private final ITTicketService ticketService;

    private JTable ticketTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> issueTypeCombo;
    private JTextArea descriptionArea;

// New detail-view fields
private JTextArea ticketDetailsArea;
private JLabel selectedTicketLabel;

    public ITSupportPanel(Employee currentUser,
                          ITSupportService supportService,
                          ITTicketService ticketService) {
        this.currentUser = currentUser;
        this.supportService = supportService;
        this.ticketService = ticketService;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        if (currentUser.getRole() == Role.IT_STAFF) {
            buildITView();
        } else {
            buildEmployeeView();
        }

        refreshTickets();
    }

    private void buildEmployeeView() {
    JLabel title = new JLabel("IT Support");
    title.setFont(new Font("SansSerif", Font.BOLD, 20));
    title.setHorizontalAlignment(SwingConstants.LEFT);

    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(BorderFactory.createTitledBorder("Submit IT Ticket"));

    issueTypeCombo = new JComboBox<>(new String[]{
            "PASSWORD_RESET",
            "ACCOUNT_LOCKED",
            "TECHNICAL_SUPPORT",
            "BUG_REPORT",
            "UI_ERROR"
    });
    issueTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

    descriptionArea = new JTextArea(5, 40);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    JButton submitBtn = new JButton("Submit Ticket");
    submitBtn.addActionListener(e -> submitTicket());

    JPanel top = new JPanel(new BorderLayout(10, 10));
    top.add(title, BorderLayout.NORTH);

    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.add(issueTypeCombo, BorderLayout.NORTH);
    content.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
    content.add(submitBtn, BorderLayout.SOUTH);

    formPanel.add(content);
    top.add(formPanel, BorderLayout.CENTER);

    add(top, BorderLayout.NORTH);
    add(createTicketContentPanel(), BorderLayout.CENTER);
    }

    
    private void buildITView() {
        JLabel title = new JLabel("IT Support Queue");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton resolveBtn = new JButton("Resolve Ticket");
        JButton tempPassBtn = new JButton("Issue Temporary Password");
        JButton unlockBtn = new JButton("Unlock Account");
        JButton refreshBtn = new JButton("Refresh");

        resolveBtn.addActionListener(e -> resolveSelectedTicket());
        tempPassBtn.addActionListener(e -> issueSelectedTemporaryPassword());
        unlockBtn.addActionListener(e -> unlockSelectedAccount());
        refreshBtn.addActionListener(e -> refreshTickets());

        actions.add(resolveBtn);
        actions.add(tempPassBtn);
        actions.add(unlockBtn);
        actions.add(refreshBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);
        top.add(actions, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(createTicketContentPanel(), BorderLayout.CENTER);
    }

private JSplitPane createTicketContentPanel() {
    JScrollPane tableScrollPane = createTicketTablePanel();
    JScrollPane detailsScrollPane = createTicketDetailsPanel();

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailsScrollPane);
    splitPane.setResizeWeight(0.65);
    splitPane.setDividerLocation(300);

    return splitPane;
}

private JScrollPane createTicketTablePanel() {
    String[] columns = {
            "Ticket ID", "Emp No", "Username", "Full Name",
            "Issue Type", "Description", "Status",
            "Created At", "Resolved At", "Resolved By"
    };

    tableModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    ticketTable = new JTable(tableModel);
    ticketTable.setRowHeight(28);
    ticketTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ticketTable.getTableHeader().setReorderingAllowed(false);

    configureTableColumns();

    ticketTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            updateTicketDetailsFromSelection();
        }
    });

    JScrollPane scrollPane = new JScrollPane(ticketTable);
    scrollPane.setBorder(BorderFactory.createTitledBorder("Tickets"));
    scrollPane.setPreferredSize(new Dimension(1000, 320));

    return scrollPane;
}

private JScrollPane createTicketDetailsPanel() {
    JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
    detailsPanel.setBorder(BorderFactory.createTitledBorder("Selected Ticket Details"));

    selectedTicketLabel = new JLabel("No ticket selected");
    selectedTicketLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

    ticketDetailsArea = new JTextArea(8, 40);
    ticketDetailsArea.setEditable(false);
    ticketDetailsArea.setLineWrap(true);
    ticketDetailsArea.setWrapStyleWord(true);
    ticketDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    detailsPanel.add(selectedTicketLabel, BorderLayout.NORTH);
    detailsPanel.add(new JScrollPane(ticketDetailsArea), BorderLayout.CENTER);

    return new JScrollPane(detailsPanel);
}

private void configureTableColumns() {
    int[] widths = {
            70,   // Ticket ID
            80,   // Emp No
            110,  // Username
            160,  // Full Name
            140,  // Issue Type
            280,  // Description
            100,  // Status
            150,  // Created At
            150,  // Resolved At
            140   // Resolved By
    };

    for (int i = 0; i < widths.length; i++) {
        TableColumn column = ticketTable.getColumnModel().getColumn(i);
        column.setPreferredWidth(widths[i]);
    }
}

private void updateTicketDetailsFromSelection() {
    int row = ticketTable.getSelectedRow();
    if (row < 0) {
        clearTicketDetails();
        return;
    }

    selectedTicketLabel.setText("Ticket #" + tableModel.getValueAt(row, 0));

    String details =
            "Employee No : " + safeValue(tableModel.getValueAt(row, 1)) + "\n" +
            "Username    : " + safeValue(tableModel.getValueAt(row, 2)) + "\n" +
            "Full Name   : " + safeValue(tableModel.getValueAt(row, 3)) + "\n" +
            "Issue Type  : " + safeValue(tableModel.getValueAt(row, 4)) + "\n" +
            "Status      : " + safeValue(tableModel.getValueAt(row, 6)) + "\n" +
            "Created At  : " + safeValue(tableModel.getValueAt(row, 7)) + "\n" +
            "Resolved At : " + safeValue(tableModel.getValueAt(row, 8)) + "\n" +
            "Resolved By : " + safeValue(tableModel.getValueAt(row, 9)) + "\n\n" +
            "Description:\n" +
            safeValue(tableModel.getValueAt(row, 5));

    ticketDetailsArea.setText(details);
    ticketDetailsArea.setCaretPosition(0);
}

private void clearTicketDetails() {
    if (selectedTicketLabel != null) {
        selectedTicketLabel.setText("No ticket selected");
    }
    if (ticketDetailsArea != null) {
        ticketDetailsArea.setText("");
    }
}

private String safeValue(Object value) {
    return value == null ? "" : value.toString();
}

    private void submitTicket() {
        String issueType = (String) issueTypeCombo.getSelectedItem();
        String description = descriptionArea.getText().trim();

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a description.");
            return;
        }

        boolean submitted = ticketService.submitTicket(currentUser, issueType, description);

        if (submitted) {
            JOptionPane.showMessageDialog(this, "Ticket submitted successfully.");
            descriptionArea.setText("");
            refreshTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to submit ticket.");
        }
    }

    private void refreshTickets() {
    tableModel.setRowCount(0);

    List<ITTicket> tickets;
    if (currentUser.getRole() == Role.IT_STAFF) {
        tickets = ticketService.getAllTickets();
    } else {
        tickets = ticketService.getTicketsByEmployee(currentUser.getEmpNo());
    }

    for (ITTicket t : tickets) {
        tableModel.addRow(new Object[]{
                t.getTicketId(),
                t.getEmployeeNo(),
                t.getUsername(),
                t.getFullName(),
                t.getIssueType(),
                t.getDescription(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getResolvedAt(),
                t.getResolvedBy()
        });
    }

    clearTicketDetails();
    }

    private ITTicket getSelectedTicket() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return null;
        }

        int ticketId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        return ticketService.getTicketById(ticketId);
    }

    private void resolveSelectedTicket() {
        ITTicket ticket = getSelectedTicket();
        if (ticket == null) return;

        boolean success = ticketService.resolveTicket(ticket.getTicketId(), currentUser.getFullName());
        if (success) {
            JOptionPane.showMessageDialog(this, "Ticket resolved.");
            refreshTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to resolve ticket.");
        }
    }


    private void unlockSelectedAccount() {
        ITTicket ticket = getSelectedTicket();
        if (ticket == null) return;

        boolean unlocked = supportService.unlockAccount(ticket.getEmployeeNo());
        if (unlocked) {
            ticketService.resolveTicket(ticket.getTicketId(), currentUser.getFullName());
            ticketService.appendAudit("Account unlocked for Employee #" + ticket.getEmployeeNo()
                    + " by " + currentUser.getFullName());
            JOptionPane.showMessageDialog(this, "Account unlocked successfully.");
            refreshTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to unlock account.");
        }
    }

    private void issueSelectedTemporaryPassword() {
        ITTicket ticket = getSelectedTicket();
        if (ticket == null) return;

        String tempPassword = supportService.issueTemporaryPassword(ticket.getEmployeeNo());

        if (tempPassword != null) {
            ticketService.resolveTicket(ticket.getTicketId(), currentUser.getFullName());
            ticketService.appendAudit("Temporary password issued for Employee #" +
                    ticket.getEmployeeNo() + " by " + currentUser.getFullName());

            JOptionPane.showMessageDialog(
                    this,
                    "Temporary password generated:\n" + tempPassword +
                    "\n\nGive this to the employee. They will be forced to change it on next login."
            );

            refreshTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to issue temporary password.");
        }
    }

    
}