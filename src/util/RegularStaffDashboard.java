package util;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.LeaveService;

public class RegularStaffDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final Employee currentUser;
    private final LeaveService leaveService; 
    
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // UI Fields
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    public RegularStaffDashboard(EmployeeDAO dao, AttendanceDAO attDao, Employee user) {
        this.employeeDao = dao;
        this.attendanceDao = attDao;
        this.currentUser = user;
        // Updated service initialization to match new requirements
        this.leaveService = new LeaveService(dao, attDao);

        setTitle("MotorPH Portal - " + user.getFirstName());
        setSize(1300, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE");

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(createTopHeader(), BorderLayout.NORTH); 
        mainContent.add(cardPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
        loadEmployeeDetails();
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel lblBreadcrumb = new JLabel("Link / Page");
        lblBreadcrumb.setForeground(new Color(140, 40, 40)); 
        titlePanel.add(lblTitle);
        titlePanel.add(lblBreadcrumb);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightSide.setOpaque(false);
        
        // Profiles and buttons logic remains visual-only per your UI
        rightSide.add(new JLabel(currentUser.getFirstName() + " " + currentUser.getLastName()));
        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightSide, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0)); 
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        addSidebarLabel(nav, "Welcome to MOTORPH,", 12, Font.PLAIN);
        addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);
        
        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Leave Application", e -> cardLayout.show(cardPanel, "LEAVE"));

        nav.add(Box.createVerticalGlue());
        
        JButton btnLogout = new JButton("Log out");
        btnLogout.addActionListener(e -> { 
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { this.dispose(); }
        });
        nav.add(btnLogout);
        return nav;
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel infoPanel = createGridPanel("Employee Information", 3, 4);
        txtEmpNo = addField(infoPanel, "EmployeeNo:", false);
        txtLastName = addField(infoPanel, "LastName:", false);
        txtFirstName = addField(infoPanel, "FirstName:", false);
        txtStatus = addField(infoPanel, "Status:", false);
        txtPosition = addField(infoPanel, "Position:", false);
        txtSupervisor = addField(infoPanel, "Supervisor:", false);

        JPanel personalPanel = createGridPanel("Personal Information", 7, 2);
        txtBirthday = addField(personalPanel, "Birthday:", false);
        txtAddress = addField(personalPanel, "Address:", true);
        txtPhone = addField(personalPanel, "Phone:", true);
        txtSss = addField(personalPanel, "SSS:", false);
        txtPhilHealth = addField(personalPanel, "PhilHealth:", false);
        txtTin = addField(personalPanel, "TIN:", false);
        txtPagibig = addField(personalPanel, "Pagibig:", false);

        JPanel financePanel = createGridPanel("Financial Information", 3, 4);
        txtSalary = addField(financePanel, "Basic Salary:", false);
        txtRice = addField(financePanel, "Rice Subsidy:", false);
        txtPhoneAllowance = addField(financePanel, "Phone Allowance:", false);
        txtClothing = addField(financePanel, "Clothing Allowance:", false);
        txtGross = addField(financePanel, "Gross Rate:", false);
        txtHourly = addField(financePanel, "Hourly Rate:", false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        JButton btnUpdate = new JButton("Update Personal Info");
        styleButton(btnUpdate, new Color(34, 139, 34));
        btnUpdate.addActionListener(e -> updatePersonalInfo());

        JButton btnViewPayslip = new JButton("View Current Payslip");
        styleButton(btnViewPayslip, new Color(0, 32, 77));
        btnViewPayslip.addActionListener(e -> showCurrentPayslip());

        JButton btnExport = new JButton("Export to PDF/Print");
        styleButton(btnExport, new Color(64, 64, 64));
        btnExport.addActionListener(e -> exportPayslipToPDF());

        actionPanel.add(btnUpdate); actionPanel.add(btnViewPayslip); actionPanel.add(btnExport);

        mainPanel.add(infoPanel); mainPanel.add(personalPanel); mainPanel.add(financePanel); mainPanel.add(actionPanel);
        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private JPanel createTimeTrackingPanel() {
        JPanel timePanel = new JPanel(new BorderLayout(15, 15));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        String[] months = {"All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthPicker = new JComboBox<>(months);
        header.add(new JLabel("View Month:"));
        header.add(monthPicker);

        JButton btnIn = new JButton("Check In");
        JButton btnOut = new JButton("Check Out");
        styleButton(btnIn, new Color(34, 139, 34)); 
        styleButton(btnOut, new Color(178, 34, 34)); 

        header.add(btnIn); header.add(btnOut);

        String[] columns = {"Date", "Log In", "Log Out"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        Runnable refresh = () -> {
            model.setRowCount(0);
            String selectedMonth = (String) monthPicker.getSelectedItem();
            // LOGIC FIX: Use the dedicated attendanceDao
            Object[][] data = attendanceDao.getAttendanceByMonth(currentUser.getEmpNo(), selectedMonth);
            if (data != null) {
                for (Object[] row : data) model.addRow(row);
            }
        };

        btnIn.addActionListener(e -> {
            attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-in");
            refresh.run();
        });

        btnOut.addActionListener(e -> {
            attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-out");
            refresh.run();
        });

        monthPicker.addActionListener(e -> refresh.run());
        refresh.run();

        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }

    private JPanel createLeaveApplicationPanel() {
        JPanel leavePanel = new JPanel(new BorderLayout(20, 20));
        leavePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Request New Leave"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
        JComboBox<String> comboType = new JComboBox<>(leaveTypes);
        JTextField txtStart = createField(false);
        JTextField txtEnd = createField(false);
        JTextArea txtReason = new JTextArea(4, 25);
        txtReason.setLineWrap(true); txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnPickStart = new JButton("📅 Start Date");
        JButton btnPickEnd = new JButton("📅 End Date");
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(0, 51, 102));

        gbc.gridy = 0; form.add(new JLabel("Type:"), gbc);
        gbc.gridy = 1; form.add(comboType, gbc);
        gbc.gridy = 2; form.add(new JLabel("Start Date:"), gbc);
        gbc.gridy = 3; form.add(txtStart, gbc);
        gbc.gridy = 4; form.add(btnPickStart, gbc);
        gbc.gridy = 5; form.add(new JLabel("End Date:"), gbc);
        gbc.gridy = 6; form.add(txtEnd, gbc);
        gbc.gridy = 7; form.add(btnPickEnd, gbc);
        gbc.gridy = 8; form.add(new JLabel("Reason:"), gbc);
        gbc.gridy = 9; form.add(new JScrollPane(txtReason), gbc);
        gbc.gridy = 10; form.add(btnSubmit, gbc);

        // LOGIC FIX: Map to the 9-column CSV structure
        String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);

        Runnable refreshTable = () -> {
            Object[][] data = leaveService.getLeaveHistory(currentUser.getEmpNo());
            model.setDataVector(data, cols);
        };

        refreshTable.run();

        btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
        btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));

        btnSubmit.addActionListener(e -> {
            if (txtStart.getText().isEmpty() || txtEnd.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select both Start and End dates.");
                return;
            }
            try {
                leaveService.submitLeave(
                    currentUser.getEmpNo(), 
                    (String) comboType.getSelectedItem(), 
                    LocalDate.parse(txtStart.getText(), dateFormatter), 
                    LocalDate.parse(txtEnd.getText(), dateFormatter), 
                    txtReason.getText()
                );
                refreshTable.run(); 
                JOptionPane.showMessageDialog(this, "Leave application submitted successfully!");
                txtStart.setText(""); txtEnd.setText(""); txtReason.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        leavePanel.add(form, BorderLayout.WEST);
        leavePanel.add(new JScrollPane(table), BorderLayout.CENTER);    
        return leavePanel;
    }

    private void updatePersonalInfo() {
        currentUser.setAddress(txtAddress.getText().trim());
        currentUser.setPhone(txtPhone.getText().trim());
        if (leaveService.updateEmployeeProfile(currentUser)) {
            JOptionPane.showMessageDialog(this, "Profile Updated!");
        }
    }

    private void showCurrentPayslip() {
        // LOGIC FIX: Re-fetch current data from DAO for real-time accuracy
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        if (latestData == null) return;

        double totalGross = latestData.getBasicSalary() + latestData.getRiceSubsidy() + 
                            latestData.getPhoneAllowance() + latestData.getClothingAllowance();

        String payslip = String.format(
            "MOTORPH PAYSLIP\n------------------------------\n" +
            "Employee: %s %s\n" +
            "Basic Salary: %,.2f\n" +
            "Total Gross: %,.2f",
            latestData.getFirstName(), latestData.getLastName(),
            latestData.getBasicSalary(), totalGross
        );
        JOptionPane.showMessageDialog(this, payslip, "My Payslip", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportPayslipToPDF() {
        String fileName = "Payslip_EMP" + currentUser.getEmpNo() + ".txt";
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("MOTORPH OFFICIAL PAYSLIP");
            writer.println("Employee ID: " + latestData.getEmpNo());
            writer.println("Name: " + latestData.getFirstName() + " " + latestData.getLastName());
            writer.printf("Basic Salary: %,.2f%n", latestData.getBasicSalary());
            JOptionPane.showMessageDialog(this, "Exported to: " + fileName);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Export failed.");
        }
    }

    private void loadEmployeeDetails() {
        txtEmpNo.setText(String.valueOf(currentUser.getEmpNo()));
        txtLastName.setText(currentUser.getLastName());
        txtFirstName.setText(currentUser.getFirstName());
        txtStatus.setText(currentUser.getStatus());
        txtPosition.setText(currentUser.getPosition());
        txtSupervisor.setText(currentUser.getSupervisor());
        if (currentUser.getBirthday() != null) txtBirthday.setText(currentUser.getBirthday().format(dateFormatter));
        txtAddress.setText(currentUser.getAddress());
        txtPhone.setText(currentUser.getPhone());
        txtSss.setText(currentUser.getSss());
        txtPhilHealth.setText(currentUser.getPhilhealth());
        txtTin.setText(currentUser.getTin());
        txtPagibig.setText(currentUser.getPagibig());
        txtSalary.setText(String.format("%.2f", currentUser.getBasicSalary()));
        txtRice.setText(String.format("%.2f", currentUser.getRiceSubsidy()));
        txtPhoneAllowance.setText(String.format("%.2f", currentUser.getPhoneAllowance()));
        txtClothing.setText(String.format("%.2f", currentUser.getClothingAllowance()));
        txtGross.setText(String.format("%.2f", currentUser.getGrossRate()));
        txtHourly.setText(String.format("%.2f", currentUser.getHourlyRate()));
    }

    private JTextField addField(JPanel p, String label, boolean edit) {
        p.add(new JLabel(label));
        JTextField f = createField(edit);
        p.add(f);
        return f;
    }

    private JPanel createGridPanel(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(r, c, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private JTextField createField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        if (!editable) field.setBackground(new Color(245, 245, 245));
        return field;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(190, 35));
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }

    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        nav.add(btn); nav.add(Box.createVerticalStrut(10));
    }

    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(l);
    }

    // Inner class DatePicker logic (as per your original code)
    class DatePicker { 
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JLabel l = new JLabel("", JLabel.CENTER);
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(JFrame parent) {
            d = new JDialog(); d.setModal(true);
            String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            p1.setPreferredSize(new Dimension(430, 120));

            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton(); button[x].setFocusPainted(false);
                button[x].setBackground(Color.WHITE);
                if (x > 6) {
                    button[x].addActionListener(e -> {
                        day = button[selection].getActionCommand();
                        d.dispose();
                    });
                }
                if (x < 7) { button[x].setText(header[x]); button[x].setForeground(Color.RED); }
                p1.add(button[x]);
            }
            JPanel p2 = new JPanel(new GridLayout(1, 3));
            JButton previous = new JButton("<< Previous");
            previous.addActionListener(e -> { month--; displayDate(); });
            p2.add(previous); p2.add(l);
            JButton next = new JButton("Next >>");
            next.addActionListener(e -> { month++; displayDate(); });
            p2.add(next);
            d.add(p1, BorderLayout.CENTER); d.add(p2, BorderLayout.SOUTH);
            d.pack(); d.setLocationRelativeTo(parent);
            displayDate(); d.setVisible(true);
        }

        public final void displayDate() {
            for (int x = 7; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 6 + dayOfWeek, d = 1; d <= daysInMonth; x++, d++)
                button[x].setText("" + d);
            l.setText(new java.text.SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
        }

        public String setPickedDate() {
            if (day.equals("")) return "";
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, Integer.parseInt(day));
            return new java.text.SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
        }
    }
}