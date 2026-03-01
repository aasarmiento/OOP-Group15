package ui;

import dao.EmployeeDAO;
import javax.swing.*;
import model.Employee;
import model.Role;

public class MainDashboard extends BaseDashboard {

    
    private final EmployeeDashboard homePanel;

    public MainDashboard(EmployeeDAO dao, Employee user) {
        super(dao, user);
        
      
        this.homePanel = new EmployeeDashboard();
        
      
        cardPanel.add(homePanel, "Home");
        
       
        switchPanel(homePanel);
    }

    @Override
    protected void addRoleSpecificComponents() {
        if (user.getRole() == Role.ADMIN) {
          
            addNavButton(btnDatabase, e -> {
                homePanel.reloadCSV();
                switchPanel(homePanel);
            });
            
            
            addNavButton(btnAddEmployee, e -> {
                
                JOptionPane.showMessageDialog(this, "ginagawa pa lang ito");
            });

        } else {
           
            addNavButton(btnProfile, e -> {
                JOptionPane.showMessageDialog(this, "Welcome " + user.getFirstName());
            });
        }
    }

    private void addNavButton(JButton btn, java.awt.event.ActionListener listener) {
        if (btn == null) return;
        
        for (java.awt.event.ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
        btn.addActionListener(listener);
        navPanel.add(btn);
        navPanel.add(Box.createVerticalStrut(10));
    }
}