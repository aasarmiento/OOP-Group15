package util;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.ITSupportService;
import service.LeaveService;
import ui.LoginPanel;

public class ITDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao; 
    private final ITSupportService itService;
    private final LeaveService leaveService; 
    private final Employee currentUser;
    
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;
    
    private JTable userTable;
    private DefaultTableModel tableModel;

    public ITDashboard(EmployeeDAO dao, AttendanceDAO attDao, Employee user) {
        this.employeeDao = dao;
        this.attendanceDao = attDao;
        this.currentUser = user;
        this.itService = new ITSupportService(dao);
        this.leaveService = new LeaveService(dao, attDao);

        setTitle("MotorPH IT Portal - " + user.getFirstName());
        setSize(1250, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createITSystemPanel(), "IT_SYSTEM");

        add(createSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        loadEmployeeDetails();
    }

    // --- RESTORED PAYSLIP LOGIC ---

    private void showCurrentPayslip() {
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        
        if (latestData == null || latestData.getBasicSalary() == 0) {
            JOptionPane.showMessageDialog(this, "Error: Salary data could not be loaded.", "Data Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalGross = latestData.getBasicSalary() + latestData.getRiceSubsidy() + 
                            latestData.getPhoneAllowance() + latestData.getClothingAllowance();

        String payslip = String.format(
            "MOTORPH PAYSLIP\n------------------------------\n" +
            "Employee: %s %s\n" +
            "Basic Salary: %,.2f\n" +
            "Rice Subsidy: %,.2f\n" +
            "Phone Allowance: %,.2f\n" +
            "Clothing Allowance: %,.2f\n" +
            "------------------------------\n" +
            "Gross Rate: %,.2f",
            latestData.getFirstName(), latestData.getLastName(),
            latestData.getBasicSalary(), latestData.getRiceSubsidy(),
            latestData.getPhoneAllowance(), latestData.getClothingAllowance(), totalGross
        );
        
        JOptionPane.showMessageDialog(this, payslip, "My Payslip", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportPayslipToPDF() {
        String fileName = "Payslip_EMP" + currentUser.getEmpNo() + ".txt";
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        
        double totalGross = latestData.getBasicSalary() + latestData.getRiceSubsidy() + 
                            latestData.getPhoneAllowance() + latestData.getClothingAllowance();

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("========================================");
            writer.println("       MOTORPH OFFICIAL PAYSLIP         ");
            writer.println("========================================");
            writer.printf("Employee ID:    %d%n", latestData.getEmpNo());
            writer.printf("Name:           %s %s%n", latestData.getFirstName(), latestData.getLastName());
            writer.println("----------------------------------------");
            writer.printf("Basic Salary:   %,.2f%n", latestData.getBasicSalary());
            writer.printf("Rice Subsidy:   %,.2f%n", latestData.getRiceSubsidy());
            writer.printf("Phone Alw:      %,.2f%n", latestData.getPhoneAllowance());
            writer.printf("Clothing Alw:   %,.2f%n", latestData.getClothingAllowance());
            writer.println("----------------------------------------");
            writer.printf("TOTAL GROSS:    %,.2f%n", totalGross);
            writer.println("========================================");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            
            writer.flush();
            JOptionPane.showMessageDialog(this, "Payslip exported successfully to: " + fileName);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployeeDetails() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
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

    // --- OTHER UI COMPONENTS RETAINED ---

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0)); 
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        addSidebarLabel(nav, "Welcome to MOTORPH,", 12, Font.PLAIN);
        addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);
        
        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "My Profile", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time Tracking", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Leave Application", e -> cardLayout.show(cardPanel, "LEAVE_APP"));
        addNavButton(nav, "IT System Support", e -> {
            refreshTableData("ALL");
            cardLayout.show(cardPanel, "IT_SYSTEM");
        });

        nav.add(Box.createVerticalGlue());
        JButton btnLogout = new JButton("Log out");
        btnLogout.addActionListener(e -> { 
            new LoginPanel(employeeDao, attendanceDao, new dao.UserLibrary(employeeDao)).setVisible(true); 
            this.dispose(); 
        });
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));
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
        DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Log In", "Log Out"}, 0);
        JTable table = new JTable(model);

        Runnable refresh = () -> {
            model.setRowCount(0);
            Object[][] data = attendanceDao.getAttendanceByMonth(currentUser.getEmpNo(), (String) monthPicker.getSelectedItem());
            if (data != null) for (Object[] row : data) model.addRow(row);
        };

        btnIn.addActionListener(e -> { attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-in"); refresh.run(); });
        btnOut.addActionListener(e -> { attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-out"); refresh.run(); });
        monthPicker.addActionListener(e -> refresh.run());
        refresh.run();

        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }

    private JPanel createLeaveApplicationPanel() {
        JPanel leavePanel = new JPanel(new BorderLayout(20, 20));
        leavePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        Runnable refreshTable = () -> model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
        refreshTable.run();

        leavePanel.add(new JScrollPane(table), BorderLayout.CENTER);    
        return leavePanel;
    }

    private JPanel createITSystemPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JPanel(new BorderLayout()) {{ 
            add(new JScrollPane(userTable), BorderLayout.CENTER); 
        }}, BorderLayout.CENTER);
        
        JPanel south = new JPanel(new BorderLayout());
        south.add(createBottomForm(), BorderLayout.CENTER);
        south.add(createActionButtons(), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBottomForm() {
        return createGridPanel("Ticket Detail View", 2, 4);
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        JButton btnReset = new JButton("Reset Credentials");
        styleButton(btnReset, new Color(64, 64, 64));
        JButton btnResolve = new JButton("Resolve Ticket");
        styleButton(btnResolve, new Color(34, 139, 34));

        btnResolve.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtEmpNo.getText());
                itService.resolveTicket(id);
                JOptionPane.showMessageDialog(this, "Ticket Resolved! Status: ACTIVE.");
                refreshTableData("ALL");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Select an employee first.");
            }
        });

        panel.add(btnReset); panel.add(btnResolve);
        return panel;
    }

    public void refreshTableData(String filter) {
        if (tableModel == null) {
            String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
            tableModel = new DefaultTableModel(columns, 0);
            userTable = new JTable(tableModel);
        }
        tableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAll()) {
            tableModel.addRow(new Object[]{ emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getStatus(), emp.getPosition(), emp.getSupervisor() });
        }
    }

    private void updatePersonalInfo() {
        currentUser.setAddress(txtAddress.getText().trim());
        currentUser.setPhone(txtPhone.getText().trim());
        if (leaveService.updateEmployeeProfile(currentUser)) {
            JOptionPane.showMessageDialog(this, "Profile Updated!");
        }
    }

    // --- HELPER UI METHODS ---

    private JTextField addField(JPanel p, String label, boolean edit) {
        p.add(new JLabel(label));
        JTextField f = createField(edit);
        p.add(f);
        return f;
    }

    private JTextField createField(boolean editable) {
        JTextField f = new JTextField();
        f.setEditable(editable);
        if (!editable) f.setBackground(new Color(245, 245, 245));
        return f;
    }

    private JPanel createGridPanel(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(r, c, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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

    class DatePicker { 
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JLabel l = new JLabel("", JLabel.CENTER);
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(JFrame parent) {
            d = new JDialog(); d.setModal(true);
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton();
                if (x > 6) button[x].addActionListener(e -> { day = button[selection].getActionCommand(); d.dispose(); });
                p1.add(button[x]);
            }
            displayDate();
            d.add(p1, BorderLayout.CENTER);
            d.pack(); d.setLocationRelativeTo(parent); d.setVisible(true);
        }

        public void displayDate() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int days = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 7, d = 1; d <= days; x++, d++) button[x].setText("" + d);
        }

        public String setPickedDate() {
            if (day.equals("")) return "";
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, Integer.parseInt(day));
            return new java.text.SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
        }
    }
}