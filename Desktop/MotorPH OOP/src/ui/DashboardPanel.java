package ui;

import dao.AttendanceDAO;
import dao.ITTicketDAOImpl;
import dao.ITTicketDao;
import dao.UserLibrary; 
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Attendance;
import model.Employee;
import model.Role;
import service.EmployeeManagementService;
import service.ITSupportService;
import service.LeaveService;
import service.PayrollCalculator;

public class DashboardPanel extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    
    private final EmployeeManagementService employeeService; 
    private final AttendanceDAO attendanceDao;
    private final UserLibrary authService;
    private final Employee currentUser; 

    public LeaveService leaveService; 
    public EmployeeDatabase databasePanel;

    private final String userRole;
    private final String userEmpNo;
    private final String userLoggedIn;
    private final String userFirstname;
    private final String userLastname;

    // Font Definitions
    private final Font titleFont = new Font("DM Sans Bold", Font.BOLD, 20);
    private final Font bodyFont = new Font("DM Sans Regular", Font.PLAIN, 14);
    private final Font bodyFontSmall = new Font("DM Sans Regular", Font.PLAIN, 11);
    private final Font cardTitleFont = new Font("DM Sans Bold", Font.BOLD, 12);
    private final Font cardValueFont = new Font("DM Sans Bold", Font.BOLD, 22);

    // Panels
    public JPanel personalInfoPanel;    
    public AddEmployeePanel addEmpPanel;
    public FullDetailsPanel fullEmpPanel;
    public LeaveRequestPanel leaveApp;
    public TimePanel timeEmpPanel;
    
    public JPanel itApprovalPanel;
    public ITSupportPanel itSupportPanel; 
    public LeaveApprovalPanel leaveApprovalPanel; 
    public PayrollFinances payrollFinancePanel; 
    public MyPayslip payslipPanel; 

    private JTextField txtEmpNo, txtLastName, txtFirstName, txtPosition, txtSupervisor;
    private JTextField txtBirthday,txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextArea txtAddress;
    private JTextField txtStatus, txtTenured; 
    private JLabel lblProfilePic;

    // PROMOTE BUTTONS TO CLASS FIELDS
    private JButton btnMyProfile, btnMyPayslip, btnDatabase, btnAttendance, btnLeaveRequest;
    private JButton btnITApproval, btnITSupport, btnLeaveApproval, btnPayrollFinances, btnLogout;

    public DashboardPanel(EmployeeManagementService empService, AttendanceDAO attDao, UserLibrary auth, Employee user) {
        this.employeeService = empService; 
        this.attendanceDao = attDao;
        this.authService = auth;
        this.currentUser = user;

        this.userRole = user.getRole().name();
        this.userEmpNo = String.valueOf(user.getEmpNo());
        this.userLoggedIn = userEmpNo; 
        this.userFirstname = user.getFirstName();
        this.userLastname = user.getLastName();

        ITTicketDao itTicketDao = new ITTicketDAOImpl(); 
        ITSupportService itService = new ITSupportService(itTicketDao, employeeService.getEmployeeDao());
        this.leaveService = new LeaveService(employeeService.getEmployeeDao(), attendanceDao);

        // INITIALIZE SERVICE FOR LEAVE APPROVAL
        service.HRSerbisyo hrSerbisyo = new service.HRSerbisyo(employeeService.getEmployeeDao());

        this.personalInfoPanel = createHomePanel(); 
        this.addEmpPanel = new AddEmployeePanel(employeeService);
        this.fullEmpPanel = new FullDetailsPanel(employeeService, currentUser);
        this.leaveApp = new LeaveRequestPanel(leaveService, currentUser);
        this.timeEmpPanel = new TimePanel(employeeService, currentUser);
        this.databasePanel = new EmployeeDatabase(employeeService, currentUser);
        this.itSupportPanel = new ITSupportPanel(itService, currentUser);
        this.itApprovalPanel = new ITApprovalPanel(itService, currentUser);
        
        this.leaveApprovalPanel = new LeaveApprovalPanel(leaveService, hrSerbisyo, currentUser);
        
        PayrollCalculator payrollCalc = new PayrollCalculator(); 
        service.PayrollService payrollService = new service.PayrollService(
            employeeService.getEmployeeDao(), payrollCalc, employeeService
        );

        this.payslipPanel = new MyPayslip(employeeService, payrollCalc, payrollService, currentUser);
        this.payrollFinancePanel = new PayrollFinances(employeeService, payrollService, payrollCalc, currentUser);

        setTitle("MotorPH Dashboard - " + userRole); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);                                            
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        JPanel homeCenteringWrapper = new JPanel(new GridBagLayout());
        homeCenteringWrapper.setBackground(new Color(245, 245, 245));
        homeCenteringWrapper.add(personalInfoPanel, new GridBagConstraints());
        
        cardPanel.add(homeCenteringWrapper, "Home"); 
        cardPanel.add(payslipPanel, "My_Payslip");
        cardPanel.add(databasePanel, "Database"); 
        cardPanel.add(addEmpPanel, "AddEmployee");
        cardPanel.add(fullEmpPanel, "FullDetails");
        cardPanel.add(leaveApp, "Leave");
        cardPanel.add(timeEmpPanel, "Time");
        cardPanel.add(itApprovalPanel, "IT_Approval");
        cardPanel.add(itSupportPanel, "IT_Support");
        cardPanel.add(leaveApprovalPanel, "Leave_Approval");
        cardPanel.add(payrollFinancePanel, "Payroll_Finances");

        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(248, 248, 248)); 
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25)); 
        logoPanel.setOpaque(false);
        logoPanel.setMaximumSize(new Dimension(220, 80)); 

        try {
            ImageIcon logoIcon = new ImageIcon("/Users/abigail/Desktop/311 721/resources/Vector.png");
            Image img = logoIcon.getImage();
            double ratio = (double) img.getWidth(null) / img.getHeight(null);
            int targetHeight = 24; 
            int targetWidth = (int) (targetHeight * ratio); 
            Image hdImage = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            logoPanel.add(new JLabel(new ImageIcon(hdImage)));
        } catch (Exception e) {}

        JLabel lblBrand = new JLabel("MotorPH");
        lblBrand.setForeground(new Color(45, 45, 45));
        lblBrand.setFont(titleFont); 
        logoPanel.add(lblBrand);

        navPanel.add(logoPanel);
        navPanel.add(Box.createVerticalStrut(10));

        // INITIALIZE BUTTON FIELDS
        btnMyProfile = new NavButton("My Profile", "./resources/dashboard.png");
        btnMyPayslip = new NavButton("My Payslip", "./resources/leaverequest.png");
        btnDatabase = new NavButton("Employee Database", "./resources/employee_database.png");
        btnAttendance = new NavButton("Attendance", "./resources/Attendance.png");
        btnLeaveRequest = new NavButton("Leave Request", "./resources/leaverequest.png");
        btnITApproval = new NavButton("IT Approval", "./resources/ITApprroval.png");
        btnITSupport = new NavButton("IT Support", "./resources/ITSupport.png");
        btnLeaveApproval = new NavButton("Leave Approval", "./resources/leaveapprove.png");
        btnPayrollFinances = new NavButton("Payroll & Finances", "./resources/leaveapprove.png");
        btnLogout = new NavButton("Logout", "./resources/SignOut.png");

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
        navPanel.add(Box.createVerticalStrut(40)); 

        btnMyProfile.addActionListener(e -> switchPanel("Home"));
        btnMyPayslip.addActionListener(e -> { if (payslipPanel != null) payslipPanel.calculateSalary(); switchPanel("My_Payslip"); });
        btnDatabase.addActionListener(e -> { if (databasePanel != null) databasePanel.refreshTable(); switchPanel("Database"); });
        btnAttendance.addActionListener(e -> { timeEmpPanel.setLoggedIn(userLoggedIn, userLastname, userFirstname); switchPanel("Time"); });
        btnLeaveRequest.addActionListener(e -> { if (leaveApp != null) leaveApp.refreshUI(); switchPanel("Leave"); });       
        
        btnITApproval.addActionListener(e -> { 
            switchPanel("IT_Approval");
            if (itApprovalPanel instanceof ITApprovalPanel) {
                ((ITApprovalPanel) itApprovalPanel).refreshUI();
            }
            itApprovalPanel.revalidate();
            itApprovalPanel.repaint();
        });

        btnITSupport.addActionListener(e -> { 
            if (itSupportPanel != null) itSupportPanel.refreshUI(); 
            switchPanel("IT_Support"); 
        });

        btnLeaveApproval.addActionListener(e -> { 
            switchPanel("Leave_Approval"); 
            if (leaveApprovalPanel != null) {
                leaveApprovalPanel.refreshUI();
                leaveApprovalPanel.revalidate();
                leaveApprovalPanel.repaint();
            }
        });

        btnPayrollFinances.addActionListener(e -> { 
            switchPanel("Payroll_Finances");
            if (payrollFinancePanel != null) {
                payrollFinancePanel.refreshData();
                payrollFinancePanel.revalidate();
                payrollFinancePanel.repaint();
            }
        });

        // UPDATED LOGOUT WITH VALIDATION AND DIALOG
        btnLogout.addActionListener(e -> {
            // 1. Check Attendance Validation
            java.util.List<Attendance> logs = attendanceDao.getAttendanceByEmployee(currentUser.getEmpNo());
            boolean hasActiveSession = false;
            for (Attendance log : logs) {
                if (log.getTimeIn() != null && log.getTimeOut() == null) {
                    hasActiveSession = true;
                    break;
                }
            }

            if (hasActiveSession) {
                JOptionPane.showMessageDialog(this, 
                    "You are still clocked in. Please go to the Attendance tab and 'Time Out' before logging out.", 
                    "Active Session Detected", 
                    JOptionPane.WARNING_MESSAGE);
                return; // Stop logout
            }

            // 2. Confirm Logout
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); 
                new LoginPanel(employeeService, attendanceDao, authService).setVisible(true);
            }
        });

        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        loadPersonalDetails(currentUser); 
        
        applyRolePermissions(); 
        
        switchPanel("Home"); 
        setVisible(true);
    }

    private void applyRolePermissions() {
        Role role = currentUser.getRole();

        // Initially hide all restricted buttons
        btnDatabase.setVisible(false);
        btnITApproval.setVisible(false);
        btnLeaveApproval.setVisible(false);
        btnPayrollFinances.setVisible(false);

        switch (role) {
            case ADMIN:
                // Admin can see everything
                btnDatabase.setVisible(true);
                btnITApproval.setVisible(true);
                btnLeaveApproval.setVisible(true);
                btnPayrollFinances.setVisible(true);
                break;
            case IT_STAFF:
                btnITApproval.setVisible(true);
                break;
            case HR_STAFF:
                // HR Staff can see Leave Approval, but NOT the Database
                btnLeaveApproval.setVisible(true);
                break;
            case ACCOUNTING:
                btnPayrollFinances.setVisible(true);
                break;
            case REGULAR_STAFF:
            default:
                // Regular staff only see their profile, payslip, attendance, and leave requests
                break;
        }
        
        // Refresh the navigation panel to reflect visibility changes
        if (btnMyProfile != null && btnMyProfile.getParent() != null) {
            btnMyProfile.getParent().revalidate();
            btnMyProfile.getParent().repaint();
        }
    }

    private void addNavComponent(JPanel panel, JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 38)); 
        panel.add(button);
        panel.add(Box.createVerticalStrut(4)); 
    }

    private void switchPanel(String cardName) { cardLayout.show(cardPanel, cardName); }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        footer.setBackground(new Color(128, 0, 0)); 
        footer.setBorder(new EmptyBorder(5, 20, 5, 20));

        JLabel copy = new JLabel("<html><body>Copyright &copy; <b>2026 MotorPH</b></body></html>");
        copy.setFont(bodyFontSmall); 
        copy.setForeground(Color.WHITE); 
        
        JLabel privacy = new JLabel("Privacy Policy");
        privacy.setFont(bodyFontSmall); 
        privacy.setForeground(Color.WHITE);
        privacy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel terms = new JLabel("Terms and conditions");
        terms.setFont(bodyFontSmall); 
        terms.setForeground(Color.WHITE);
        terms.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel contact = new JLabel("Contact");
        contact.setFont(bodyFontSmall); 
        contact.setForeground(Color.WHITE);
        contact.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(copy);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(privacy);
        footer.add(terms);
        footer.add(contact);
        return footer;
    }

    private JPanel createHomePanel() {
        Color figmaBackground = new Color(245, 245, 245); 
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(figmaBackground);
        mainPanel.setPreferredSize(new Dimension(950, 720));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
// --- KPI Row: Uniform Light Pink Branding ---
        Color kpiPink = new Color(255, 173, 173); // Your specific light pink color
        
        kpiRow.add(createCircularKPICard("Available Leave", 14, 20, kpiPink));
        kpiRow.add(createCircularKPICard("Hours This Period", (int)totalHours, 160, kpiPink));
        kpiRow.add(createCircularKPICard("Attendance", logs.isEmpty() ? 0 : 95, 100, kpiPink));
        
        JPanel centerColumn = new JPanel(new BorderLayout(0, 20));
        centerColumn.setOpaque(false);

        JPanel profileHeader = createStyledTile();
        profileHeader.setLayout(new BorderLayout(20, 0));
        profileHeader.setPreferredSize(new Dimension(0, 160)); 
        
        JPanel photoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        photoWrapper.setOpaque(false);

        lblProfilePic = new RoundedImageLabel(); 
        lblProfilePic.setPreferredSize(new Dimension(110, 110));
        lblProfilePic.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblProfilePic.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    employeeService.updateEmployeePhoto(currentUser, fileChooser.getSelectedFile());
                    displayEmployeePhoto(lblProfilePic); 
                }
            }
        });

        photoWrapper.add(lblProfilePic);
        profileHeader.add(photoWrapper, BorderLayout.WEST);

        JPanel headerText = new JPanel(new GridLayout(2, 2, 20, 5));
        headerText.setOpaque(false);
        txtFirstName = addTransparentField(headerText, "Display Name:");
        txtStatus = addTransparentField(headerText, "Status:"); 
        txtPosition = addTransparentField(headerText, "Current Role:");
        txtTenured = addTransparentField(headerText, "E-mail address:"); 
        profileHeader.add(headerText, BorderLayout.CENTER);

        JPanel gridContainer = new JPanel(new GridLayout(2, 2, 20, 20));
        gridContainer.setOpaque(false);
        
        JPanel pPersonal = createStyledTile();
        pPersonal.setBorder(BorderFactory.createTitledBorder(null, "Personal Details", 0, 0, cardTitleFont, new Color(128, 0, 0)));
        pPersonal.setLayout(new GridLayout(4, 1, 0, 5));
        txtEmpNo = addTransparentField(pPersonal, "Employee ID:");
        txtLastName = addTransparentField(pPersonal, "Full Name:"); 
        txtBirthday = addTransparentField(pPersonal, "Birthday:");
        txtSupervisor = addTransparentField(pPersonal, "Supervisor:");
        
        JPanel pContact = createStyledTile();
        pContact.setBorder(BorderFactory.createTitledBorder(null, "Contact Information", 0, 0, cardTitleFont, new Color(128, 0, 0)));
        pContact.setLayout(new GridLayout(2, 1, 0, 5));
        txtAddress = addTransparentTextArea(pContact, "Address:");
        txtPhone = addTransparentField(pContact, "Contact Number:");

        JPanel pGov = createStyledTile();
        pGov.setBorder(BorderFactory.createTitledBorder(null, "Government Details", 0, 0, cardTitleFont, new Color(128, 0, 0)));
        pGov.setLayout(new GridLayout(4, 1, 0, 5));
        txtSss = addTransparentField(pGov, "SSS #:");
        txtPhilHealth = addTransparentField(pGov, "Philhealth #:");
        txtTin = addTransparentField(pGov, "TIN #:");
        txtPagibig = addTransparentField(pGov, "Pagibig #:");

        JPanel pAnnounce = createStyledTile();
        pAnnounce.setBorder(BorderFactory.createTitledBorder(null, "Announcements", 0, 0, cardTitleFont, new Color(128, 0, 0)));
        pAnnounce.setLayout(new BorderLayout(0, 10));
        JLabel eventTitle = new JLabel("🎄 Christmas Event");
        eventTitle.setFont(new Font("DM Sans Bold", Font.BOLD, 14));
        JTextArea eventDesc = new JTextArea("MotorPH is celebrating Christmas! Annual event on Dec 20th.");
        eventDesc.setWrapStyleWord(true);
        eventDesc.setLineWrap(true);
        eventDesc.setOpaque(false);
        eventDesc.setEditable(false);
        eventDesc.setFont(bodyFont);
        pAnnounce.add(eventTitle, BorderLayout.NORTH);
        pAnnounce.add(eventDesc, BorderLayout.CENTER);

        gridContainer.add(pPersonal); 
        gridContainer.add(pContact);
        gridContainer.add(pGov); 
        gridContainer.add(pAnnounce);
        
        centerColumn.add(profileHeader, BorderLayout.NORTH);
        centerColumn.add(gridContainer, BorderLayout.CENTER);

        mainPanel.add(kpiRow, BorderLayout.NORTH);
        mainPanel.add(centerColumn, BorderLayout.CENTER);

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
        txtStatus.setText(""); 
        txtTenured.setText("");
        displayEmployeePhoto(lblProfilePic);
    }

    private void displayEmployeePhoto(JLabel lblPhoto) {
        int empId = currentUser.getEmpNo();
        File imgFile = new File("resources/profile_pics/" + empId + ".png");
        if (!imgFile.exists()) imgFile = new File("resources/profile_pics/" + empId + ".jpg");
        if (!imgFile.exists()) imgFile = new File("resources/profile_pics/default.png");
        try {
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                lblPhoto.setIcon(new ImageIcon(img));
                lblPhoto.setText(""); 
            } else { lblPhoto.setIcon(null); lblPhoto.setText("No Image"); }
        } catch (Exception e) { lblPhoto.setText("Error"); }
    }

    private JPanel createKPICard(String title, String value, Color themeColor) {
        JPanel card = createStyledTile(); card.setLayout(new GridLayout(2, 1));
        JLabel lblTitle = new JLabel(title); lblTitle.setFont(cardTitleFont); lblTitle.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value); lblValue.setFont(cardValueFont); lblValue.setForeground(themeColor);
        card.add(lblTitle); card.add(lblValue); return card;
    }

    private JPanel createStyledTile() {
        Color elevatedBg = new Color(248, 248, 248); 
        Color strokeColor = new Color(225, 225, 225);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        panel.setBackground(elevatedBg);
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JTextField addTransparentField(JPanel panel, String labelText) {
        JPanel container = new JPanel(new BorderLayout()); container.setOpaque(false);
        JLabel label = new JLabel(labelText); label.setFont(cardTitleFont); label.setForeground(Color.GRAY);
        JTextField field = new JTextField(); field.setEditable(false); field.setBorder(null); field.setOpaque(false);
        field.setFont(bodyFont); field.setForeground(Color.BLACK);
        container.add(label, BorderLayout.NORTH); container.add(field, BorderLayout.CENTER);
        panel.add(container); return field;
    }

    class NavButton extends JButton {
        private final Color hoverBg = new Color(128, 0, 0); 
        private final Color normalText = new Color(85, 85, 85); 
        private final Color hoverText = Color.WHITE;

        public NavButton(String text, String iconPath) {
            super();
            setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Inter", Font.PLAIN, 13));
            setForeground(normalText);

            if (iconPath != null) {
                try {
                    ImageIcon rawIcon = new ImageIcon(iconPath);
                    Image img = rawIcon.getImage();
                    int nw = img.getWidth(null); int nh = img.getHeight(null);
                    double ratio = (double) nw / nh;
                    int finalW = (nw > nh) ? 18 : (int)(18 * ratio);
                    int finalH = (nw > nh) ? (int)(18 / ratio) : 18;
                    Image scaledImg = img.getScaledInstance(finalW, finalH, Image.SCALE_SMOOTH);
                    JPanel iconContainer = new JPanel(new GridBagLayout());
                    iconContainer.setOpaque(false);
                    iconContainer.setPreferredSize(new Dimension(20, 20));
                    iconContainer.add(new JLabel(new ImageIcon(scaledImg)));
                    add(iconContainer);
                } catch (Exception e) {}
            }
            JLabel textLabel = new JLabel(text);
            textLabel.setFont(getFont()); textLabel.setForeground(getForeground());
            add(textLabel);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover() || getModel().isPressed() || isFocusOwner()) {
                g2.setColor(hoverBg);
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 20, 20);
                updateChildColors(hoverText);
            } else { updateChildColors(normalText); }
            g2.dispose();
            super.paintComponent(g);
        }

        private void updateChildColors(Color color) {
            for (Component c : getComponents()) {
                if (c instanceof JLabel) c.setForeground(color);
                else if (c instanceof JPanel) {
                    for (Component sub : ((JPanel) c).getComponents()) if (sub instanceof JLabel) sub.setForeground(color);
                }
            }
        }
    }

    class RoundedImageLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int width = getWidth();
            int height = getHeight();
            int diameter = Math.min(width, height) - 4;
            int x = (width - diameter) / 2;
            int y = (height - diameter) / 2;
            java.awt.geom.Ellipse2D.Double circle = new java.awt.geom.Ellipse2D.Double(x, y, diameter, diameter);
            g2.setClip(circle);
            super.paintComponent(g2); 
            g2.setClip(null);
            g2.setColor(new Color(128, 0, 0)); 
            g2.setStroke(new BasicStroke(2.5f)); 
            g2.draw(circle);
            g2.dispose();
        }
    }


    private JTextArea createWrappedLabel(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setFont(new Font("DM Sans Regular", Font.PLAIN, 13)); // Matches your bodyFont
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setOpaque(false);
    textArea.setBackground(new Color(0, 0, 0, 0));
    return textArea;
}

