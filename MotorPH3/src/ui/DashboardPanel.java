/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author abigail
 */

import java.awt.*;
import javax.swing.*;
import model.Employee;
import model.Role;



public class DashboardPanel extends JPanel {

//fields 
    private final AppController controller; 
    private final JPanel sidebar = new JPanel();
    private final JPanel content = new JPanel(new CardLayout());

//cardlayouts for the screen router functionality [polymorph], used 'final' para immutable sya or naka lock

//DEFAULT SCREEN
private static final String SCREEN_HOME = "HOME";
private static final String SCREEN_PROFILE = "PROFILE";
private static final String SCREEN_SALARY = "SALARY";
private static final String SCREEN_LEAVE = "LEAVE";

//for admin
private static final String SCREEN_ADMIN_ADD = "ADMIN_ADD"; //used snake case for readability, tells that constant ito
private static final String SCREEN_ADMIN_UPDATE = "ADMIN_UPDATE";
private static final String SCREEN_ADMIN_REMOVE = "ADMIN_REMOVE";
private static final String SCREEN_ADMIN_LEAVE_STATUS = "ADMIN_LEAVE_STATUS";

//for HR
private static final String SCREEN_HR_VIEW_PROFILE = "HR_VIEW_PROFILE";
private static final String SCREEN_HR_UPDATE_DETAILS = "HR_UPDATE_DETAILS";
private static final String SCREEN_HR_VIEW_LEAVES = "HR_VIEW_LEAVES";
private static final String SCREEN_HR_APPROVE_LEAVE = "HR_APPROVE_LEAVE";
private static final String SCREEN_HR_REJECT_LEAVE = "HR_REJECT_LEAVE";

//for IT staff
private static final String SCREEN_IT_RESET = "IT_RESET";
private static final String SCREEN_IT_PW_STRENGTH = "IT_PW_STRENGTH";

//for Accounting
private static final String SCREEN_ACC_BATCH = "ACC_BATCH";
private static final String SCREEN_ACC_PAYSLIP = "ACC_PAYSLIP";
private static final String SCREEN_ACC_TAX = "ACC_TAX";



// the constructor
    public DashboardPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        buildRoleBasedMenu(); //this is where polymorphsm will be initiated
        registerScreens();
        showScreen(SCREEN_HOME); // <- default screen
    
}

private void buildRoleBasedMenu() {
    sidebar.removeAll();

    Employee currentUser = controller.getCurrentUser();

    //null-safety check to avoid program crashing
    if (currentUser == null) {
        addTitle("Welcome to your MotorPH Dashboard!");
        addButton("Logout", SCREEN_HOME);
        return;
    }

    Role role = currentUser.getRole(); //runtime polymorph dito

    addTitle("Welcome To Your MotorPH Payroll Dashboard!");

    addButton("View Personal Info", SCREEN_PROFILE);
    addButton("View Salary", SCREEN_SALARY);
    addButton("Request for Leave", SCREEN_LEAVE);

    sidebar.add(Box.createVerticalStrut(10));
    sidebar.add(new JSeparator());
    sidebar.add(Box.createVerticalStrut(10));

    switch (role) {
        case ADMIN -> {
            addButton("Add Employee", SCREEN_ADMIN_ADD);
            addButton("Update Employee", SCREEN_ADMIN_UPDATE);
            addButton("Remove Employee", SCREEN_ADMIN_REMOVE);
            addButton("Update Leave Status", SCREEN_ADMIN_LEAVE_STATUS);
        }


        case HR_STAFF -> {
            addButton("View Employee Profile", SCREEN_HR_VIEW_PROFILE);
            addButton("Update Employee Details", SCREEN_HR_UPDATE_DETAILS);
            addButton("View All Leave Requests", SCREEN_HR_VIEW_LEAVES);
            addButton("Approve Leave Request", SCREEN_HR_APPROVE_LEAVE);
            addButton("Reject Leave Ruquest", SCREEN_HR_REJECT_LEAVE);

        }

        case ACCOUNTING -> {
            addButton("Batch Process Payroll", SCREEN_ACC_BATCH);
            addButton("Generate Payslip", SCREEN_ACC_PAYSLIP);
            addButton("Generate Tax Report", SCREEN_ACC_TAX);

        }


        case IT_STAFF -> {
            addButton("Reset Password", SCREEN_IT_RESET);
            addButton("Check Password Strength", SCREEN_IT_PW_STRENGTH);
        }

        case REGULAR_STAFF -> {
            // leave natin to as is, default
        }

        
    }

    sidebar.add(Box.createVerticalGlue());

    JButton logoutBtn = new JButton("Logout");
    logoutBtn.addActionListener(e -> { 
        controller.logout();
        
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose(); //iclose nya yung window
        }
        
        JOptionPane.showMessageDialog(this, "You have logged out.");
        });
        
    sidebar.add(logoutBtn);

    };

private void addTitle(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 18));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    sidebar.add(label);
    sidebar.add(Box.createVerticalStrut(15));

}


private void addButton(String text, String screenKey) {
    JButton button = new JButton(text);
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    button.addActionListener(e -> showScreen(screenKey));
    sidebar.add(button);
    sidebar.add(Box.createVerticalStrut(8));

}


private void  registerScreens() {

    //default screen
    content.add(new PlaceholderPanel("Home"), SCREEN_HOME);
    content.add(new PlaceholderPanel("Personal Info"), SCREEN_PROFILE);
    content.add(new PlaceholderPanel("Salary View"), SCREEN_SALARY);
    content.add(new PlaceholderPanel("Request for Leave"), SCREEN_LEAVE);

    //Admin screen
    content.add(new PlaceholderPanel("Admin - Add Employee"), SCREEN_ADMIN_ADD);
    content.add(new PlaceholderPanel("Admin - Update Employee"), SCREEN_ADMIN_UPDATE);
    content.add(new PlaceholderPanel("Admin - Remove Employee"), SCREEN_ADMIN_REMOVE);
    content.add(new PlaceholderPanel("Admin - Update Leave Status"), SCREEN_ADMIN_LEAVE_STATUS);

    //HR screen
    content.add(new PlaceholderPanel("HR - View Employee Profile"), SCREEN_HR_VIEW_PROFILE);
    content.add(new PlaceholderPanel("HR - Update Employee Details"), SCREEN_HR_UPDATE_DETAILS);
    content.add(new PlaceholderPanel("HR - View All Leave Requests"), SCREEN_HR_VIEW_LEAVES);
    content.add(new PlaceholderPanel("HR - Approve Leave Requests"), SCREEN_HR_APPROVE_LEAVE);
    content.add(new PlaceholderPanel("HR - Reject Leave Requests"), SCREEN_HR_REJECT_LEAVE);

    //finance screen
    content.add(new PlaceholderPanel("Accounting - Batch Process Payroll"), SCREEN_ACC_BATCH);
    content.add(new PlaceholderPanel("Accounting - Generate Payslip"), SCREEN_ACC_PAYSLIP);
    content.add(new PlaceholderPanel("Accounting - Generate Tax Report"), SCREEN_ACC_TAX);

    
    //IT screen
    content.add(new PlaceholderPanel("IT - Reset Password"), SCREEN_IT_RESET);
    content.add(new PlaceholderPanel("IT - Password Strength Check"), SCREEN_IT_PW_STRENGTH);



}



private void showScreen(String key) {
    CardLayout cl = (CardLayout) content.getLayout();
    cl.show(content, key);
}

private static class PlaceholderPanel extends JPanel {
    PlaceholderPanel(String text) {
        setLayout(new GridBagLayout());
        add(new JLabel(text));
    }
}



}




