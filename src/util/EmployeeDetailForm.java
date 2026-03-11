package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import model.Employee;
import service.EmployeeManagementService;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private JComboBox<String> positionDropdown; // UI element for Position selection
    private JComboBox<String> statusDropdown;   // UI element for Status selection
    private JButton btnEdit, btnSave;
    private final EmployeeManagementService service;
    private final Employee currentUser;

    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Gender", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    // Position data
    private final String[] positions = {
        "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer",
        "Chief Marketing Officer", "IT Operations and Systems", "HR Manager",
        "HR Team Leader", "HR Rank and File", "Accounting Head", "Payroll Manager",
        "Payroll Team Leader", "Payroll Rank and File", "Account Manager",
        "Account Team Leader", "Account Rank and File", "Sales & Marketing",
        "Supply Chain and Logistics", "Customer Service and Relations"
    };

    // Status data
    private final String[] statusOptions = {"Regular", "Probationary"};

    public EmployeeDetailForm(Object[] rawRowData, EmployeeManagementService service, Employee currentUser) {  
        this.service = service;
        this.currentUser = currentUser;

        setTitle("Employee Full Details");
        setSize(550, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] displayData = service.getFormattedDataForForm(rawRowData);

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            
            String value = (i < displayData.length && displayData[i] != null) ? displayData[i] : "";
            
            // UI Logic: Handle Status as a Dropdown at index 11
            if (i == 11) {
                statusDropdown = new JComboBox<>(statusOptions);
                statusDropdown.setSelectedItem(value);
                statusDropdown.setEnabled(false);
                statusDropdown.setBackground(new Color(235, 235, 235));
                mainPanel.add(statusDropdown);
                fields[i] = new JTextField(value); // Keep array index consistent
            }
            // UI Logic: Handle Position as a Dropdown at index 12
            else if (i == 12) {
                positionDropdown = new JComboBox<>(positions);
                positionDropdown.setSelectedItem(value);
                positionDropdown.setEnabled(false);
                positionDropdown.setBackground(new Color(235, 235, 235));
                mainPanel.add(positionDropdown);
                fields[i] = new JTextField(value); 
            } else {
                fields[i] = new JTextField(value);
                fields[i].setEditable(false);
                applyFilters(i, fields[i]);
                
                if (isFieldEditable(i)) {
                    fields[i].setBackground(Color.WHITE);
                } else {
                    fields[i].setBackground(new Color(235, 235, 235));
                }
                mainPanel.add(fields[i]);
            }
        }

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addActionButtons(footer);
        add(footer, BorderLayout.SOUTH);
        
        setVisible(true);
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
        btnEdit = new JButton("Edit Employee");
        btnSave = new JButton("Save Changes");
        btnSave.setVisible(false);

        btnEdit.addActionListener(e -> {
            setFieldsEditable(true);
            btnEdit.setVisible(false);
            btnSave.setVisible(true);
        });

        btnSave.addActionListener(e -> {
            // UI Logic: Sync dropdowns to fields array before Service Layer call
            fields[11].setText((String) statusDropdown.getSelectedItem());
            fields[12].setText((String) positionDropdown.getSelectedItem());

            if (service.updateEmployeeFromForm(currentUser, fields)) {
                JOptionPane.showMessageDialog(this, "Employee Information Changes Saved");
                setFieldsEditable(false);
                btnSave.setVisible(false);
                btnEdit.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Update Failed. Check permissions or data format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
    }

    private void setFieldsEditable(boolean active) {
        for (int i = 0; i < fields.length; i++) {
            if (i == 11) { // Status Dropdown
                statusDropdown.setEnabled(active);
                statusDropdown.setBackground(active ? Color.WHITE : new Color(235, 235, 235));
            } else if (i == 12) { // Position Dropdown
                positionDropdown.setEnabled(active);
                positionDropdown.setBackground(active ? Color.WHITE : new Color(235, 235, 235));
            } else if (isFieldEditable(i)) {
                fields[i].setEditable(active);
                fields[i].setBackground(active ? Color.WHITE : new Color(250, 250, 250));
            } else {
                fields[i].setEditable(false);
                fields[i].setBackground(new Color(235, 235, 235)); 
            }
        }
    }

    private boolean isFieldEditable(int index) {
        return switch (index) {
            case 1 -> true;  // Last Name
            case 5 -> true;  // Address
            case 11 -> true; // Status (Dropdown)
            case 12 -> true; // Position (Dropdown)
            case 14 -> true; // Basic Salary
            case 15 -> true; // Rice Subsidy
            case 16 -> true; // Phone Allowance
            case 17 -> true; // Clothing Allowance
            case 18 -> true; // Gross Semi-monthly Rate
            case 20 -> true; // Role
            default -> false; 
        };
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
}