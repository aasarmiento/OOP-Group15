package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.HRSerbisyo;
import service.LeaveService;

public class LeaveApprovalPanel extends JPanel {
    private final LeaveService leaveService;
    private final Employee currentUser;
    private final HRSerbisyo hrSerbisyo;
    
    private DefaultTableModel model;
    private JTable table;
    private JTextArea txtDetailReason;
    private JLabel lblDetailID, lblDetailEmployee, lblDetailType, lblPendingCount;

    private final Color primaryMaroon = new Color(128, 0, 0);
    private final Color bgColor = new Color(245, 245, 245);
    private final Color tileBg = new Color(248, 248, 248);
    private final Font titleFont = new Font("DM Sans Bold", Font.BOLD, 18);
    private final Font bodyFont = new Font("DM Sans Regular", Font.PLAIN, 13);
    private final Font cardTitleFont = new Font("DM Sans Bold", Font.BOLD, 12);
    
    private final String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};

    public LeaveApprovalPanel(LeaveService leaveService, HRSerbisyo hrSerbisyo, Employee user) {
        this.leaveService = leaveService;
        this.hrSerbisyo = hrSerbisyo;
        this.currentUser = user;
        
        setLayout(new BorderLayout());
        setBackground(bgColor);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                checkAccessFirewall();
            }
        });

        checkAccessFirewall();
    }

    private void checkAccessFirewall() {
        removeAll();
        
        String role = currentUser.getRole().name();
        
        if (!role.equalsIgnoreCase("HR_STAFF") && !role.equalsIgnoreCase("ADMIN")) {
            showAccessDenied();
        } else {
            setBorder(new EmptyBorder(25, 40, 25, 40));
            initFullUI();
            refreshUI();
        }
        
        revalidate();
        repaint();
    }

    private void showAccessDenied() {
        JPanel deniedPanel = new JPanel(new GridBagLayout());
        deniedPanel.setBackground(bgColor);
        
        JLabel lblMessage = new JLabel("ACCESS DENIED: HR AUTHORIZATION REQUIRED");
        lblMessage.setFont(new Font("DM Sans Bold", Font.BOLD, 22));
        lblMessage.setForeground(primaryMaroon);
        
        deniedPanel.add(lblMessage);
        
        add(deniedPanel, BorderLayout.CENTER);
    }

    private void initFullUI() {
        setLayout(new BorderLayout(20, 20));
        
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable();
        
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
    }

    private void handleAction(String status) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
            return;
        }

        String leaveId = table.getValueAt(row, 0).toString();
        boolean success = hrSerbisyo.updateLeaveStatus(currentUser, leaveId, status);

        if (success) {
            JOptionPane.showMessageDialog(this, "Leave Request " + status + " successfully.");
            refreshUI();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Unauthorized: Your account does not have permission to modify leave requests.", 
                "Permission Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMainContentArea() {
        JPanel container = new JPanel(new BorderLayout(25, 0));
        container.setOpaque(false);

        container.add(createApprovalActionForm(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);
        
        JPanel tableCard = createStyledTile("Incoming Leave Requests");
        tableCard.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);
        
        rightPanel.add(tableCard, BorderLayout.CENTER);
        rightPanel.add(createDetailsPanel(), BorderLayout.SOUTH);

        container.add(rightPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel createApprovalActionForm() {
        JPanel formCard = createStyledTile("Admin Decision");
        formCard.setPreferredSize(new Dimension(280, 0));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel instruction = new JLabel("<html><body style='width: 180px'>Select a request from the table to approve or reject.</body></html>");
        instruction.setFont(bodyFont);
        instruction.setForeground(Color.GRAY);

        JButton btnApprove = new StyledButton("APPROVE REQUEST", new Color(34, 139, 34)); 
        JButton btnDecline = new StyledButton("REJECT REQUEST", primaryMaroon); 

        gbc.gridy = 0; formCard.add(instruction, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(40, 10, 10, 10);
        formCard.add(btnApprove, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(5, 10, 10, 10);
        formCard.add(btnDecline, gbc);

        btnApprove.addActionListener(e -> handleAction("Approved"));
        btnDecline.addActionListener(e -> handleAction("Rejected"));

        return formCard;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsCard = createStyledTile("Request Preview");
        detailsCard.setPreferredSize(new Dimension(0, 180));
        detailsCard.setLayout(new BorderLayout(15, 10));

        JPanel infoGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        infoGrid.setOpaque(false);
        lblDetailID = new JLabel("ID: --");
        lblDetailEmployee = new JLabel("Employee: --");
        lblDetailType = new JLabel("Type: --");
        
        for(JLabel lbl : new JLabel[]{lblDetailID, lblDetailEmployee, lblDetailType}) {
            lbl.setFont(cardTitleFont);
            infoGrid.add(lbl);
        }
        
        txtDetailReason = new JTextArea("Select a record to view...");
        txtDetailReason.setEditable(false);
        txtDetailReason.setFont(bodyFont);
        txtDetailReason.setLineWrap(true);
        txtDetailReason.setWrapStyleWord(true);
        txtDetailReason.setBackground(new Color(242, 242, 242));
        txtDetailReason.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailsCard.add(infoGrid, BorderLayout.NORTH);
        detailsCard.add(new JScrollPane(txtDetailReason), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                lblDetailID.setText("ID: " + table.getValueAt(r, 0));
                lblDetailEmployee.setText("User: " + table.getValueAt(r, 2) + ", " + table.getValueAt(r, 3));
                lblDetailType.setText("Type: " + table.getValueAt(r, 4));
                txtDetailReason.setText("REASON FOR LEAVE:\n" + table.getValueAt(r, 7).toString());
            }
        });

        return detailsCard;
    }

    private JPanel createTopKPIDashboard() {
        JPanel header = new JPanel(new GridLayout(1, 2, 20, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 80));
        header.add(createKPICard("Pending Approval Requests", "0", primaryMaroon));
        header.add(createKPICard("Admin Access", currentUser.getFirstName() + " " + currentUser.getLastName(), Color.DARK_GRAY));
        return header;
    }

    private JPanel createKPICard(String title, String value, Color themeColor) {
        JPanel card = createStyledTile("");
        card.setLayout(new GridLayout(2, 1));
        JLabel lblT = new JLabel(title); lblT.setFont(cardTitleFont); lblT.setForeground(Color.GRAY);
        JLabel lblV = new JLabel(value); lblV.setFont(new Font("DM Sans Bold", Font.BOLD, 22)); lblV.setForeground(themeColor);
        card.add(lblT); card.add(lblV);
        if(title.contains("Pending")) lblPendingCount = lblV;
        return card;
    }

    private JPanel createStyledTile(String title) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tileBg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        if(!title.isEmpty()) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                panel.getBorder(), 
                BorderFactory.createTitledBorder(null, title, 0, 0, cardTitleFont, primaryMaroon)
            ));
        }
        return panel;
    }

    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(bodyFont);
        table.getTableHeader().setFont(cardTitleFont);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(primaryMaroon);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 235, 235));
        setupStatusRenderer();
    }

    public void refreshUI() {
        Object[][] data = leaveService.getAllLeaveRequests();
        model.setDataVector(data, cols);
        
        int pendingCount = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 8).toString().equalsIgnoreCase("PENDING")) {
                pendingCount++;
            }
        }
        
        if (lblPendingCount != null) lblPendingCount.setText(String.valueOf(pendingCount));
        setupStatusRenderer();
    }

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                String s = (v != null) ? v.toString() : "";
                if (s.equalsIgnoreCase("APPROVED")) comp.setForeground(new Color(0, 128, 0));
                else if (s.equalsIgnoreCase("REJECTED")) comp.setForeground(primaryMaroon);
                else {
                    comp.setForeground(Color.BLUE);
                    comp.setFont(new Font("DM Sans Bold", Font.BOLD, 12));
                }
                return comp;
            }
        });
    }

    class StyledButton extends JButton {
        private Color bgColor;
        public StyledButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(Color.WHITE); setFont(cardTitleFont);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}