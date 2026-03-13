package util;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Deduction;
import model.Employee;
import service.LeaveService;
import service.PayrollCalculator;
import service.PayrollService;


public class AccountingDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final Employee currentUser;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final PayrollCalculator payrollCalculator;
    
    // Formatting for currency
    private final DecimalFormat df = new DecimalFormat("₱#,##0.00");

    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    // UI Components
    private JTable empTable; // Global reference for the table
    private JLabel lblMonthlyBasic, lblAllowances, lblGrossPay;
    private JLabel lblLateMinutes, lblLateDeduction, lblSSS, lblPhilHealth, lblPagIbig, lblTax, lblNetPay;
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;
    private JLabel lblProfilePic;

    public AccountingDashboard(EmployeeDAO dao, AttendanceDAO attDao, Employee user) {
        this.employeeDao = dao;
        this.attendanceDao = attDao;
        this.currentUser = user;
        this.leaveService = new LeaveService(dao, attDao);
        this.payrollService = new PayrollService(dao, attDao);
        this.payrollCalculator = new PayrollCalculator();

        setTitle("MotorPH Accounting Portal - " + user.getFirstName() + " " + user.getLastName());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add Panels
        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createFinancesPanel(), "FINANCES");

        add(createSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        loadPersonalDetails(currentUser);
    }

    private void loadPersonalDetails(Employee emp) {
        if (emp == null || txtEmpNo == null) return; 
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        
        txtEmpNo.setText(String.valueOf(emp.getEmpNo()));
        txtLastName.setText(emp.getLastName());
        txtFirstName.setText(emp.getFirstName());
        txtStatus.setText(emp.getStatus());
        txtPosition.setText(emp.getPosition());
        txtSupervisor.setText(emp.getSupervisor());
        
        if (emp.getBirthday() != null) txtBirthday.setText(emp.getBirthday().format(dateFormatter));
        txtAddress.setText(emp.getAddress());
        txtPhone.setText(emp.getPhone());
        txtSss.setText(emp.getSss());
        txtPhilHealth.setText(emp.getPhilhealth());
        txtTin.setText(emp.getTin());
        txtPagibig.setText(emp.getPagibig());

        txtSalary.setText(df.format(emp.getBasicSalary()));
        txtRice.setText(df.format(emp.getRiceSubsidy()));
        txtPhoneAllowance.setText(df.format(emp.getPhoneAllowance()));
        txtClothing.setText(df.format(emp.getClothingAllowance()));
        
        double totalAllowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        txtGross.setText(df.format(emp.getBasicSalary() + totalAllowances));
        txtHourly.setText(df.format(emp.getHourlyRate()));

        displayEmployeePhoto(lblProfilePic);
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel infoPanel = new JPanel(new BorderLayout(15, 0));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Employee Information"));

        lblProfilePic = new JLabel("No Image");
        lblProfilePic.setPreferredSize(new Dimension(150, 150));
        lblProfilePic.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblProfilePic.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel photoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoWrapper.add(lblProfilePic);
        infoPanel.add(photoWrapper, BorderLayout.WEST);

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 4, 10, 5));
        txtEmpNo = addField(fieldsPanel, "EmployeeNo:", false);
        txtLastName = addField(fieldsPanel, "LastName:", false);
        txtFirstName = addField(fieldsPanel, "FirstName:", false);
        txtStatus = addField(fieldsPanel, "Status:", false);
        txtPosition = addField(fieldsPanel, "Position:", false);
        txtSupervisor = addField(fieldsPanel, "Supervisor:", false);
        infoPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel personalPanel = new JPanel(new GridLayout(7, 2, 10, 5));
        personalPanel.setBorder(BorderFactory.createTitledBorder("Personal Information"));
        txtBirthday = addField(personalPanel, "Birthday:", false);
        txtAddress = addField(personalPanel, "Address:", true);
        txtPhone = addField(personalPanel, "Phone:", true);
        txtSss = addField(personalPanel, "SSS:", false);
        txtPhilHealth = addField(personalPanel, "PhilHealth:", false);
        txtTin = addField(personalPanel, "TIN:", false);
        txtPagibig = addField(personalPanel, "Pagibig:", false);

        JPanel financePanel = new JPanel(new GridLayout(3, 4, 10, 5));
        financePanel.setBorder(BorderFactory.createTitledBorder("Financial Information"));
        txtSalary = addField(financePanel, "Basic Salary:", false);
        txtRice = addField(financePanel, "Rice Subsidy:", false);
        txtPhoneAllowance = addField(financePanel, "Phone Allowance:", false);
        txtClothing = addField(financePanel, "Clothing Allowance:", false);
        txtGross = addField(financePanel, "Gross Rate:", false);
        txtHourly = addField(financePanel, "Hourly Rate:", false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        JButton btnUpdate = new JButton("Update Personal Info");
        btnUpdate.setBackground(new Color(34, 139, 34));
        btnUpdate.setForeground(Color.WHITE);
        actionPanel.add(btnUpdate);
        
        mainPanel.add(infoPanel); 
        mainPanel.add(personalPanel); 
        mainPanel.add(financePanel); 
        mainPanel.add(actionPanel);
        
        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private JPanel createFinancesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JComboBox<String> cbYear = new JComboBox<>(new String[]{"2024", "2025"});
        JComboBox<String> cbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        
        JButton btnCompute = new JButton("Compute Selected");
        JButton btnTimecard = new JButton("Open Timecard");
        styleButton(btnCompute, new Color(0, 102, 204));
        styleButton(btnTimecard, new Color(102, 102, 102));

        filterBar.add(new JLabel("Year:")); filterBar.add(cbYear);
        filterBar.add(new JLabel("Month:")); filterBar.add(cbMonth);
        filterBar.add(btnCompute); filterBar.add(btnTimecard);

        String[] columns = {"ID", "Last Name", "First Name", "Position", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        empTable = new JTable(tableModel); // Initialize class field
        
        for (Employee e : employeeDao.getAll()) {
            tableModel.addRow(new Object[]{e.getEmpNo(), e.getLastName(), e.getFirstName(), e.getPosition(), e.getStatus()});
        }

        JPanel payslipView = createViewPayslipPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(empTable), new JScrollPane(payslipView));
        splitPane.setDividerLocation(450);

        empTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = empTable.getSelectedRow();
                if (row != -1) {
                    int id = (int) empTable.getValueAt(row, 0);
                    Employee selected = employeeDao.findById(id);
                    if (selected != null) updatePayslipLabels(selected);
                }
            }
        });

        btnCompute.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row != -1) {
                int id = (int) empTable.getValueAt(row, 0);
                calculateAndDisplayPayroll(employeeDao.findById(id), (String) cbMonth.getSelectedItem());
            }
        });

        mainPanel.add(filterBar, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private void updatePayslipLabels(Employee emp) {
        lblMonthlyBasic.setText(df.format(emp.getBasicSalary()));
        double totalAllowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        lblAllowances.setText(df.format(totalAllowances));
        lblGrossPay.setText(df.format(emp.getBasicSalary() + totalAllowances));
        
        lblLateMinutes.setText("0");
        lblLateDeduction.setText(df.format(0));
        lblSSS.setText(df.format(0));
        lblPhilHealth.setText(df.format(0));
        lblPagIbig.setText(df.format(0));
        lblTax.setText(df.format(0));
        lblNetPay.setText("TOTAL NET PAY: " + df.format(0));
    }

    private void calculateAndDisplayPayroll(Employee emp, String month) {
        if (emp == null) return;
        Deduction d = payrollService.calculateMonthlyPayroll(emp, month);
        updatePayslipUI(emp, d, d.getLateMinutes());
    }

    private void updatePayslipUI(Employee emp, Deduction d, int lateMinutes) {
        double allowances = emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
        double totalGross = emp.getBasicSalary() + allowances;
        double netPay = totalGross - d.getTotal();

        lblMonthlyBasic.setText(df.format(emp.getBasicSalary()));
        lblAllowances.setText(df.format(allowances));
        lblGrossPay.setText(df.format(totalGross));
        
        lblLateMinutes.setText(String.valueOf(lateMinutes));
        lblLateDeduction.setText(df.format(d.getLateAmount()));
        lblSSS.setText(df.format(d.getSss()));
        lblPhilHealth.setText(df.format(d.getPhilHealth()));
        lblPagIbig.setText(df.format(d.getPagIbig()));
        lblTax.setText(df.format(d.getTax()));
        
        lblNetPay.setText("TOTAL NET PAY: " + df.format(netPay));
    }

    private JPanel createViewPayslipPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        container.add(createSectionLabel("Employee Information"));
        lblMonthlyBasic = addDetailRow(container, "Monthly Basic:");
        lblAllowances = addDetailRow(container, "Total Allowances:");
        lblGrossPay = addDetailRow(container, "Gross Pay:");
        container.add(new JSeparator());
        
        container.add(createSectionLabel("Attendance Summary"));
        lblLateMinutes = addDetailRow(container, "Late Minutes:");
        lblLateDeduction = addDetailRow(container, "Late Deduction:");
        container.add(new JSeparator());

        container.add(createSectionLabel("Deductions"));
        lblSSS = addDetailRow(container, "SSS:");
        lblPhilHealth = addDetailRow(container, "PhilHealth:");
        lblPagIbig = addDetailRow(container, "Pag-IBIG:");
        lblTax = addDetailRow(container, "Withholding Tax:");
        container.add(Box.createVerticalStrut(20));
        
        lblNetPay = new JLabel("TOTAL NET PAY: ₱0.00");
        lblNetPay.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblNetPay.setForeground(new Color(0, 128, 0));
        container.add(lblNetPay);

        return container;
    }

    private JLabel addDetailRow(JPanel panel, String labelText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.add(new JLabel(labelText), BorderLayout.WEST);
        JLabel valueLabel = new JLabel("₱0.00");
        row.add(valueLabel, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(5));
        return valueLabel;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Tahoma", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        return label;
    }

