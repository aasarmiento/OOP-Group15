package ui;

import dao.EmployeeDAO;
import model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ITDashboard extends BaseDashboard {
    
    private JTable userTable;
    private DefaultTableModel tableModel;

    /**
     * CONSTRUCTOR: Inherits from BaseDashboard to get the standard layout
     */
    public ITDashboard(EmployeeDAO dao, Employee user) {
        super(dao, user); // Initializes this.dao and this.user from BaseDashboard
        
        // Setup the IT-specific central panel
        JPanel itContent = createUserManagementPanel();
        
        // Add it to the card layout container
        cardPanel.add(itContent, "IT_Main");
        
        // Immediately show the IT console
        switchPanel(itContent);
        
        // Load the data
        refreshUserList();
    }

    @Override
    protected void addRoleSpecificComponents() {
        // Here we add IT-specific sidebar buttons
        addNavButton(btnDatabase, e -> switchPanel(createUserManagementPanel()));
        
        JButton btnResetPass = new JButton("Reset Password");
        addNavButton(btnResetPass, e -> JOptionPane.showMessageDialog(this, "Password Reset Tool Opening..."));
        
        JButton btnSync = new JButton("Sync System");
        addNavButton(btnSync, e -> refreshUserList());
    }

    private JPanel createUserManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header specific to IT console
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        JLabel lblTitle = new JLabel("  IT SYSTEM CONSOLE | Logged in: " + user.getFirstName());
        lblTitle.setForeground(Color.CYAN);
        header.add(lblTitle, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Emp ID", "Username", "Role", "Account Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(tableModel);
        mainPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        
        return mainPanel;
    }

    private void refreshUserList() {
        // Use 'dao' (inherited from BaseDashboard) to get data
        if (tableModel == null || dao == null) return;
        
        tableModel.setRowCount(0);
        try {
            for (Employee emp : dao.getAll()) {
                tableModel.addRow(new Object[]{
                    emp.getEmpNo(),
                    emp.getFirstName().toLowerCase() + emp.getEmpNo(),
                    emp.getRole(),
                    emp.getStatus()
                });
            }
        } catch (Exception e) {
            System.err.println("IT Console Error: " + e.getMessage());
        }
    }

    // Standard helper to add buttons to sidebar
    private void addNavButton(JButton btn, java.awt.event.ActionListener listener) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 40));
        btn.addActionListener(listener);
        navPanel.add(btn);
        navPanel.add(Box.createVerticalStrut(10));
    }
}