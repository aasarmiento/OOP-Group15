package ui;

import dao.CSVHandler;
import dao.EmployeeDAO;
import dao.UserLibrary;
import java.awt.*;
import javax.swing.*;
import model.Employee;
import model.Role;


public class LoginPanel extends JFrame {

    private final EmployeeDAO myHandler = new CSVHandler();
    private final UserLibrary authService = new UserLibrary(myHandler);
    
    private JTextField empField;
    private JPasswordField passField;
    private int loginAttempts = 0; // Now used in handleLogin()

    public LoginPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("MotorPH Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(128, 0, 0));
        leftPanel.setPreferredSize(new Dimension(400, 450));

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(60, 0, 20, 0));
        
        JLabel imageLabel = new JLabel(); // Placeholder for image
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);
        leftPanel.add(imageLabel, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.LIGHT_GRAY);

        JLabel loginLabel = new JLabel("Please log in with your credentials");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginLabel.setBounds(50, 30, 300, 25);
        rightPanel.add(loginLabel);

        JLabel empLabel = new JLabel("Username :");
        empLabel.setBounds(50, 80, 100, 25);
        rightPanel.add(empLabel);

        empField = new JTextField();
        empField.setBounds(160, 80, 200, 25);
        rightPanel.add(empField);

        JLabel passLabel = new JLabel("Password :");
        passLabel.setBounds(50, 120, 100, 25);
        rightPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(160, 120, 200, 25);
        rightPanel.add(passField);

        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setBounds(370, 120, 70, 25);
        showPassword.setBackground(Color.LIGHT_GRAY);
        showPassword.addActionListener(e -> passField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        rightPanel.add(showPassword);

        // 2. UIUtils is now recognized thanks to the import above
        JButton loginButton = UIUtils.createButton("Log in", Color.WHITE, Color.BLACK);
        loginButton.setBounds(160, 170, 90, 30);
        loginButton.addActionListener(e -> handleLogin());
        rightPanel.add(loginButton);

        JButton exitButton = UIUtils.createButton("Exit", Color.WHITE, Color.BLACK);
        exitButton.setBounds(270, 170, 90, 30);
        exitButton.addActionListener(e -> System.exit(0));
        rightPanel.add(exitButton);

        JButton resetButton = UIUtils.createButton("Forgot Password?", Color.WHITE, Color.BLACK);
        resetButton.setBounds(160, 220, 200, 30);
        resetButton.addActionListener(e -> handlePasswordReset());
        rightPanel.add(resetButton);

        return rightPanel;
    }

    private void handleLogin() {
        String username = empField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // 3. authService is now READ/USED here, clearing the warning
        if (authService.authenticate(username, password)) {
            Employee loggedUser = UserLibrary.getLoggedInEmployee();
            Role role = UserLibrary.getUserRole();
            
            JOptionPane.showMessageDialog(this, "Welcome, " + loggedUser.getFirstName() + "!");
            openDashboard(role, loggedUser);
        } else {
            // 4. loginAttempts is now READ/USED here, clearing the warning
            loginAttempts++; 
            if (loginAttempts >= 3) {
                JOptionPane.showMessageDialog(this, "Too many failed attempts. Closing.");
                System.exit(0);
            }
            JOptionPane.showMessageDialog(this, "Invalid credentials. Attempts left: " + (3 - loginAttempts));
        }
    }

    private void handlePasswordReset() {
        JOptionPane.showMessageDialog(this, 
            "Please contact the IT Department or your HR Manager to reset your password.", 
            "Password Reset", 
            JOptionPane.INFORMATION_MESSAGE);
    }

private void openDashboard(Role role, Employee user) {
    this.dispose(); 
    
    // Get position safely for department checks
    String pos = (user.getPosition() != null) ? user.getPosition().toLowerCase() : "";
    
    // 1. PRIORITY #1: Finance / Accounting / Payroll
    if (pos.contains("accounting") || pos.contains("payroll") || pos.contains("finance")) {
        System.out.println("Routing to AccountingDashboard...");
        new AccountingDashboard(myHandler, user).setVisible(true);
        return; 
    }

    // 2. PRIORITY #2: IT Department
    if (pos.contains("it") || pos.contains("information technology") || role == Role.IT_STAFF) {
        System.out.println("Access Level IT detected. Opening ITDashboard...");
        new ITDashboard(myHandler, user).setVisible(true);
        return; 
    }

    // 3. PRIORITY #3: HR Department
    if (pos.contains("hr") || role == Role.HR_STAFF) {
        System.out.println("Access Level HR detected. Opening HRDashboard...");
        new HRDashboard(myHandler, user).setVisible(true);
        return;
    }

    // 4. Default Role-based routing
    switch (role) {
        case ADMIN -> {
            System.out.println("Access Level Admin detected. Opening AdminDashboard...");
            new AdminDashboard(myHandler, user).setVisible(true);
        }
        case IT_STAFF -> {
            new ITDashboard(myHandler, user).setVisible(true);
        }
        case HR_STAFF -> {
            new HRDashboard(myHandler, user).setVisible(true);
        }
        default -> {
            // FIXED: Now opens your new RegularStaffDashboard
            System.out.println("Opening Regular Staff Dashboard...");
            new RegularStaffDashboard(myHandler, user).setVisible(true);
        }
    }
}
}