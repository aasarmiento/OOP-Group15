package ui;

import dao.AttendanceDAO;
import dao.UserLibrary;
import java.awt.*;
import javax.swing.*;
import model.Employee;
import service.EmployeeManagementService;

public class LoginPanel extends JFrame {

    private final EmployeeManagementService employeeService; 
    private final AttendanceDAO attendanceDao; 
    private final UserLibrary authService;

    private JTextField empField;
    private JPasswordField passField;
    private int loginAttempts = 0;

    private final Color BRAND_MAROON = new Color(88, 16, 16);
    private final Color INPUT_BG = new Color(248, 249, 250);

    public LoginPanel(EmployeeManagementService service, AttendanceDAO dao, UserLibrary auth) {
        this.employeeService = service;
        this.attendanceDao = dao;
        this.authService = auth;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("MotorPH Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550); 
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2)); 

        add(createLeftPanel());
        add(createRightPanel());
        
        // Finalize setup
        if (empField != null) {
            empField.requestFocusInWindow();
        }
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(BRAND_MAROON);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        try {
            java.io.File logoFile = new java.io.File("resources/logo.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(160, -1, Image.SCALE_SMOOTH);
                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 25, 0); 
                leftPanel.add(new JLabel(new ImageIcon(scaled)), gbc);
            } else {
                JLabel fallback = new JLabel("MotorPH");
                fallback.setFont(new Font("SansSerif", Font.BOLD, 40));
                fallback.setForeground(Color.WHITE);
                leftPanel.add(fallback, gbc);
            }
        } catch (Exception e) {
            // Silently fail if logo not found
        }

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        leftPanel.add(welcomeLabel, gbc);

        JLabel subLabel = new JLabel("The Filipino's Choice");
        subLabel.setForeground(new Color(220, 220, 220));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridy = 2;
        leftPanel.add(subLabel, gbc);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 60, 5, 60); 
        gbc.gridx = 0;

        JLabel header = new JLabel("Welcome Back");
        header.setFont(new Font("SansSerif", Font.BOLD, 26));
        gbc.gridy = 0;
        rightPanel.add(header, gbc);

        JLabel subHeader = new JLabel("Log in to your MotorPH dashboard");
        subHeader.setForeground(Color.GRAY);
        subHeader.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 60, 35, 60); 
        rightPanel.add(subHeader, gbc);

        // Username
        gbc.insets = new Insets(5, 60, 5, 60);
        gbc.gridy = 2;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rightPanel.add(userLabel, gbc);
        
        empField = new JTextField();
        styleField(empField);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 60, 20, 60); 
        rightPanel.add(empField, gbc);

        // Password
        gbc.insets = new Insets(5, 60, 5, 60);
        gbc.gridy = 4;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rightPanel.add(passLabel, gbc);

        passField = new JPasswordField();
        styleField(passField);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 60, 8, 60);
        rightPanel.add(passField, gbc);

        JPanel options = new JPanel(new BorderLayout());
        options.setBackground(Color.WHITE);
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBackground(Color.WHITE);
        showPass.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPass.addActionListener(e -> passField.setEchoChar(showPass.isSelected() ? (char)0 : '•'));
        
        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        forgotBtn.setForeground(BRAND_MAROON);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Contact IT Support."));
        
        options.add(showPass, BorderLayout.WEST);
        options.add(forgotBtn, BorderLayout.EAST);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 60, 30, 60); 
        rightPanel.add(options, gbc);

        JButton loginBtn = new JButton("Log In");
        loginBtn.setBackground(BRAND_MAROON);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(0, 50)); 
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 60, 20, 60);
        rightPanel.add(loginBtn, gbc);
        this.getRootPane().setDefaultButton(loginBtn);

        return rightPanel;
    }

    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(0, 42)); 
        field.setBackground(INPUT_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void handleLogin() {
        String username = empField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (authService.authenticate(username, password)) {
            Employee user = authService.getLoggedInEmployee();
            if (user != null) {
                navigateToDashboard(user);
            } else {
                handleFailedAttempt();
            }
        } else {
            handleFailedAttempt();
        }
    }

    private void navigateToDashboard(Employee user) {
        this.dispose(); 
        // Correctly passing the services to Dashboard
        new DashboardPanel(employeeService, attendanceDao, authService, user).setVisible(true);
    }

    private void handleFailedAttempt() {
        loginAttempts++;
        if (loginAttempts >= 3) {
            JOptionPane.showMessageDialog(this, "Too many failed attempts. Closing.");
            System.exit(0);
        }
        JOptionPane.showMessageDialog(this, "Invalid credentials. Attempts left: " + (3 - loginAttempts));
    }
}