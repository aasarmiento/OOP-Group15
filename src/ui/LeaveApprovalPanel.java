package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.LeaveService;

import model.Employee;        // <--- Add this
import service.LeaveService;  // <--- Add this

public class LeaveApprovalPanel extends JPanel {
    private final LeaveService leaveService;
    private final Employee currentUser;
    private DefaultTableModel model;
    private JTable table;
    
    // Components for the Details/Action area
    private JTextArea txtDetailReason;
    private JLabel lblDetailID, lblDetailEmployee, lblDetailType;
    
    private final String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};

    public LeaveApprovalPanel(LeaveService leaveService, Employee user) {
        this.leaveService = leaveService;
        this.currentUser = user;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        
        // Initialize Table Model (Non-editable)
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        // Build the Layout following LeaveRequestPanel structure
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        
        refreshUI();
    }

    private JPanel createMainContentArea() {
        JPanel centerWrapper = new JPanel(new BorderLayout(20, 20));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // LEFT: Approval Action Form
        centerWrapper.add(createApprovalActionForm(), BorderLayout.WEST);

        // RIGHT: Table area
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(new Color(128, 0, 0)); // Corporate Red
        table.setRowHeight(30);
        setupStatusRenderer();
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        
        rightPanel.add(tableContainer, BorderLayout.CENTER);
        rightPanel.add(createDetailsPanel(), BorderLayout.SOUTH);

        centerWrapper.add(rightPanel, BorderLayout.CENTER);
        return centerWrapper;
    }

    private JPanel createApprovalActionForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Admin Decision"));
        form.setPreferredSize(new Dimension(250, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnApprove = new JButton("APPROVE REQUEST");
        styleButton(btnApprove, new Color(34, 139, 34)); // Forest Green
        
        JButton btnDecline = new JButton("REJECT REQUEST");
        styleButton(btnDecline, new Color(153, 0, 0)); // Dark Red

        gbc.gridy = 0; form.add(new JLabel("Select a request from table"), gbc);
        gbc.gridy = 1; form.add(new JLabel("to perform actions."), gbc);
        gbc.gridy = 2; gbc.insets = new Insets(30, 10, 5, 10);
        form.add(btnApprove, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(5, 10, 5, 10);
        form.add(btnDecline, gbc);

        // Action Logic
        btnApprove.addActionListener(e -> handleAction("Approved"));
        btnDecline.addActionListener(e -> handleAction("Rejected"));

        return form;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Request Details"));
        detailsPanel.setPreferredSize(new Dimension(0, 150));

        JPanel infoGrid = new JPanel(new GridLayout(1, 3));
        lblDetailID = new JLabel("Leave ID: --");
        lblDetailEmployee = new JLabel("Employee: --");
        lblDetailType = new JLabel("Type: --");
        infoGrid.add(lblDetailID); infoGrid.add(lblDetailEmployee); infoGrid.add(lblDetailType);
        
        txtDetailReason = new JTextArea("Click a row to view employee's reason...");
        txtDetailReason.setEditable(false);
        txtDetailReason.setBackground(new Color(245, 245, 245));

        detailsPanel.add(infoGrid, BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(txtDetailReason), BorderLayout.CENTER);

        // Selection Listener to update details
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                lblDetailID.setText("Leave ID: " + table.getValueAt(r, 0));
                lblDetailEmployee.setText("Name: " + table.getValueAt(r, 2) + ", " + table.getValueAt(r, 3));
                lblDetailType.setText("Type: " + table.getValueAt(r, 4));
                txtDetailReason.setText(table.getValueAt(r, 7).toString());
            }
        });

        return detailsPanel;
    }

    private void handleAction(String status) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a leave request first!");
            return;
        }
        
        String leaveId = table.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Set request " + leaveId + " to " + status + "?");
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Assuming your LeaveService has updateLeaveStatus(String id, String status)
            leaveService.updateLeaveStatus(leaveId, status); 
            refreshUI();
            JOptionPane.showMessageDialog(this, "Request " + status);
        }
    }

    private JPanel createTopKPIDashboard() {
        JPanel header = new JPanel(new GridLayout(1, 2, 20, 0));
        header.setBackground(new Color(230, 230, 230));
        header.setPreferredSize(new Dimension(0, 60));
        header.add(new JLabel("PENDING REQUESTS: " + model.getRowCount(), SwingConstants.CENTER));
        header.add(new JLabel("ADMIN: " + currentUser.getFirstName() + " " + currentUser.getLastName(), SwingConstants.CENTER));
        return header;
    }

    public void refreshUI() {
        // Admin view: fetch ALL leaves, or just PENDING leaves depending on your service
        model.setDataVector(leaveService.getAllLeaveRequests(), cols);
        setupStatusRenderer();
    }

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String s = (v != null) ? v.toString() : "";
                if (s.equalsIgnoreCase("APPROVED")) comp.setForeground(new Color(0, 128, 0));
                else if (s.equalsIgnoreCase("REJECTED")) comp.setForeground(Color.RED);
                else comp.setForeground(Color.BLUE);
                return comp;
            }
        });
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