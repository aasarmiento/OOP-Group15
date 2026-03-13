package util;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import model.Employee;
import service.EmployeeManagementService;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private JButton btnEdit, btnSave;
    private final EmployeeManagementService service;
    private final Employee currentUser; // This is the employee whose details are being viewed
    private JLabel lblProfilePic;

    // Branding Colors
    private final Color primaryMaroon = new Color(128, 0, 0);
    private final Color bgColor = new Color(245, 245, 245);
    private final Color darkGray = new Color(45, 45, 45);
    private final Font labelFont = new Font("DM Sans Bold", Font.BOLD, 12);
    private final Font fieldFont = new Font("DM Sans Regular", Font.PLAIN, 13);

    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Gender", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    public EmployeeDetailForm(Object[] rawRowData, EmployeeManagementService service, Employee employeeViewed) {  
        this.service = service;
        this.currentUser = employeeViewed;

        setTitle("MotorPH - Employee Full Details");
        setSize(550, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgColor);

        String[] displayData = service.getFormattedDataForForm(rawRowData);

        // --- TOP PANEL: Profile Picture ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        topPanel.setOpaque(false);

        lblProfilePic = new RoundedImageLabel();
        lblProfilePic.setPreferredSize(new Dimension(120, 120));
        lblProfilePic.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblProfilePic.setToolTipText("Click to change photo");
        
        // Add photo upload listener
        lblProfilePic.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    service.updateEmployeePhoto(currentUser, fileChooser.getSelectedFile());
                    displayEmployeePhoto(lblProfilePic); 
                }
            }
        });
        
        displayEmployeePhoto(lblProfilePic);
        topPanel.add(lblProfilePic);

        // --- CENTER PANEL: Form Fields ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);
        fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setFont(labelFont);
            lbl.setForeground(darkGray);
            mainPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;
            String value = (i < displayData.length && displayData[i] != null) ? displayData[i] : "";
            fields[i] = new JTextField(value);
            fields[i].setFont(fieldFont);
            fields[i].setEditable(false);
            fields[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            applyFilters(i, fields[i]);
            
            // Read-only logic for ID and Financials
            if (i == 0 || i >= 18) {
                fields[i].setBackground(new Color(240, 240, 240));
            } else {
                fields[i].setBackground(Color.WHITE);
            }
            mainPanel.add(fields[i], gbc);
        }

        // Wrap panels
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(topPanel, BorderLayout.NORTH);
        wrapper.add(mainPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        
        // --- FOOTER: Actions ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setBackground(bgColor);
        addActionButtons(footer);
        add(footer, BorderLayout.SOUTH);
        
        setVisible(true);
    }

    private void displayEmployeePhoto(JLabel lblPhoto) {
        int empId = currentUser.getEmpNo();
        File imgFile = new File("resources/profile_pics/" + empId + ".png");
        if (!imgFile.exists()) imgFile = new File("resources/profile_pics/" + empId + ".jpg");
        if (!imgFile.exists()) imgFile = new File("resources/profile_pics/default.png");
        
        try {
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                lblPhoto.setIcon(new ImageIcon(img));
                lblPhoto.setText(""); 
            } else { 
                lblPhoto.setIcon(null); 
                lblPhoto.setText("No Image"); 
            }
        } catch (Exception e) { 
            lblPhoto.setText("Error"); 
        }
    }

    private void applyFilters(int index, JTextField field) {
        AbstractDocument doc = (AbstractDocument) field.getDocument();
        switch (index) {
            case 6: doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###")); break;
            case 7: doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#")); break;
            case 10: doc.setDocumentFilter(new NumericLimitFilter(12)); break;
            case 14: case 15: case 16: case 17: doc.setDocumentFilter(new NumericLimitFilter(10)); break;
        }
    }

    private void addActionButtons(JPanel buttonPanel) {
        btnEdit = new StyledButton("Edit Employee", darkGray);
        btnSave = new StyledButton("Save Changes", primaryMaroon);
        btnSave.setVisible(false);

        btnEdit.addActionListener(e -> {
            setFieldsEditable(true);
            btnEdit.setVisible(false);
            btnSave.setVisible(true);
        });

        btnSave.addActionListener(e -> {
            if (service.updateEmployeeFromForm(currentUser, fields)) {
                JOptionPane.showMessageDialog(this, "Employee Information Changes Saved");
                setFieldsEditable(false);
                btnSave.setVisible(false);
                btnEdit.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Update Failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
    }

    private void setFieldsEditable(boolean active) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0 && i < 18) { 
                fields[i].setEditable(active);
                fields[i].setBackground(active ? Color.WHITE : new Color(245, 245, 245));
                if (active) fields[i].setBorder(BorderFactory.createLineBorder(primaryMaroon));
                else fields[i].setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            }
        }
    }

    class RoundedImageLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            Shape circle = new java.awt.geom.Ellipse2D.Double(0, 0, size, size);
            g2.setClip(circle);
            super.paintComponent(g2);
            g2.setClip(null);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(1, 1, size-2, size-2);
            g2.dispose();
        }
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
            setFont(labelFont);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    class NumericLimitFilter extends javax.swing.text.DocumentFilter {
        private final int limit;
        public NumericLimitFilter(int limit) { this.limit = limit; }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
                throws javax.swing.text.BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);
            if (next.matches("\\d*") && next.length() <= limit) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }


   public ImageIcon getEmployeePhoto(int empId, int width, int height) {
    java.io.File photoFile = employeeDao.getEmployeePhotoFile(empId);
    
    if (photoFile != null && photoFile.exists()) {
        ImageIcon icon = new ImageIcon(photoFile.getAbsolutePath());
        
        java.awt.Image img = icon.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        
        return new ImageIcon(img);
    }
    return null; 
}
}