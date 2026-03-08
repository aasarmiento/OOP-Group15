package ui;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.UserLibrary;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import model.Employee;
import service.EmployeeManagementService;
import service.ITSupportService;
import service.ITTicketService;


public class DashboardPanel extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    
    private final EmployeeManagementService employeeService; 
    private final AttendanceDAO attendanceDao;
    private final UserLibrary authService;
    private final Employee currentUser; 

    public EmployeeDatabase databasePanel;

    private final String userRole;
    private final String userEmpNo;
    private final String userLoggedIn;
    private final String userFirstname;
    private final String userLastname;

    // Panels
    public JPanel personalInfoPanel;    
    public AddEmployeePanel addEmpPanel;
    public FullDetailsPanel fullEmpPanel;
    public LeavePanel leaveApp; 
    public TimePanel timeEmpPanel;
    
    public JPanel itApprovalPanel;
    public ITSupportPanel itSupportPanel; //updated this for IT support panel
    public JPanel leaveApprovalPanel;
    public JPanel payrollFinancePanel;

    // UI Components for the Home Panel
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JLabel lblProfilePic;

    public DashboardPanel(EmployeeManagementService empService, AttendanceDAO attDao, UserLibrary auth, Employee user) {
    // 1. Assign your dependencies
    this.employeeService = empService;
    this.attendanceDao = attDao;
    this.authService = auth;
    this.currentUser = user; 

        this.userRole = user.getRole().name();
        this.userEmpNo = String.valueOf(user.getEmpNo());
        this.userLoggedIn = userEmpNo; 
        this.userFirstname = user.getFirstName();
        this.userLastname = user.getLastName();

        // --- STEP 1: INITIALIZE ALL PANELS ---

        
        this.personalInfoPanel = createHomePanel(); 
        this.addEmpPanel = new AddEmployeePanel(employeeService);
        this.fullEmpPanel = new FullDetailsPanel(employeeService, currentUser);
        this.leaveApp = new LeavePanel();
    this.timeEmpPanel = new TimePanel(employeeService, currentUser);
        this.databasePanel = new EmployeeDatabase(employeeService, currentUser);

        // Explicitly creating placeholders for the modules you'll build later
        this.itApprovalPanel = createPlaceholderPanel("IT Approval Module");
        this.leaveApprovalPanel = createPlaceholderPanel("Manager Leave Approval");
        this.payrollFinancePanel = createPlaceholderPanel("Payroll & My Payslip");

        // fot IT ticketing panel to
        EmployeeDAO employeeDao = employeeService.getEmployeeDao();
        ITSupportService itSupportService = new ITSupportService(employeeDao);
        ITTicketService ticketService = new ITTicketService(employeeDao);

        this.itSupportPanel = new ITSupportPanel(currentUser, itSupportService, ticketService);

        // --- STEP 2: FRAME SETUP ---
        setTitle("MotorPH Dashboard - " + userRole); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);                                            
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- STEP 3: CARD PANEL SETUP ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        cardPanel.add(personalInfoPanel, "Home"); 
        cardPanel.add(databasePanel, "Database"); 
        cardPanel.add(addEmpPanel, "AddEmployee");
        cardPanel.add(fullEmpPanel, "FullDetails");
        cardPanel.add(leaveApp, "Leave");
        cardPanel.add(timeEmpPanel, "Time");
        cardPanel.add(itApprovalPanel, "IT_Approval");
        cardPanel.add(itSupportPanel, "IT_Support");
        cardPanel.add(leaveApprovalPanel, "Leave_Approval");
        cardPanel.add(payrollFinancePanel, "Payroll");

        // --- STEP 4: NAVIGATION PANEL (MATCHING YOUR SCREENSHOT) ---
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(128, 0, 0));
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        JLabel adminTitle = new JLabel("MotorPH Admin");
        adminTitle.setForeground(Color.WHITE);
        adminTitle.setFont(new Font("Arial", Font.BOLD, 22));
        adminTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Create buttons exactly as seen in your screenshot
        JButton btnMyProfile = UIUtils.createNavButton("My Profile", Color.white, Color.white);
        JButton btnMyPayslip = UIUtils.createNavButton("My Payslip", Color.white, Color.white);
        JButton btnDatabase = UIUtils.createNavButton("Employee Database", Color.white, Color.white);
        JButton btnAttendance = UIUtils.createNavButton("Attendance", Color.white, Color.white);
        JButton btnLeaveRequest = UIUtils.createNavButton("Leave Request", Color.white, Color.white);
        JButton btnITApproval = UIUtils.createNavButton("IT Approval", Color.white, Color.white);
        JButton btnITSupport = UIUtils.createNavButton("IT Support", Color.white, Color.white);
        JButton btnLeaveApproval = UIUtils.createNavButton("Leave Approval", Color.white, Color.white);
        JButton btnPayrollFinances = UIUtils.createNavButton("Payroll & Finances", Color.white, Color.white);
        JButton btnLogout = UIUtils.createNavButton("Log out", Color.white, Color.white);

        navPanel.add(adminTitle);

        // Adding components to nav bar
        addNavComponent(navPanel, btnMyProfile);
        addNavComponent(navPanel, btnMyPayslip);
        addNavComponent(navPanel, btnDatabase);
        addNavComponent(navPanel, btnAttendance);
        addNavComponent(navPanel, btnLeaveRequest);
        addNavComponent(navPanel, btnITApproval);
        addNavComponent(navPanel, btnITSupport);
        addNavComponent(navPanel, btnLeaveApproval);
        addNavComponent(navPanel, btnPayrollFinances);
        
        navPanel.add(Box.createVerticalGlue());
        addNavComponent(navPanel, btnLogout);

        // --- STEP 5: LISTENERS ---
        btnMyProfile.addActionListener(e -> switchPanel("Home"));
        btnMyPayslip.addActionListener(e -> switchPanel("Payroll")); // Currently using the payroll placeholder
        
        btnDatabase.addActionListener(e -> { 
            if (databasePanel != null) {
                databasePanel.refreshTable(); 
            }
            switchPanel("Database"); 
        });

        btnAttendance.addActionListener(e -> { 
            timeEmpPanel.setLoggedIn(userLoggedIn, userLastname, userFirstname); 
            switchPanel("Time"); 
        });
        
        btnLeaveRequest.addActionListener(e -> switchPanel("Leave"));
        btnITApproval.addActionListener(e -> switchPanel("IT_Approval"));
        btnITSupport.addActionListener(e -> switchPanel("IT_Support"));
        btnLeaveApproval.addActionListener(e -> switchPanel("Leave_Approval"));
        btnPayrollFinances.addActionListener(e -> switchPanel("Payroll"));
        
        btnLogout.addActionListener(e -> { 
            dispose(); 
            new LoginPanel(employeeService, attendanceDao, authService).setVisible(true);
        });

        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        
        loadPersonalDetails(currentUser); 
        switchPanel("Home"); 
        setVisible(true);
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // KPI Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiRow.setOpaque(false);

        java.util.List<model.Attendance> logs = attendanceDao.getAttendanceByEmployee(currentUser.getEmpNo());
        double totalHours = 0;
        for (model.Attendance a : logs) {
            if (a.getTimeIn() != null && a.getTimeOut() != null) {
                java.time.Duration duration = java.time.Duration.between(a.getTimeIn(), a.getTimeOut());
                totalHours += duration.toMinutes() / 60.0;
            }
        }
        kpiRow.add(createKPICard("Available Leave", "12 Days", new Color(128, 0, 0)));
        kpiRow.add(createKPICard("Hours This Period", String.format("%.1f Hrs", totalHours), Color.DARK_GRAY));
        kpiRow.add(createKPICard("Attendance", logs.isEmpty() ? "0%" : "95%", new Color(0, 102, 51)));
        
        JPanel centerColumn = new JPanel(new BorderLayout(0, 20));
        centerColumn.setOpaque(false);

        JPanel profileHeader = createStyledTile();
        profileHeader.setLayout(new BorderLayout(20, 0));
        profileHeader.setPreferredSize(new Dimension(0, 120));
        
        lblProfilePic = new JLabel();
        lblProfilePic.setPreferredSize(new Dimension(100, 100));
        lblProfilePic.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 0), 2));
        profileHeader.add(lblProfilePic, BorderLayout.WEST);

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        txtFirstName = addTransparentField(headerText, "Display Name:");
        txtPosition = addTransparentField(headerText, "Current Role:");
        profileHeader.add(headerText, BorderLayout.CENTER);

        JPanel detailsArea = new JPanel();
        detailsArea.setLayout(new BoxLayout(detailsArea, BoxLayout.Y_AXIS));
        detailsArea.setOpaque(false);
        
        JPanel pPersonal = createStyledTile();
        pPersonal.setBorder(BorderFactory.createTitledBorder(null, "Personal Details", 0, 0, null, new Color(128, 0, 0)));
        pPersonal.setLayout(new GridLayout(4, 1, 0, 5));
        txtEmpNo = addTransparentField(pPersonal, "Employee ID:");
        txtLastName = addTransparentField(pPersonal, "Full Name:"); 
        txtBirthday = addTransparentField(pPersonal, "Birthday:");
        txtSupervisor = addTransparentField(pPersonal, "Supervisor:");
        
        JPanel pContact = createStyledTile();
        pContact.setBorder(BorderFactory.createTitledBorder(null, "Contact Information", 0, 0, null, new Color(128, 0, 0)));
        pContact.setLayout(new GridLayout(2, 1, 0, 5));
        txtAddress = addTransparentField(pContact, "Address:");
        txtPhone = addTransparentField(pContact, "Contact Number:");

        JPanel pGov = createStyledTile();
        pGov.setBorder(BorderFactory.createTitledBorder(null, "Government Details", 0, 0, null, new Color(128, 0, 0)));
        pGov.setLayout(new GridLayout(4, 1, 0, 5));
        txtSss = addTransparentField(pGov, "SSS #:");
        txtPhilHealth = addTransparentField(pGov, "Philhealth #:");
        txtTin = addTransparentField(pGov, "TIN #:");
        txtPagibig = addTransparentField(pGov, "Pagibig #:");

        detailsArea.add(pPersonal);
        detailsArea.add(Box.createVerticalStrut(15));
        detailsArea.add(pContact);
        detailsArea.add(Box.createVerticalStrut(15));
        detailsArea.add(pGov);
        
        centerColumn.add(profileHeader, BorderLayout.NORTH);
        centerColumn.add(detailsArea, BorderLayout.CENTER);

        JPanel announcementBar = new JPanel(new BorderLayout(0, 10));
        announcementBar.setPreferredSize(new Dimension(250, 0));
        announcementBar.setOpaque(false);
        
        JLabel annTitle = new JLabel("Announcements");
        annTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        annTitle.setForeground(new Color(128, 0, 0));
        
        JPanel annCard = createStyledTile();
        annCard.setLayout(new BoxLayout(annCard, BoxLayout.Y_AXIS));
        JLabel eventTitle = new JLabel("🎄 Christmas Event");
        eventTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        JTextArea eventDesc = new JTextArea("MotorPH is celebrating Christmas! Annual event on Dec 20th.");
        eventDesc.setWrapStyleWord(true);
        eventDesc.setLineWrap(true);
        eventDesc.setOpaque(false);
        eventDesc.setEditable(false);
        
        annCard.add(eventTitle);
        annCard.add(Box.createVerticalStrut(10));
        annCard.add(eventDesc);

        announcementBar.add(annTitle, BorderLayout.NORTH);
        announcementBar.add(annCard, BorderLayout.CENTER);

        mainPanel.add(kpiRow, BorderLayout.NORTH);
        mainPanel.add(centerColumn, BorderLayout.CENTER);
        mainPanel.add(announcementBar, BorderLayout.EAST);

        return mainPanel;
    }

    private void loadPersonalDetails(Employee emp) {
        if (emp == null || txtEmpNo == null) return; 
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        txtFirstName.setText(emp.getFirstName() + " " + emp.getLastName());
        txtPosition.setText(emp.getPosition());
        txtEmpNo.setText(String.valueOf(emp.getEmpNo()));
        txtLastName.setText(emp.getFirstName() + " " + emp.getLastName());
        if (emp.getBirthday() != null) txtBirthday.setText(emp.getBirthday().format(dateFormatter));
        txtSupervisor.setText(emp.getSupervisor());
        txtAddress.setText(emp.getAddress());
        txtPhone.setText(emp.getPhone());
        txtSss.setText(emp.getSss());
        txtPhilHealth.setText(emp.getPhilhealth());
        txtTin.setText(emp.getTin());
        txtPagibig.setText(emp.getPagibig());
        displayEmployeePhoto(lblProfilePic);
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.ITALIC, 24));
        label.setForeground(new Color(128, 0, 0)); 
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private void displayEmployeePhoto(JLabel lblPhoto) {
        String path = currentUser.getPhotoPath();
        File imgFile = (path != null) ? new File(path) : null;
        if (imgFile == null || !imgFile.exists()) path = "resources/profile_pics/default.png"; 

        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblPhoto.setIcon(new ImageIcon(img));
            lblPhoto.setText(""); 
        } catch (Exception e) {
            lblPhoto.setText("No Image");
        }
    }

    private JPanel createKPICard(String title, String value, Color themeColor) {
        JPanel card = createStyledTile();
        card.setLayout(new GridLayout(2, 1));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblValue.setForeground(themeColor);
        card.add(lblTitle);
        card.add(lblValue);
        return card;
    }

    private void addNavComponent(JPanel panel, JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40)); // Increased width slightly for longer text
        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }

    private void switchPanel(String cardName) { cardLayout.show(cardPanel, cardName); }

    private JPanel createStyledTile() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JTextField addTransparentField(JPanel panel, String labelText) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(Color.GRAY);
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBorder(null);
        field.setOpaque(false);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.BLACK);
        container.add(label, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);
        panel.add(container);
        return field;
    }



    
}