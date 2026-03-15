package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.ITStaff; // For Abstraction check
import model.ITTicket;
import service.ITSupportService;

/**
 * UI LAYER: Handles display and user input.
 * No file loading or core business logic here.
 */
public class ITApprovalPanel extends JPanel {
    private final ITSupportService itService;
    private final Employee currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnResolve;

    // UI Styling Constants
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

        // UI Access Rule: Only IT Staff and Admins can see this panel
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
        
        // --- ADDED SINGLE SELECTION MODE ---
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ------------------------------------

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
        
        footerPanel.add(btnResolve);
        mainCard.add(footerPanel, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);
        refreshUI();
    }

    /**
     * UI Logic: Refreshes table data by calling Service.
     * Implements Abstraction: Disables button if currentUser is not ITStaff.
     */
    public void refreshUI() {
        if (model == null) return;
        model.setRowCount(0);
        
        // UI calling Service Layer
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
        
        // BUSINESS RULE (Partial Abstraction): 
        // Admin can view, but ONLY an ITStaff instance can click Resolve.
        if (!(currentUser instanceof ITStaff)) {
            btnResolve.setEnabled(false);
            btnResolve.setToolTipText("Access Restricted: Only IT Staff can resolve tickets.");
        }
    }

    /**
     * UI Action: Handles the button click and sends request to Service.
     */
    private void handleResolve() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ticket to resolve.");
            return;
        }
        
        String ticketId = table.getValueAt(row, 0).toString();
        String status = table.getValueAt(row, 4).toString();

        if (status.equalsIgnoreCase("RESOLVED")) {
            JOptionPane.showMessageDialog(this, "This ticket is already resolved.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to resolve Ticket #" + ticketId + "?", 
            "Confirm Resolution", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // UI calling Service Layer (Protection Layer)
                itService.resolveTicket(ticketId, currentUser); 
                JOptionPane.showMessageDialog(this, "Ticket " + ticketId + " has been marked as Resolved.");
                refreshUI();
            } catch (SecurityException ex) {
                // Service Layer blocked the action based on business rules
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- UI STYLING METHODS (NO LOGIC) ---

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
}