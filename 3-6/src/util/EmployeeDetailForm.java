package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import model.Employee;
import service.EmployeeManagementService;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private JButton btnEdit, btnSave;
    private final EmployeeManagementService service;
    private final Employee currentUser;


    

    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    public EmployeeDetailForm(Object[] data, EmployeeManagementService service, Employee currentUser) {
        this.service = service;
        this.currentUser = currentUser;

        setTitle("Employee Full Details");
        setSize(550, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());



        

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            String value = (data != null && i < data.length && data[i] != null) ? data[i].toString() : "";
            fields[i] = new JTextField(value);
            fields[i].setEditable(false);

            // APPLY FILTERS (Like your New Hire Form)
            applyFilters(i, fields[i]);

            if (i == 0 || i >= 17) {
                fields[i].setBackground(new Color(230, 230, 230));
            }
            mainPanel.add(fields[i]);
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
        case 4: 
            // Address: No filter needed so letters/numbers can be typed.
            // Just ensure setFieldsEditable(true) includes index 4.
            break;
            
        case 5: // Phone #
            doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###"));
            break;
            
        case 6: // SSS #
            doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#"));
            break;
            
        case 9: // Pag-ibig (12 Digits Only)
            doc.setDocumentFilter(new NumericLimitFilter(12));
            break;
            
        case 13: // Basic Salary
        case 14: // Rice Subsidy
        case 15: // Phone Allowance
        case 16: // Clothing Allowance
            // Numeric only, NO decimals, max 6 digits
            doc.setDocumentFilter(new NumericLimitFilter(6)); 
            break;
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
            // This triggers the Service-Layer validation we built earlier
            if (service.updateEmployeeFromForm(currentUser, fields)) {
                JOptionPane.showMessageDialog(this, "Employee Information Changes Saved");
                setFieldsEditable(false);
                btnSave.setVisible(false);
                btnEdit.setVisible(true);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
    }

    




// Helper class to handle digit-only input and length limits
class NumericLimitFilter extends javax.swing.text.DocumentFilter {
    private final int limit;

    public NumericLimitFilter(int limit) {
        this.limit = limit;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
            throws javax.swing.text.BadLocationException {
        
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String next = current.substring(0, offset) + text + current.substring(offset + length);
        
        // Validation: Must be numeric AND within length limit (no decimals allowed)
        if (next.matches("\\d*") && next.length() <= limit) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}

private void setFieldsEditable(boolean active) {
    for (int i = 0; i < fields.length; i++) {
        // Index 4 is Address. 13-16 are Financials.
        // This range (1 to 16) covers Name, Bday, Address, IDs, and Allowances.
        if (i > 0 && i < 17) { 
            fields[i].setEditable(active);
            // Change background to white when editing to show it is active
            fields[i].setBackground(active ? Color.WHITE : new Color(245, 245, 245));
        }
    }
}


    
}