package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.ITTicket;
import service.ITSupportService;

public class ITApprovalPanel extends JPanel {
    private final ITSupportService itService;
    private final Employee currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnResolve;

    private final Color primaryMaroon = new Color(128, 0, 0);
    private final Color bgColor = new Color(245, 245, 245);
    private final Color tileBg = new Color(248, 248, 248);
    private final Font titleFont = new Font("DM Sans Bold", Font.BOLD, 20);
    private final Font cardTitleFont = new Font("DM Sans Bold", Font.BOLD, 12);
    private final Font bodyFont = new Font("DM Sans Regular", Font.PLAIN, 13);

    public ITApprovalPanel(ITSupportService service, Employee user) {
        this.itService = service;
        this.currentUser = user;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(bgColor);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        String role = currentUser.getRole().name();
        if (!role.equalsIgnoreCase("IT_STAFF") && !role.equalsIgnoreCase("ADMIN")) {
            showAccessDenied();
            return; 
        }

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("IT Ticket Approval & Management");
        lblTitle.setFont(titleFont);
        lblTitle.setForeground(new Color(45, 45, 45));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        headerPanel.add(createStatusLegend(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainCard = createStyledTile("");
        mainCard.setLayout(new BorderLayout(0, 15));

        String[] columns = {"ID", "Emp No", "User", "Issue Type", "Status", "Created At"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);

        btnResolve = new StyledButton("Mark as Resolved", new Color(0, 102, 51));
        btnResolve.setPreferredSize(new Dimension(180, 40));
        btnResolve.addActionListener(e -> handleResolve());

        JButton btnReopen = new StyledButton("Reopen Ticket", primaryMaroon);
        btnReopen.setPreferredSize(new Dimension(160, 40));
        btnReopen.addActionListener(e -> handleReopen());

        JButton btnDelete = new StyledButton("Delete Ticket", Color.DARK_GRAY);
        btnDelete.setPreferredSize(new Dimension(140, 40));
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnTempPassword = new StyledButton("Generate Temp Password", new Color(0, 102, 102));
        btnTempPassword.setPreferredSize(new Dimension(220, 40));
        btnTempPassword.addActionListener(e -> handleGenerateTempPassword());

        footerPanel.add(btnTempPassword);
        footerPanel.add(btnReopen);
        footerPanel.add(btnDelete);
        footerPanel.add(btnResolve);

        mainCard.add(footerPanel, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);
        refreshUI();
    }

    private void showAccessDenied() {
        JPanel deniedPanel = new JPanel(new GridBagLayout());
        deniedPanel.setBackground(bgColor);
        JLabel lblMessage = new JLabel("ACCESS DENIED: IT AUTHORIZATION REQUIRED");
        lblMessage.setFont(titleFont);
        lblMessage.setForeground(primaryMaroon);
        deniedPanel.add(lblMessage);
        add(deniedPanel, BorderLayout.CENTER);
    }

    private void styleTable() {
        table.setRowHeight(40);
        table.setFont(bodyFont);
        table.getTableHeader().setFont(cardTitleFont);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(primaryMaroon);
        table.setSelectionForeground(Color.WHITE); 
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(v);
                
                if (isS) {
                    l.setForeground(Color.WHITE);
                } else {
                    if (val.equalsIgnoreCase("RESOLVED")) {
                        l.setForeground(new Color(0, 153, 51));
                    } else {
                        l.setForeground(primaryMaroon);
                    }
                }
                l.setFont(new Font("DM Sans Bold", Font.BOLD, 12));
                return l;
            }
        });
    }

    private JPanel createStatusLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        String roleDisplay = currentUser.getRole().name().replace("_", " ");
        JLabel lbl = new JLabel("Role: " + roleDisplay);
        lbl.setFont(bodyFont);
        lbl.setForeground(Color.GRAY);
        p.add(lbl);
        return p;
    }

    private JPanel createStyledTile(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tileBg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(225, 225, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }

    class StyledButton extends JButton {
        private Color color;
        public StyledButton(String text, Color bg) {
            super(text);
            this.color = bg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(cardTitleFont);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setColor(Color.LIGHT_GRAY);
            } else {
                g2.setColor(getModel().isPressed() ? color.darker() : color);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public void refreshUI() {
        if (model == null) return;
        model.setRowCount(0);
        List<ITTicket> tickets = itService.getAllTickets(); 
        for (ITTicket t : tickets) {
            model.addRow(new Object[]{
                t.getTicketId(),
                t.getEmployeeNo(),
                t.getFullName(),
                t.getIssueType(),
                t.getStatus(),
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
        try {
            itService.resolveTicket(ticketId, currentUser); 
            JOptionPane.showMessageDialog(this, "Ticket #" + ticketId + " Resolved.");
            refreshUI();
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReopen() {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Select a ticket to reopen.");
        return;
    }

    int ticketId = (int) table.getValueAt(row, 0);
    try {
        itService.reopenTicket(ticketId, currentUser);
        JOptionPane.showMessageDialog(this, "Ticket #" + ticketId + " reopened.");
        refreshUI();
    } catch (SecurityException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ticket to delete.");
            return;
        }

        int ticketId = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete ticket #" + ticketId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

            try {
                itService.deleteTicket(ticketId, currentUser);
                JOptionPane.showMessageDialog(this, "Ticket #" + ticketId + " deleted.");
                refreshUI();
            } catch (SecurityException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    private void handleGenerateTempPassword() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a forgot-password ticket.");
            return;
        }

        int ticketId = (int) table.getValueAt(row, 0);
        ITTicket ticket = itService.getTicketById(ticketId);

        if (ticket == null) {
            JOptionPane.showMessageDialog(this, "Ticket not found.");
            return;
        }

        if (!"FORGOT_PASSWORD".equalsIgnoreCase(ticket.getIssueType())) {
            JOptionPane.showMessageDialog(this, "This action is only for forgot-password tickets.");
            return;
        }

        try {
            String tempPassword = itService.generateTemporaryPasswordForEmployee(ticket.getEmployeeNo(), currentUser);

            if (tempPassword == null) {
                JOptionPane.showMessageDialog(this, "Unable to generate temporary password.");
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "Temporary password created for Employee #" + ticket.getEmployeeNo()
                + ":\n" + tempPassword
                + "\n\nSend this to the employee through approved channels."
            );

        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}