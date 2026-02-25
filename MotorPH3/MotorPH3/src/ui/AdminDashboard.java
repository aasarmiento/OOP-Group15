package ui;

import dao.EmployeeDAO;
import java.awt.*;
import javax.swing.*;
import model.Employee;

public class AdminDashboard extends JFrame {
    // 1. Fields must be declared at the top level
    private final EmployeeDAO employeeDao;
    private final Employee user;

    // UI Components must be declared here, but initialized carefully
    private final JPanel navPanel = new JPanel();
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

    // Navigation Buttons
    private JButton btnDatabase, btnFullDetails, btnAddEmployee, btnAttendance, btnProfile, btnLeave;

    public AdminDashboard(EmployeeDAO dao, Employee user) {
        // 2. Assign constructor parameters to class fields
        this.employeeDao = dao;
        this.user = user;

        // 3. Configure the Frame
        setTitle("MotorPH Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 4. Initialize UI Logic
        initializeNavButtons();
        setupNavigationPanel();
        
        // 5. Initialize the Database Panel and add to the CardLayout
        // We do this INSIDE the constructor so 'employeeDao' is ready
        EmployeeDashboard dbPanel = new EmployeeDashboard(employeeDao); 
        cardPanel.add(dbPanel, "Database");

        // 6. Final Assembly of the Frame
        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        
        // Show the database by default
        switchPanel(dbPanel);
    }

    private void initializeNavButtons() {
        btnDatabase = new JButton("Employee Database");
        btnFullDetails = new JButton("View Full Details");
        btnAddEmployee = new JButton("Add Employee");
        btnAttendance = new JButton("Time");
        btnProfile = new JButton("My Profile");
        btnLeave = new JButton("Leave Application");

        JButton[] allButtons = {btnDatabase, btnFullDetails, btnAddEmployee, btnAttendance, btnProfile, btnLeave};

        for (JButton btn : allButtons) {
            if (btn != null) {
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(190, 40));
                btn.setFocusable(false);
                btn.setBackground(Color.WHITE);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }
        
        // Listeners for navigation
        btnDatabase.addActionListener(e -> switchPanel(new EmployeeDashboard(employeeDao)));
        // Example: btnAddEmployee.addActionListener(e -> switchPanel(new AddEmployeePanel(employeeDao)));
    }

    private void setupNavigationPanel() {
        navPanel.setBackground(new Color(128, 0, 0)); // Dark Red
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        navPanel.add(Box.createVerticalStrut(30));

        // Welcome Text
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

        // Adding buttons to sidebar
        navPanel.add(btnDatabase);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(btnFullDetails);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(btnAddEmployee);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(btnAttendance);

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
}