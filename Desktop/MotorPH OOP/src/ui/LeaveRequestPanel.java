package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.LeaveService;

public class LeaveRequestPanel extends JPanel {
    private final LeaveService leaveService;
    private final Employee currentUser;
    private DefaultTableModel model;
    private JTable table;
    
    private final Color primaryMaroon = new Color(128, 0, 0);
    private final Color bgColor = new Color(245, 245, 245);
    private final Color tileBg = new Color(248, 248, 248);
    private final Font bodyFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font cardTitleFont = new Font("SansSerif", Font.BOLD, 12);

    private JTextArea txtDetailReason;
    private JLabel lblDetailID, lblDetailType, lblDetailStatus;
    
    private final String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public LeaveRequestPanel(LeaveService leaveService, Employee user) {
        this.leaveService = leaveService;
        this.currentUser = user;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(bgColor);
        setBorder(new EmptyBorder(20, 40, 20, 40));
        
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable();
        
        add(createTopKPIDashboard(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        
        refreshUI();
    }

    private JPanel createMainContentArea() {
        JPanel container = new JPanel(new BorderLayout(25, 0));
        container.setOpaque(false);
        container.add(createLeaveApplicationForm(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);
        
        JPanel tableCard = createStyledTile("Leave History");
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

   private JPanel createLeaveApplicationForm() {
    JPanel formCard = createStyledTile("Apply for Leave");
    formCard.setPreferredSize(new Dimension(350, 0)); 
    formCard.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    gbc.insets = new Insets(5, 12, 5, 12);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    gbc.gridx = 0;
    int row = 0;

    String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
    JComboBox<String> comboType = new JComboBox<>(leaveTypes);
    comboType.setFont(bodyFont);

    JTextField txtStart = createStyledTextField();
    JTextField txtEnd = createStyledTextField();
    
    JTextArea txtReason = new JTextArea(6, 20); 
    txtReason.setFont(bodyFont);
    txtReason.setLineWrap(true);
    txtReason.setWrapStyleWord(true);
    JScrollPane reasonScroll = new JScrollPane(txtReason);
    reasonScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

    JButton btnPickStart = new StyledButton("📅 Select Start", Color.DARK_GRAY);
    JButton btnPickEnd = new StyledButton("📅 Select End", Color.DARK_GRAY);
    JButton btnSubmit = new StyledButton("Submit Request", primaryMaroon);

    gbc.gridy = row++;
    formCard.add(new JLabel("Type:"), gbc);
    gbc.gridy = row++;
    formCard.add(comboType, gbc);

    gbc.gridy = row++;
    formCard.add(new JLabel("Start Date:"), gbc);
    gbc.gridy = row++;
    formCard.add(txtStart, gbc);
    gbc.gridy = row++;
    formCard.add(btnPickStart, gbc);

    gbc.gridy = row++;
    formCard.add(new JLabel("End Date:"), gbc);
    gbc.gridy = row++;
    formCard.add(txtEnd, gbc);
    gbc.gridy = row++;
    formCard.add(btnPickEnd, gbc);
    
    gbc.gridy = row++;
    gbc.insets = new Insets(15, 12, 5, 12);
    formCard.add(new JLabel("Reason:"), gbc);
    
    gbc.gridy = row++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1.0; 
    gbc.insets = new Insets(0, 12, 10, 12);
    formCard.add(reasonScroll, gbc);
    
    gbc.gridy = row++;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 12, 15, 12);
    formCard.add(btnSubmit, gbc);

    btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
    btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));
    btnSubmit.addActionListener(e -> handleSubmission(comboType, txtStart, txtEnd, txtReason));

    return formCard;
}

    private JPanel createDetailsPanel() {
        JPanel detailsCard = createStyledTile("Request Details");
        detailsCard.setPreferredSize(new Dimension(0, 180));
        detailsCard.setLayout(new BorderLayout(15, 10));

        JPanel infoHeader = new JPanel(new GridLayout(1, 3, 10, 0));
        infoHeader.setOpaque(false);
        lblDetailID = new JLabel("ID: --");
        lblDetailType = new JLabel("Type: --");
        lblDetailStatus = new JLabel("Status: --");
        
        for(JLabel lbl : new JLabel[]{lblDetailID, lblDetailType, lblDetailStatus}) {
            lbl.setFont(cardTitleFont);
            infoHeader.add(lbl);
        }

        txtDetailReason = new JTextArea("Select a record to view details...");
        txtDetailReason.setEditable(false);
        txtDetailReason.setFont(bodyFont);
        txtDetailReason.setBackground(new Color(240, 240, 240));
        txtDetailReason.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnPrint = new StyledButton("Print Leave Form (PDF)", new Color(45, 45, 45));
        btnPrint.addActionListener(e -> handlePrint());

        detailsCard.add(infoHeader, BorderLayout.NORTH);
        detailsCard.add(new JScrollPane(txtDetailReason), BorderLayout.CENTER);
        detailsCard.add(btnPrint, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                lblDetailID.setText("ID: " + table.getValueAt(r, 0));
                lblDetailType.setText("Type: " + table.getValueAt(r, 4));
                lblDetailStatus.setText("Status: " + table.getValueAt(r, 8));
                txtDetailReason.setText(table.getValueAt(r, 7).toString());
            }
        });

