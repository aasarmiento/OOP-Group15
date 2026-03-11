package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.LeaveService;

public class LeaveRequestPanel extends JPanel {
    private final LeaveService leaveService;
    private final Employee currentUser;
    private DefaultTableModel model;
    private JTable table;
    
    // New Components for the Bottom Details
    private JTextArea txtDetailReason;
    private JLabel lblDetailID, lblDetailType, lblDetailStatus;
    
    private final String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public LeaveRequestPanel(LeaveService leaveService, Employee user) {
        this.leaveService = leaveService;
        this.currentUser = user;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        
        refreshUI();
    }

    private JPanel createMainContentArea() {
        JPanel centerWrapper = new JPanel(new BorderLayout(20, 20));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // LEFT: The Form (Existing Logic)
        centerWrapper.add(createLeaveApplicationForm(), BorderLayout.WEST);

        // RIGHT: Table AND Details (Nested BorderLayout)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        
        // Top of Right: Table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(new Color(128, 0, 0)); // Corporate Red
        table.setRowHeight(30);
        setupStatusRenderer();
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Bottom of Right: Details Panel & Print Button
        rightPanel.add(tableContainer, BorderLayout.CENTER);
        rightPanel.add(createDetailsPanel(), BorderLayout.SOUTH);

        centerWrapper.add(rightPanel, BorderLayout.CENTER);
        return centerWrapper;
    }
private JPanel createDetailsPanel() {
    JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
    detailsPanel.setBorder(BorderFactory.createTitledBorder("Leave Details (Click ID to View)"));
    detailsPanel.setPreferredSize(new Dimension(0, 180)); // Slightly taller for the button

    // 1. Info Labels Setup
    JPanel infoGrid = new JPanel(new GridLayout(2, 2));
    lblDetailID = new JLabel("ID: --");
    lblDetailType = new JLabel("Type: --");
    lblDetailStatus = new JLabel("Status: --");
    infoGrid.add(lblDetailID);
    infoGrid.add(lblDetailType);
    infoGrid.add(lblDetailStatus);
    
    // 2. Reason Area Setup
    txtDetailReason = new JTextArea("Click a row to see the full reason...");
    txtDetailReason.setEditable(false);
    txtDetailReason.setLineWrap(true);
    txtDetailReason.setWrapStyleWord(true);
    txtDetailReason.setBackground(new Color(245, 245, 245));

    // 3. Print Button Setup (Defined here so it's "resolved")
    JButton btnPrint = new JButton("Print to PDF");
    styleButton(btnPrint, new Color(50, 50, 50)); // Dark grey/black
    
    // 4. The Print Action Logic
    btnPrint.addActionListener(e -> {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave record from the table first.");
            return;
        }

        // Gather data from the selected row
        String[] leaveData = new String[cols.length];
        for(int i = 0; i < cols.length; i++) {
            leaveData[i] = table.getValueAt(row, i).toString();
        }

        // Call the ReportService (N-Tier Compliance)
        try {
            service.ReportService reportService = new service.ReportService();
            reportService.generateLeaveReport(currentUser, leaveData);
            JOptionPane.showMessageDialog(this, "Employee Record printed successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error printing: " + ex.getMessage());
        }
    });

    // 5. Layout Assembly
    detailsPanel.add(infoGrid, BorderLayout.NORTH);
    detailsPanel.add(new JScrollPane(txtDetailReason), BorderLayout.CENTER);
    detailsPanel.add(btnPrint, BorderLayout.SOUTH);

    // 6. Sync Table Click to Details
    table.getSelectionModel().addListSelectionListener(event -> {
        if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
            int r = table.getSelectedRow();
            lblDetailID.setText("ID: " + table.getValueAt(r, 0).toString());
            lblDetailType.setText("Type: " + table.getValueAt(r, 4).toString());
            lblDetailStatus.setText("Status: " + table.getValueAt(r, 8).toString());
            txtDetailReason.setText(table.getValueAt(r, 7).toString());
        }
    });

    return detailsPanel;
}

    // --- REUSED METHODS FROM PREVIOUS CODE (Application Form, KPI, Style) ---
    
    private JPanel createLeaveApplicationForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Apply Leave Calendar"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
        JComboBox<String> comboType = new JComboBox<>(leaveTypes);
        JTextField txtStart = new JTextField(10);
        JTextField txtEnd = new JTextField(10);
        txtStart.setEditable(false); txtEnd.setEditable(false);
        JTextArea txtReason = new JTextArea(4, 20);
        txtReason.setLineWrap(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnPickStart = new JButton("📅 Start Date");
        JButton btnPickEnd = new JButton("📅 End Date");
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(0, 51, 102));

        gbc.gridy = 0; form.add(new JLabel("Type:"), gbc);
        gbc.gridy = 1; form.add(comboType, gbc);
        gbc.gridy = 2; form.add(new JLabel("Start Date:"), gbc);
        gbc.gridy = 3; form.add(txtStart, gbc);
        gbc.gridy = 4; form.add(btnPickStart, gbc);
        gbc.gridy = 5; form.add(new JLabel("End Date:"), gbc);
        gbc.gridy = 6; form.add(txtEnd, gbc);
        gbc.gridy = 7; form.add(btnPickEnd, gbc);
        gbc.gridy = 8; form.add(new JLabel("Reason:"), gbc);
        gbc.gridy = 9; form.add(new JScrollPane(txtReason), gbc);
        gbc.gridy = 10; form.add(btnSubmit, gbc);

        // Listeners for Dates
        btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
        btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));

        btnSubmit.addActionListener(e -> {
            String type = (String) comboType.getSelectedItem();
            int balance = leaveService.getRemainingBalance(currentUser.getEmpNo(), type);
            if (balance <= 0) { JOptionPane.showMessageDialog(this, "Insufficient balance!"); return; }
            
            try {
                leaveService.submitLeave(currentUser.getEmpNo(), type, 
                    LocalDate.parse(txtStart.getText(), formatter), 
                    LocalDate.parse(txtEnd.getText(), formatter), txtReason.getText());
                refreshUI();
                JOptionPane.showMessageDialog(this, "Submitted!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        return form;
    }

    private JPanel createTopKPIDashboard() {
        JPanel header = new JPanel(new GridLayout(1, 2, 20, 0));
        header.setBackground(new Color(240, 240, 240));
        header.setPreferredSize(new Dimension(0, 80));
        header.add(new JLabel("KPI Available Leave: 15 Days", SwingConstants.CENTER));
        header.add(new JLabel("Upcoming Leave: Jan 01", SwingConstants.CENTER));
        return header;
    }

    public void refreshUI() {
        if (leaveService == null || currentUser == null) return;
        model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
        setupStatusRenderer();
    }

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String s = (v != null) ? v.toString() : "";
                comp.setForeground(s.equalsIgnoreCase("APPROVED") ? new Color(0, 128, 0) : 
                                  s.contains("REJECT") ? Color.RED : Color.BLUE);
                return comp;
            }
        });
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }
}