private JTextArea addTransparentTextArea(JPanel panel, String labelText) {
        JPanel container = new JPanel(new BorderLayout()); 
        container.setOpaque(false);
        
        JLabel label = new JLabel(labelText); 
        label.setFont(cardTitleFont); 
        label.setForeground(Color.GRAY);
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);      // Moves text to the next line
        textArea.setWrapStyleWord(true); // Breaks lines between words
        textArea.setOpaque(false);
        textArea.setBorder(null);
        textArea.setFont(bodyFont);      // Matches your other fields
        textArea.setForeground(Color.BLACK);
        
        container.add(label, BorderLayout.NORTH);
        container.add(textArea, BorderLayout.CENTER);
        panel.add(container);
        return textArea;
    }

/* private JTextArea addTransparentTextArea(JPanel panel, String labelText) {
            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);
            
            JLabel label = new JLabel(labelText);
            label.setFont(cardTitleFont);
            label.setForeground(Color.GRAY);
            
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setOpaque(false);
            textArea.setFont(bodyFont);
            textArea.setForeground(Color.BLACK);
            // Ensure it doesn't add extra margins that break alignment
            textArea.setBorder(null); 
            
            container.add(label, BorderLayout.NORTH);
            container.add(textArea, BorderLayout.CENTER);
            panel.add(container);
            
            // Return the text area so loadPersonalDetails can set its text
            return textArea;
        } */

      private JPanel createCircularKPICard(String title, int current, int total, Color bgColor) {
        JPanel card = createStyledTile();
        card.setBackground(bgColor);
        card.setLayout(new BorderLayout(15, 0));
        card.setPreferredSize(new Dimension(280, 110));

        // Side-Panel for Text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(cardTitleFont); // DM Sans Bold 12
        lblTitle.setForeground(new Color(45, 45, 45)); // Darker gray for readability
        
        // Handle logic for displaying % for Attendance vs Numbers for others
        String unitLabel = title.equals("Attendance") ? "%" : (title.contains("Hours") ? " Hrs" : " Days");
        String displayValue = title.equals("Attendance") ? current + "%" : current + "/" + total;
        
        JLabel lblValue = new JLabel("<html><b style='font-size:18px; color:#3C0000;'>" + displayValue + "</b><br>" +
                                    "<font size='3' color='#555555'>" + unitLabel + "</font></html>");

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalGlue());

        // Side-Panel for the Circular Graph
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight()) - 25;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Track: Deep Maroon (Matches the 14/20 Days image)
                g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(60, 0, 0)); 
                g2.drawOval(x, y, size, size);
                
                // Progress: Pure White
                g2.setColor(Color.WHITE);
                double percentage = Math.min(1.0, (double) current / total);
                int angle = (int) (percentage * 360);
                g2.drawArc(x, y, size, size, 90, -angle);
                
                g2.dispose();
            }
        };
        graphPanel.setOpaque(false);
        graphPanel.setPreferredSize(new Dimension(85, 85));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(graphPanel, BorderLayout.EAST);

        return card;
    }
}