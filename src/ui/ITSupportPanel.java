package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.ITTicket;
import service.ITSupportService;

public class ITSupportPanel extends JPanel {
    private final ITSupportService itService;
    private final Employee currentUser;
    
    private DefaultTableModel model;
    private JTable table;

    private JLabel lblActiveTicketsCount; // Add this line
    
    // Components for Form/Details
    private JComboBox<String> cbIssueType;
    private JTextArea txtDescription, txtDetailView;
    private JLabel lblDetailID, lblDetailStatus, lblDetailDate;
    
    private final String[] cols = {"ID", "Emp No", "Username", "Type", "Status", "Created", "Resolved By", "Description"};

    public ITSupportPanel(ITSupportService itService, Employee user) {
        this.itService = itService;
        this.currentUser = user;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        
        // Table Setup (Non-editable)
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        
        // Layout Construction
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        
        refreshUI();
    }

    private JPanel createMainContentArea() {
        JPanel centerWrapper = new JPanel(new BorderLayout(20, 20));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // LEFT: Ticket Submission Form
        centerWrapper.add(createTicketForm(), BorderLayout.WEST);

        // RIGHT: Table and Details
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(new Color(128, 0, 0)); // MotorPH Red
        table.setRowHeight(30);
        setupStatusRenderer();
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        
        rightPanel.add(tableContainer, BorderLayout.CENTER);
        rightPanel.add(createDetailsPanel(), BorderLayout.SOUTH);

        centerWrapper.add(rightPanel, BorderLayout.CENTER);
        return centerWrapper;
    }

    private JPanel createTicketForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Submit New Ticket"));
        form.setPreferredSize(new Dimension(280, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        cbIssueType = new JComboBox<>(new String[]{"Forgot Password", "Account Locked", "Technical Support", "UI Error"});
        txtDescription = new JTextArea(5, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnSubmit = new JButton("SUBMIT TICKET");
        styleButton(btnSubmit, new Color(128, 0, 0));

        gbc.gridy = 0; form.add(new JLabel("Issue Type:"), gbc);
        gbc.gridy = 1; form.add(cbIssueType, gbc);
        gbc.gridy = 2; form.add(new JLabel("Description:"), gbc);
        gbc.gridy = 3; form.add(new JScrollPane(txtDescription), gbc);
        gbc.gridy = 4; gbc.insets = new Insets(20, 10, 10, 10);
        form.add(btnSubmit, gbc);

        btnSubmit.addActionListener(e -> handleSubmit());

        return form;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Ticket Information"));
        detailsPanel.setPreferredSize(new Dimension(0, 180));

        JPanel infoGrid = new JPanel(new GridLayout(1, 3));
        lblDetailID = new JLabel("Ticket: --");
        lblDetailStatus = new JLabel("Status: --");
        lblDetailDate = new JLabel("Created: --");
        infoGrid.add(lblDetailID); infoGrid.add(lblDetailStatus); infoGrid.add(lblDetailDate);
        
        txtDetailView = new JTextArea("Select a row to view full description...");
        txtDetailView.setEditable(false);
        txtDetailView.setBackground(new Color(245, 245, 245));

        detailsPanel.add(infoGrid, BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(txtDetailView), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r != -1) {
                lblDetailID.setText("Ticket ID: " + table.getValueAt(r, 0));
                lblDetailStatus.setText("Status: " + table.getValueAt(r, 4));
                lblDetailDate.setText("Created: " + table.getValueAt(r, 5));
                txtDetailView.setText("DESCRIPTION:\n" + table.getValueAt(r, 7).toString());
            }
        });

        return detailsPanel;
    }

   private void handleSubmit() {
    // Corrected variable name from comboIssueType to cbIssueType
    String type = (String) cbIssueType.getSelectedItem(); 
    String desc = txtDescription.getText().trim();

    if (desc.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please describe the issue.");
        return;
    }

    // Call Service Layer (N-Tier)
    boolean success = itService.submitNewTicket(
        currentUser.getEmpNo(),
        currentUser.getFirstName(), 
        currentUser.getFirstName() + " " + currentUser.getLastName(),
        type,
        desc
    );

    if (success) {
        JOptionPane.showMessageDialog(this, "Ticket Submitted Successfully!");
        txtDescription.setText("");
        refreshUI(); 
    } else {
        JOptionPane.showMessageDialog(this, "Error: Could not save ticket.");
    }
}

 private JPanel createTopKPIDashboard() {
    JPanel header = new JPanel(new GridLayout(1, 3, 20, 0));
    header.setBackground(new Color(235, 235, 235));
    header.setPreferredSize(new Dimension(0, 50));
    
    // Assign the label to the class variable instead of declaring it locally
    lblActiveTicketsCount = new JLabel("ACTIVE TICKETS: 0", SwingConstants.CENTER);
    
    header.add(lblActiveTicketsCount);
    header.add(new JLabel("USER: " + currentUser.getFirstName() + " " + currentUser.getLastName(), SwingConstants.CENTER));
    header.add(new JLabel("MODULE: IT Support", SwingConstants.CENTER));
    
    return header;
}

  // Inside ITSupportPanel.java

public void refreshUI() {
    model.setRowCount(0);
    
    // SECURITY FIX: Only fetch tickets belonging to the current user
    List<ITTicket> myTickets = itService.getTicketsByEmployee(currentUser.getEmpNo());
    
    for (ITTicket t : myTickets) {
        model.addRow(new Object[]{
            t.getTicketId(), 
            t.getEmployeeNo(), 
            t.getUsername(), 
            t.getIssueType(), 
            t.getStatus(), 
            t.getCreatedAt(), 
            t.getResolvedBy(), 
            t.getDescription()
        });
    }
    
    // Update the KPI Header to show ONLY the user's active ticket count
    updateKPIHeader(myTickets.size());
}

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                if (v != null) {
                    comp.setForeground("OPEN".equalsIgnoreCase(v.toString()) ? Color.BLUE : new Color(0, 128, 0));
                }
                return comp;
            }
        });
    }
private void updateKPIHeader(int count) {
    if (lblActiveTicketsCount != null) {
        lblActiveTicketsCount.setText("ACTIVE TICKETS: " + count);
    }
}
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}