package ui;

import service.PangVerify;
import model.Employee;
import javax.swing.JOptionPane;

public class AppController {

    private final PangVerify verifier = new PangVerify();

    public void startSystem(String user, String pass) {
        
        if (verifier.login(user, pass)) {
            
            Employee current = PangVerify.getCurrentUser();
            
            
            if (PangVerify.isHR()) {
                
                AdminDashboard adminUI = new AdminDashboard(current);
                adminUI.setVisible(true);
            } else {
               
                EmployeeDashboard staffUI = new EmployeeDashboard(current);
                staffUI.setVisible(true);
            }
            
            
        } else {
            
            JOptionPane.showMessageDialog(null, 
                "Access Denied: Invalid Credentials.", 
                "Security Alert", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}