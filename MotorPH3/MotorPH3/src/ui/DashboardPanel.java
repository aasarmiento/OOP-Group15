package ui;

import dao.CSVHandler;
import dao.EmployeeDAO; 
import java.awt.*;
import javax.swing.*;


public class DashboardPanel extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final String userRole;
    private final String userEmpNo;
    private final String userLoggedIn;
    private final String userFirstname;
    private final String userLastname;
    private String selectedEmpNo;

    
    private final EmployeeDAO dao = new CSVHandler(); 

    
    public EmployeeDashboard homePanel = new EmployeeDashboard();
    public AddEmployeePanel addEmpPanel = new AddEmployeePanel(dao); 
    public FullDetailsPanel fullEmpPanel = new FullDetailsPanel();
    public LeavePanel leaveApp = new LeavePanel();
    public TimePanel timeEmpPanel = new TimePanel();

    public DashboardPanel(String employeenum, String accesslevel, String loginnum, String lastname, String firstname) {
        this.userRole = accesslevel;
        this.userEmpNo = employeenum;
        this.userLoggedIn = loginnum;
        this.userFirstname = firstname;
        this.userLastname = lastname;

        setTitle("MotorPH Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

       
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(128, 0, 0));
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

       
        JLabel welcome = new JLabel("Welcome to MOTORPH");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 14));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel adminTitle = new JLabel("Admin Dashboard");
        adminTitle.setForeground(Color.WHITE);
        adminTitle.setFont(new Font("Arial", Font.BOLD, 22));
        adminTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        JButton btnDatabase = UIUtils.createNavButton("Employee Database", Color.white, Color.white);
        JButton btnFullDetails = UIUtils.createNavButton("View Full Details", Color.white, Color.white);
        JButton btnAdd = UIUtils.createNavButton("Add Employee", Color.white, Color.white);
        JButton btnTime = UIUtils.createNavButton("Time", Color.white, Color.white);
        JButton btnLeave = UIUtils.createNavButton("Leave Application", Color.white, Color.white);
        JButton btnEmpHome = UIUtils.createNavButton("Home", Color.white, Color.white);
        JButton btnLogout = UIUtils.createNavButton("Log out", Color.white, Color.white);

        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(homePanel, "Home");
        cardPanel.add(addEmpPanel, "AddEmployee");
        cardPanel.add(fullEmpPanel, "FullDetails");
        cardPanel.add(leaveApp, "Leave");
        cardPanel.add(timeEmpPanel, "Time");

        
        navPanel.add(Box.createVerticalStrut(30)); 
        navPanel.add(welcome);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(adminTitle);
        navPanel.add(Box.createVerticalStrut(40)); 

        if (userRole.equalsIgnoreCase("Admin") || userRole.equalsIgnoreCase("Chief Executive Officer")) {
            addNavComponent(navPanel, btnDatabase);
            addNavComponent(navPanel, btnFullDetails);
            addNavComponent(navPanel, btnAdd);
            addNavComponent(navPanel, btnTime);
        } else {
            adminTitle.setText("Staff Dashboard");
            addNavComponent(navPanel, btnEmpHome);
            addNavComponent(navPanel, btnTime);
            addNavComponent(navPanel, btnLeave);
            fullEmpPanel.setEmployeeNo(userEmpNo);
        }

        navPanel.add(Box.createVerticalGlue()); 
        addNavComponent(navPanel, btnLogout);
        navPanel.add(Box.createVerticalStrut(20));

        
        btnDatabase.addActionListener(e -> {
            homePanel.reloadCSV();
            switchPanel("Home");
        });

        btnAdd.addActionListener(e -> switchPanel("AddEmployee"));

        btnTime.addActionListener(e -> {
            timeEmpPanel.setLoggedIn(userLoggedIn, userLastname, userFirstname);
            switchPanel("Time");
        });

        btnFullDetails.addActionListener(e -> {
            selectedEmpNo = homePanel.getSelectedEmployeeNo();
            if (selectedEmpNo != null) {
                fullEmpPanel.setEmployeeNo(selectedEmpNo);
                switchPanel("FullDetails");
            } else {
                JOptionPane.showMessageDialog(this, "Please select an employee from the database first.");
            }
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginPanel().setVisible(true);
        });

      
        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        
        switchPanel("Home"); 
        setVisible(true);
    }

   
    private void addNavComponent(JPanel panel, JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        panel.add(button);
        panel.add(Box.createVerticalStrut(15));
    }

    private void switchPanel(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }
}