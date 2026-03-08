package ui;

import dao.AttendanceDAO;
import dao.UserLibrary;
import java.awt.*;
import javax.swing.*;
import model.Employee;
import service.EmployeeManagementService;
import service.ITTicketService;

public class LoginPanel extends JFrame {

    private final EmployeeManagementService employeeService;
    private final AttendanceDAO attendanceDao;
    private final UserLibrary authService;

    private JTextField empField;
    private JPasswordField passField;

    public LoginPanel(EmployeeManagementService service, AttendanceDAO dao, UserLibrary auth) {
        this.employeeService = service;
        this.attendanceDao = dao;
        this.authService = auth;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("MotorPH Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createLeftPanel(), BorderLayout.WEST);
        add(createRightPanel(), BorderLayout.CENTER);

        empField.requestFocusInWindow();
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(128, 0, 0));
        leftPanel.setPreferredSize(new Dimension(400, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        try {
            java.net.URL imgURL = getClass().getResource("/logo.png");
            if (imgURL == null) imgURL = getClass().getResource("/resources/logo.png");

            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaled = icon.getImage().getScaledInstance(180, -1, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 20, 0);
                leftPanel.add(imageLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Logo load error: " + e.getMessage());
        }

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridy = 1;
        leftPanel.add(welcomeLabel, gbc);

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
        empField.addActionListener(e -> handleLogin());
        rightPanel.add(empField);

        JLabel passLabel = new JLabel("Password :");
        passLabel.setBounds(50, 120, 100, 25);
        rightPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(160, 120, 200, 25);
        passField.addActionListener(e -> handleLogin());
        rightPanel.add(passField);

        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setBounds(370, 120, 70, 25);
        showPassword.setBackground(Color.LIGHT_GRAY);
        showPassword.addActionListener(e ->
                passField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        rightPanel.add(showPassword);

        JButton loginButton = createStyledButton("Log in", new Color(30, 144, 255), Color.BLACK);
        loginButton.setBounds(160, 170, 90, 30);
        loginButton.addActionListener(e -> handleLogin());
        this.getRootPane().setDefaultButton(loginButton);
        rightPanel.add(loginButton);

        JButton exitButton = createStyledButton("Exit", Color.WHITE, Color.BLACK);
        exitButton.setBounds(270, 170, 90, 30);
        exitButton.addActionListener(e -> System.exit(0));
        rightPanel.add(exitButton);

        JButton forgotBtn = createStyledButton("Forgot Password?", Color.WHITE, Color.BLACK);
        forgotBtn.setBounds(160, 220, 200, 30);
        forgotBtn.addActionListener(e -> openForgotPasswordDialog());
        rightPanel.add(forgotBtn);

        JButton itSupportBtn = createStyledButton("IT Support", Color.WHITE, Color.BLACK);
        itSupportBtn.setBounds(160, 260, 200, 30);
        itSupportBtn.addActionListener(e -> openITSupportDialog());
        rightPanel.add(itSupportBtn);

        return rightPanel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(fg);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        if (bg.equals(new Color(30, 144, 255))) {
            btn.setBorder(new javax.swing.border.LineBorder(Color.WHITE, 1, true));
        } else {
            btn.setBorder(new javax.swing.border.LineBorder(Color.LIGHT_GRAY, 1, true));
        }
        return btn;
    }

    private void handleLogin() {
    String username = empField.getText().trim();
    String password = new String(passField.getPassword()).trim();

    if (authService.authenticate(username, password)) {
        Employee user = authService.getLoggedInEmployee();

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Login failed. User session was not created.");
            return;
        }

        if (authService.isPasswordChangeRequired()) {
            handleForcedPasswordChange(user);
            return;
        }

        navigateToDashboard(user);
    } else {
        JOptionPane.showMessageDialog(this, authService.getLastLoginMessage());
        }
    }

    private void navigateToDashboard(Employee user) {
        this.dispose();
        new DashboardPanel(employeeService, attendanceDao, authService, user).setVisible(true);
    }

    private void handleForcedPasswordChange(Employee user) {
    JOptionPane.showMessageDialog(
            this,
            "You logged in using a temporary password. You must create a new password now."
    );

    while (true) {
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        Object[] message = {
                "New Password:", newPassField,
                "Confirm Password:", confirmPassField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(
                    this,
                    "Password change is required before you can continue."
            );
            continue;
        }

        String newPassword = new String(newPassField.getPassword()).trim();
        String confirmPassword = new String(confirmPassField.getPassword()).trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both password fields are required.");
            continue;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.");
            continue;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            continue;
        }

        if (newPassword.equals(user.getPassword())) {
            JOptionPane.showMessageDialog(this, "New password must be different from the temporary password.");
            continue;
        }

        employeeService.getEmployeeDao().saveNewPassword(user.getEmpNo(), newPassword);
        employeeService.getEmployeeDao().setMustChangePassword(user.getEmpNo(), false);
        user.setPassword(newPassword);
        authService.clearPasswordChangeRequired();

        JOptionPane.showMessageDialog(this, "Password changed successfully.");
        navigateToDashboard(user);
        return;
        }
    }

    private boolean submitITTicket(String identifier, String issueType, String description) {
    ITTicketService ticketService = new ITTicketService(employeeService.getEmployeeDao());

    if (identifier.matches("\\d+")) {
        return ticketService.submitTicketByEmployeeNo(
                Integer.parseInt(identifier),
                issueType,
                description
        );
    } else {
        return ticketService.submitTicketByUsername(
                identifier,
                issueType,
                description
        );
    }
}
    //for Forgot password functionality / dialog to
    private void openForgotPasswordDialog() {  
    String identifier = JOptionPane.showInputDialog(
            this,
            "Enter Username or Employee Number:",
            "Forgot Password",
            JOptionPane.PLAIN_MESSAGE
    );





    if (identifier == null || identifier.trim().isEmpty()) {
        return;
    }

    String[] issueTypes = {"PASSWORD_RESET", "ACCOUNT_LOCKED"};
    String issueType = (String) JOptionPane.showInputDialog(
            this,
            "Select issue type:",
            "Forgot Password",
            JOptionPane.PLAIN_MESSAGE,
            null,
            issueTypes,
            issueTypes[0]
    );

    if (issueType == null) {
        return;
    }

    String description = JOptionPane.showInputDialog(
            this,
            "Describe the issue:",
            issueType.equals("ACCOUNT_LOCKED")
                    ? "I've been locked out and I need help with my access."
                    : "I need to reset my password."
    );

    if (description == null || description.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ticket description is required.");
        return;
    }

    boolean submitted = submitITTicket(identifier.trim(), issueType, description.trim());

        if (submitted) {
        JOptionPane.showMessageDialog(this,
                "Your password reset request has been submitted. Please wait for MotorPH IT team confirmation email before logging in again");
    } else {
        JOptionPane.showMessageDialog(this,
                "User not found. Please check the username or employee number.");
    }
}

private void openITSupportDialog() {
    String identifier = JOptionPane.showInputDialog(
            this,
            "Enter Username or Employee Number:",
            "IT Support",
            JOptionPane.PLAIN_MESSAGE
    );

    if (identifier == null || identifier.trim().isEmpty()) {
        return;
    }

    String[] issueTypes = {"TECHNICAL_SUPPORT", "LOGIN_ISSUE", "BUG_REPORT", "UI_ERROR"};
    String issueType = (String) JOptionPane.showInputDialog(
            this,
            "Select issue type:",
            "IT Support",
            JOptionPane.PLAIN_MESSAGE,
            null,
            issueTypes,
            issueTypes[0]
    );

    if (issueType == null) {
        return;
    }

    String description = JOptionPane.showInputDialog(
            this,
            "Describe the issue:",
            "Please describe the bug, error, or issue you encountered."
    );

    if (description == null || description.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ticket description is required.");
        return;
    }

    boolean submitted = submitITTicket(identifier.trim(), issueType, description.trim());

    if (submitted) {
        JOptionPane.showMessageDialog(this,
                "Your IT support ticket has been submitted.");
    } else {
        JOptionPane.showMessageDialog(this,
                "User not found. Please check the username or employee number.");
    }
}
}