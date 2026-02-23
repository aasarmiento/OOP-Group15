package ui;

import dao.EmployeeDAO;
import java.awt.*;
import javax.swing.*;
import model.Employee;

public abstract class BaseDashboard extends JFrame {
    protected CardLayout cardLayout = new CardLayout();
    protected JPanel cardPanel = new JPanel(cardLayout);
    protected JPanel navPanel = new JPanel();
    
    protected final EmployeeDAO dao;
    protected final Employee user;
    protected JButton btnDatabase, btnAddEmployee, btnAttendance, btnProfile, btnLeave, btnFullDetails;

    public BaseDashboard(EmployeeDAO dao, Employee user) {
        this.dao = dao;
        this.user = user;
        
        setTitle("MotorPH Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initializeNavButtons();
        setupNavigationPanel();
        
        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        
    }

    private void initializeNavButtons() {
    // 1. INITIALIZE EVERYTHING FIRST
    btnDatabase = new JButton("Employee Database");
    btnFullDetails = new JButton("View Full Details"); // <--- Ensure this line exists!
    btnAddEmployee = new JButton("Add Employee");
    btnAttendance = new JButton("Time");
    btnProfile = new JButton("My Profile");
    btnLeave = new JButton("Leave Application");
    
    // 2. NOW PUT THEM IN THE ARRAY
    JButton[] allButtons = {
        btnDatabase, 
        btnFullDetails, // If this was null, the loop below crashed
        btnAddEmployee, 
        btnAttendance, 
        btnProfile, 
        btnLeave
    };
    
    // 3. APPLY STYLES (This is where your crash happened)
    for (JButton btn : allButtons) {
        if (btn != null) { // Extra safety check
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(190, 40));
            btn.setFocusable(false);
        }
    }
}

    private void setupNavigationPanel() {
        navPanel.setBackground(new Color(128, 0, 0)); // Dark Red
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        
        navPanel.add(Box.createVerticalStrut(30));
        
       
        JLabel welcomeLabel = new JLabel("Welcome to MOTORPH,");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(welcomeLabel);

        JLabel nameLabel = new JLabel(user.getFirstName() + "!");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(nameLabel);

        navPanel.add(Box.createVerticalStrut(10));

        
        JLabel adminTitle = new JLabel("Admin Dashboard");
        adminTitle.setForeground(Color.WHITE);
        adminTitle.setFont(new Font("Tahoma", Font.BOLD, 20));
        adminTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(adminTitle);

        navPanel.add(Box.createVerticalStrut(40));

      
        addRoleSpecificComponents();

        
        navPanel.add(Box.createVerticalGlue());
        JButton btnLogout = new JButton("Log out");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(190, 40));
        btnLogout.addActionListener(e -> {
            new LoginPanel().setVisible(true); 
            dispose();
        });
        navPanel.add(btnLogout);
        navPanel.add(Box.createVerticalStrut(20));
    }

    protected final void switchPanel(JPanel panel) {
        cardPanel.removeAll();
        cardPanel.add(panel, "Current");
        cardLayout.show(cardPanel, "Current");
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    protected abstract void addRoleSpecificComponents();
    
   // Inside BaseDashboard.java
public void render() {
    addRoleSpecificComponents(); // Adds the role-based buttons
    revalidate();
    repaint();
    setVisible(true); // Finally shows the window
}
}