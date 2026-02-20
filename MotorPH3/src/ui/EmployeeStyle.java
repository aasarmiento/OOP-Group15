package ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dao.CSVHandler;

import model.RegularStaff;
import dao.CSVHandler; // This points to your DAO package
    
import model.Employee;
import model.Admin;
import model.RegularStaff;


public class EmployeeStyle extends JFrame {

    // 1. DATA LAYER
    private CSVHandler csvHandler = new CSVHandler();
    
    // 2. UI COMPONENTS (Created using your UIUtils)
    private JTextField txtEmpNo = UIUtils.createTextField(false); // Read-only
    private JTextField txtLastName = UIUtils.createTextField(true);
    private JTextField txtFirstName = UIUtils.createTextField(true);
    // ... add others for personal/financial info
    
    public EmployeeManagementFrame() {
        setTitle("MotorPH Employee Management System");
        setLayout(new BorderLayout());
        
        // 3. BUILDING THE PANELS
        JPanel infoContainer = new JPanel(new GridLayout(1, 3, 10, 10));
        
        // Using your UIUtils to build the groups
        infoContainer.add(UIUtils.createEmployeeInfoPanel(txtEmpNo, txtLastName, txtFirstName, ...));
        infoContainer.add(UIUtils.createPersonalInfoPanel(...));
        infoContainer.add(UIUtils.createFinancialInfoPanel(...));
        
        add(infoContainer, BorderLayout.CENTER);
        
        // 4. ADD THE SAVE BUTTON
        JButton btnSave = UIUtils.createButton("Save Changes", Color.BLUE, Color.WHITE);
        btnSave.addActionListener(e -> handleSave());
        add(btnSave, BorderLayout.SOUTH);
    }

    private void handleSave() {
        // This connects your UI back to the Model and DAO
        try {
            int id = Integer.parseInt(txtEmpNo.getText());
            // Create the Employee object based on UI input
            // (Assuming RegularStaff for this example)
            Employee emp = new RegularStaff(id, txtLastName.getText(), txtFirstName.getText(), ...);
            
            csvHandler.add(emp); // This triggers your CSV writing logic!
            JOptionPane.showMessageDialog(this, "Employee Saved Successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }
}
}