private void styleButton(JButton btn, Color bg) {
    btn.setBackground(bg);
    // If background is white/light, use black text; otherwise use white text
    btn.setForeground(bg.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
    
    btn.setOpaque(true);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    
    // Maintain a consistent height for the "MotorPH look"
    btn.setPreferredSize(new Dimension(180, 35));
}
    private JTextField addField(JPanel panel, String label, boolean editable) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField();
        field.setEditable(editable);
        panel.add(field);
        return field;
    }

    private JPanel createSidebar() {
    
    JPanel nav = new JPanel();
    nav.setBackground(new Color(128, 0, 0)); // MOTORPH MAROON
    nav.setPreferredSize(new Dimension(220, getHeight()));
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

    nav.add(Box.createVerticalStrut(30));
    // Branding Label
    addSidebarLabel(nav, "MotorPH Portal", 16, Font.BOLD);
    addSidebarLabel(nav, "ACCOUNTING", 12, Font.PLAIN); 

    nav.add(Box.createVerticalStrut(40));
    // Use the same navigation buttons
    addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
    addNavButton(nav, "Finances", e -> cardLayout.show(cardPanel, "FINANCES"));
    
    // 1. This pushes everything above this point to the top
    nav.add(Box.createVerticalGlue());

    // 2. Create the Logout Button
    JButton btnLogout = new JButton("Log out");
    btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Using your standardized branding style
    styleButton(btnLogout, Color.WHITE); 
    btnLogout.setForeground(Color.BLACK); // Make text black so it's readable on white
    
    btnLogout.addActionListener(e -> { 
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); 
            // This returns them to a new login screen
            // new LoginPanel(employeeDao, attendanceDao, new UserLibrary()).setVisible(true);
        }
    });

    // 3. Add the button and a little bit of breathing room at the very bottom
    nav.add(btnLogout);
    nav.add(Box.createVerticalStrut(20)); 

   
    return nav;
}

   


private void addSidebarLabel(JPanel nav, String text, int size, int style) {
    JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
    l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT);
    nav.add(l);
}

private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
    JButton btn = new JButton(text); 
    btn.setMaximumSize(new Dimension(180, 35));
    btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
    btn.addActionListener(al);
    nav.add(btn); nav.add(Box.createVerticalStrut(10));
}

// Dummy methods to satisfy the constructor calls
private JPanel createTimeTrackingPanel() { return new JPanel(); }
private JPanel createLeaveApplicationPanel() { return new JPanel(); }
private void displayEmployeePhoto(JLabel label) { label.setText("No Photo"); }
}