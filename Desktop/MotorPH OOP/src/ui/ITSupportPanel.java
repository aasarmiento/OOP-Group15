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

public class ITSupportPanel extends JPanel {
    private final ITSupportService itService;
    private final Employee currentUser;
    
    private DefaultTableModel model;
    private JTable table;
    private JLabel lblActiveTicketsCount; 

    private final Color primaryMaroon = new Color(128, 0, 0);
    private final Color bgColor = new Color(245, 245, 245);
    private final Color tileBg = new Color(248, 248, 248);
    private final Font titleFont = new Font("DM Sans Bold", Font.BOLD, 18);
    private final Font bodyFont = new Font("DM Sans Regular", Font.PLAIN, 13);
    private final Font cardTitleFont = new Font("DM Sans Bold", Font.BOLD, 12);
    
    private JComboBox<String> cbIssueType;
    private JTextArea txtDescription, txtDetailView;
    private JLabel lblDetailID, lblDetailStatus, lblDetailDate;
    
    private final String[] cols = {"ID", "Emp No", "Username", "Type", "Status", "Created", "Resolved By", "Description"};

    public ITSupportPanel(ITSupportService itService, Employee user) {
        this.itService = itService;
        this.currentUser = user;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(bgColor);
        setBorder(new EmptyBorder(25, 40, 25, 40));
        
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        styleTable();
        
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        
        refreshUI();
    }

    private JPanel createMainContentArea() {
        JPanel centerWrapper = new JPanel(new BorderLayout(25, 0));
        centerWrapper.setOpaque(false);

        centerWrapper.add(createTicketForm(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);
        
        JPanel tableCard = createStyledTile("Your Ticket History");
        tableCard.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);
        
        rightPanel.add(tableCard, BorderLayout.CENTER);
        rightPanel.add(createDetailsPanel(), BorderLayout.SOUTH);

        centerWrapper.add(rightPanel, BorderLayout.CENTER);
        return centerWrapper;
    }

    private JPanel createTicketForm() {
        JPanel formCard = createStyledTile("Submit New Ticket");
        formCard.setPreferredSize(new Dimension(300, 0));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        cbIssueType = new JComboBox<>(new String[]{"Forgot Password", "Account Locked", "Technical Support", "UI Error"});
        cbIssueType.setFont(bodyFont);
        
        txtDescription = new JTextArea(6, 20);
        txtDescription.setFont(bodyFont);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JButton btnSubmit = new StyledButton("SUBMIT TICKET", primaryMaroon);

        int row = 0;
        gbc.gridy = row++; formCard.add(new JLabel("Issue Category"), gbc);
        gbc.gridy = row++; formCard.add(cbIssueType, gbc);
        gbc.gridy = row++; formCard.add(new JLabel("Detailed Description"), gbc);
        gbc.gridy = row++; formCard.add(new JScrollPane(txtDescription), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(25, 10, 10, 10);
        formCard.add(btnSubmit, gbc);

        btnSubmit.addActionListener(e -> handleSubmit());

        return formCard;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsCard = createStyledTile("Ticket Information");
        detailsCard.setPreferredSize(new Dimension(0, 200));
        detailsCard.setLayout(new BorderLayout(15, 10));

        JPanel infoGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        infoGrid.setOpaque(false);
        lblDetailID = new JLabel("Ticket: --");
        lblDetailStatus = new JLabel("Status: --");
        lblDetailDate = new JLabel("Created: --");
        
        for(JLabel lbl : new JLabel[]{lblDetailID, lblDetailStatus, lblDetailDate}) {
            lbl.setFont(cardTitleFont);
            infoGrid.add(lbl);
        }
        
        txtDetailView = new JTextArea("Select a record to view details...");
        txtDetailView.setEditable(false);
        txtDetailView.setFont(bodyFont);
        txtDetailView.setLineWrap(true);
        txtDetailView.setBackground(new Color(242, 242, 242));
        txtDetailView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailsCard.add(infoGrid, BorderLayout.NORTH);
        detailsCard.add(new JScrollPane(txtDetailView), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r != -1) {
                lblDetailID.setText("ID: " + table.getValueAt(r, 0));
                lblDetailStatus.setText("Status: " + table.getValueAt(r, 4));
                lblDetailDate.setText("Created: " + table.getValueAt(r, 5));
                txtDetailView.setText("DESCRIPTION:\n" + table.getValueAt(r, 7).toString());
            }
        });

        return detailsCard;
    }

    private JPanel createTopKPIDashboard() {
        JPanel header = new JPanel(new GridLayout(1, 3, 20, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 80));
        
        lblActiveTicketsCount = new JLabel("0", SwingConstants.CENTER); // Will be updated in KPICard
        
        header.add(createKPICard("Your Tickets", "0", primaryMaroon)); // Updated via refreshUI
        header.add(createKPICard("Logged User", currentUser.getFirstName(), Color.DARK_GRAY));
        header.add(createKPICard("Support Module", "IT Helpdesk", new Color(45, 45, 45)));
        
        return header;
    }


    private JPanel createKPICard(String title, String value, Color themeColor) {
        JPanel card = createStyledTile("");
        card.setLayout(new GridLayout(2, 1));
        JLabel lblT = new JLabel(title); lblT.setFont(cardTitleFont); lblT.setForeground(Color.GRAY);
        JLabel lblV = new JLabel(value); lblV.setFont(new Font("DM Sans Bold", Font.BOLD, 22)); lblV.setForeground(themeColor);
        card.add(lblT); card.add(lblV);
        
        if(title.equals("Your Tickets")) lblActiveTicketsCount = lblV;
        
        return card;
    }

    private JPanel createStyledTile(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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
        table.setSelectionForeground(Color.WHITE); 
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 235, 235));
        setupStatusRenderer();
    }

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isS) {
                    if (v != null) {
                        l.setForeground("OPEN".equalsIgnoreCase(v.toString()) ? Color.BLUE : new Color(0, 128, 0));
                    }
                } else {
                    l.setForeground(Color.WHITE);
                }
                
                if (v != null) {
                    l.setFont(new Font("DM Sans Bold", Font.BOLD, 12));
                }
                return l;
            }
        });
    }


    public void refreshUI() {
        model.setRowCount(0);
        List<ITTicket> myTickets = itService.getTicketsByEmployee(currentUser.getEmpNo());
        for (ITTicket t : myTickets) {
            model.addRow(new Object[]{
                t.getTicketId(), t.getEmployeeNo(), t.getUsername(), 
                t.getIssueType(), t.getStatus(), t.getCreatedAt(), 
                t.getResolvedBy(), t.getDescription()
            });
        }
        updateKPIHeader(myTickets.size());
    }

    private void updateKPIHeader(int count) {
        if (lblActiveTicketsCount != null) {
            lblActiveTicketsCount.setText(String.valueOf(count));
        }
    }

    private void handleSubmit() {
        String type = (String) cbIssueType.getSelectedItem(); 
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please describe the issue.");
            return;
        }

    boolean success = itService.submitNewTicket(
        currentUser.getEmpNo(),
        currentUser.getUsername(),
        currentUser.getFullName(),
        type,
        desc
    );
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Ticket Submitted!");
            txtDescription.setText("");
            refreshUI(); 
        } else {
            JOptionPane.showMessageDialog(this, "Error: Could not save ticket.");
        }
    }

    // Inner Button Class
    class StyledButton extends JButton {
        private Color bgColor;
        public StyledButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(Color.WHITE); setFont(cardTitleFont);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}