        return detailsCard;
    }

private JPanel createTopKPIDashboard() {
        JPanel header = new JPanel(new GridLayout(1, 2, 20, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 90)); 
        
        Color kpiPink = new Color(255, 173, 173); 

        header.add(createPinkKPICard("Available Leave Balance", "15 Days", kpiPink));
        header.add(createPinkKPICard("Upcoming Approved Leave", "Jan 01, 2026", kpiPink));
        
        return header;
    }

    private JPanel createPinkKPICard(String title, String value, Color bgColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(2, 1, 0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblT = new JLabel(title); 
        lblT.setFont(cardTitleFont); 
        lblT.setForeground(new Color(45, 45, 45)); // Dark Gray
        
        JLabel lblV = new JLabel(value); 
        lblV.setFont(new Font("SansSerif", Font.BOLD, 22)); 
        lblV.setForeground(new Color(60, 0, 0)); // Deep Maroon text
        
        card.add(lblT); 
        card.add(lblV);
        return card;
    }

   

    private JPanel createStyledTile(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tileBg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
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

    private void addFormField(JPanel p, String label, Component field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        JLabel l = new JLabel(label); l.setFont(cardTitleFont);
        p.add(l, gbc);
        gbc.gridy = ++row;
        p.add(field, gbc);
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(bodyFont);
        f.setEditable(false);
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return f;
    }

    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(bodyFont);
        table.getTableHeader().setFont(cardTitleFont);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(primaryMaroon);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        setupStatusRenderer();
    }

    private void setupStatusRenderer() {
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                String s = (v != null) ? v.toString() : "";
                if(s.equalsIgnoreCase("APPROVED")) comp.setForeground(new Color(0, 128, 0));
                else if(s.equalsIgnoreCase("PENDING")) comp.setForeground(primaryMaroon);
                else comp.setForeground(Color.GRAY);
                return comp;
            }
        });
    }

    class StyledButton extends JButton {
        private Color bgColor;
        public StyledButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
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
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    private void handleSubmission(JComboBox combo, JTextField start, JTextField end, JTextArea reason) {
        String type = (String) combo.getSelectedItem();
        if (start.getText().isEmpty() || end.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select dates.");
            return;
        }
        try {
            leaveService.submitLeave(currentUser.getEmpNo(), type, 
                LocalDate.parse(start.getText(), formatter), 
                LocalDate.parse(end.getText(), formatter), reason.getText());
            refreshUI();
            JOptionPane.showMessageDialog(this, "Leave request submitted successfully!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void handlePrint() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a record first."); return; }
        try {
            String[] data = new String[cols.length];
            for(int i=0; i<cols.length; i++) data[i] = table.getValueAt(row, i).toString();
            new service.ReportService().generateLeaveReport(currentUser, data);
            JOptionPane.showMessageDialog(this, "PDF Generated!");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage()); }
    }

    public void refreshUI() {
        if (leaveService == null || currentUser == null) return;
        model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
        setupStatusRenderer();
    }
}