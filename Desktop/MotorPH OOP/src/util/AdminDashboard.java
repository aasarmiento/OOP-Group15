package util;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.IAdminOperations;
import model.RegularStaff;
import service.EmployeeManagementService;
import service.HRSerbisyo;
import service.LeaveService;

public class AdminDashboard extends JFrame {

    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao; 
    private final Employee currentUser;
    private final LeaveService leaveService;
    private final HRSerbisyo hrService;
    private final EmployeeManagementService employeeManagementService;

    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private JTable empTable; 
    private DefaultTableModel empTableModel;
    private JTable leaveTable;
    private DefaultTableModel leaveApprovalModel;
    private JLabel lblProfilePic; 

    private CardLayout masterlistLayout;
    private JPanel masterlistContainer;

    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    // Constructor fixed to accept AttendanceDAO to match LoginPanel call
    public AdminDashboard(EmployeeDAO dao, AttendanceDAO attDao, Employee user) {
        this.employeeDao = dao;
        this.attendanceDao = attDao;
        this.currentUser = user;
        
        
        this.leaveService = new LeaveService(dao, attDao);                                                                               
        this.hrService = new HRSerbisyo(dao);   
        this.employeeManagementService = new EmployeeManagementService(dao);

        setTitle("MotorPH Admin Portal - " + user.getFirstName() + " " + user.getLastName());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createMasterlistPanel(), "MASTERLIST");
        cardPanel.add(createLeaveApprovalPanel(), "LEAVE_APPROVALS");

        add(createSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        loadPersonalDetails(this.currentUser);     
        refreshTable();       
        refreshLeaveTable();
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
        txtSalary.setText(String.format("%.2f", emp.getBasicSalary()));
        txtRice.setText(String.format("%.2f", emp.getRiceSubsidy()));
        txtPhoneAllowance.setText(String.format("%.2f", emp.getPhoneAllowance()));
        txtClothing.setText(String.format("%.2f", emp.getClothingAllowance()));
        txtGross.setText(String.format("%.2f", emp.getGrossRate()));
        txtHourly.setText(String.format("%.2f", emp.getHourlyRate()));
        txtGross.setText(String.format("%.2f", emp.getGrossRate())); 

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

    // --- TEXT FIELDS SECTION ---
    JPanel fieldsPanel = new JPanel(new GridLayout(3, 4, 10, 5));
    txtEmpNo = addField(fieldsPanel, "EmployeeNo:", false);
    txtLastName = addField(fieldsPanel, "LastName:", false);
    txtFirstName = addField(fieldsPanel, "FirstName:", false);
    txtStatus = addField(fieldsPanel, "Status:", false);
    txtPosition = addField(fieldsPanel, "Position:", false);
    txtSupervisor = addField(fieldsPanel, "Supervisor:", false);
    
    infoPanel.add(fieldsPanel, BorderLayout.CENTER);

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
    btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update functionality triggered."));

    JButton btnViewPayslip = new JButton("View Current Payslip");
    styleButton(btnViewPayslip, new Color(0, 32, 77));
    btnViewPayslip.addActionListener(e -> showCurrentPayslip());

    JButton btnExport = new JButton("Export to PDF/Print");
    styleButton(btnExport, new Color(64, 64, 64));
    btnExport.addActionListener(e -> exportPayslipToPDF());

    actionPanel.add(btnUpdate); actionPanel.add(btnViewPayslip); actionPanel.add(btnExport);
    
    mainPanel.add(infoPanel); 
    mainPanel.add(personalPanel); 
    mainPanel.add(financePanel); 
    mainPanel.add(actionPanel);
    
    return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
}

    private void handleLeaveAction(String newStatus) {
    int selectedRow = leaveTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a leave request from the table.");
        return;
    }

    String requestId = leaveTable.getValueAt(selectedRow, 0).toString();
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Are you sure you want to set this request to " + newStatus + "?", 
        "Confirm Action", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        // Call service to update CSV
        boolean success = hrService.updateLeaveStatus(requestId, newStatus);
        if (success) {
            refreshLeaveTable(); // Instantly removes it from the "Pending" list
            JOptionPane.showMessageDialog(this, "Request marked as " + newStatus);
        }
    }
}

    private void refreshLeaveTable() {
        if (leaveApprovalModel == null) return;
        leaveApprovalModel.setRowCount(0);
        Object[][] data = hrService.getAllLeaveRequestsForTable(); 
        if (data != null) {
            for (Object[] row : data) leaveApprovalModel.addRow(row);
        }
    }

    private JPanel createLeaveApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        String[] columns = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start", "End", "Reason", "Status"};
        leaveApprovalModel = new DefaultTableModel(columns, 0);
        leaveTable = new JTable(leaveApprovalModel);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnDecline = new JButton("Decline"); 
        styleButton(btnDecline, new Color(153, 0, 0));
        btnDecline.addActionListener(e -> handleLeaveAction("Rejected"));
        
        JButton btnApprove = new JButton("Approve"); 
        styleButton(btnApprove, new Color(34, 139, 34));
        btnApprove.addActionListener(e -> handleLeaveAction("Approved"));
        
        actionPanel.add(btnDecline); actionPanel.add(btnApprove);
        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshTable() {
        List<Employee> updatedList = employeeManagementService.getAll();
        if (empTableModel != null) {
            empTableModel.setRowCount(0);
            for (Employee emp : updatedList) {
                String cleanSupervisor = (emp.getSupervisor() != null) ? emp.getSupervisor().replace("\"", "") : "N/A";
                empTableModel.addRow(new Object[]{ 
                    emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getStatus(), emp.getPosition(), cleanSupervisor
                });
            }
        }
    }

    private void displayEmployeePhoto(JLabel lblPhoto) {
    // 1. Get the path stored in the employee object
    String path = currentUser.getPhotoPath();
    
    // 2. Check if the file exists, otherwise use the default string path
    File imgFile = (path != null) ? new File(path) : null;
    
    if (imgFile == null || !imgFile.exists()) {
        // MUST BE IN QUOTES to avoid the "Syntax error on token default"
        path = "resources/profile_pics/default.png"; 
    }

    try {
        ImageIcon icon = new ImageIcon(path);
        // Use 150x150 if the label hasn't rendered a size yet
        int w = lblPhoto.getWidth() > 0 ? lblPhoto.getWidth() : 150;
        int h = lblPhoto.getHeight() > 0 ? lblPhoto.getHeight() : 150;

        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        lblPhoto.setIcon(new ImageIcon(img));
        lblPhoto.setText(""); 
    } catch (Exception e) {
        lblPhoto.setText("No Image");
        System.err.println("Error loading photo: " + e.getMessage());
    }
}

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0));
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.add(Box.createVerticalStrut(30));
       addSidebarLabel(nav, "Welcome " + currentUser.getRole() + ",", 12, Font.PLAIN);
    addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);
        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Apply Leave", e -> cardLayout.show(cardPanel, "LEAVE_APP"));
        nav.add(Box.createVerticalStrut(20));
        nav.add(new JSeparator(JSeparator.HORIZONTAL));
        nav.add(Box.createVerticalStrut(20));
        addNavButton(nav, "Employee Database", e -> { refreshTable(); cardLayout.show(cardPanel, "MASTERLIST"); });
        addNavButton(nav, "Leave Approval", e -> { refreshLeaveTable(); cardLayout.show(cardPanel, "LEAVE_APPROVALS"); });
        nav.add(Box.createVerticalGlue());
        JButton btnLogout = new JButton("Log out");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(btnLogout, Color.WHITE);
        btnLogout.setForeground(Color.BLACK);
        btnLogout.addActionListener(e -> { if (JOptionPane.showConfirmDialog(this, "Log out?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) this.dispose(); });
        nav.add(btnLogout); nav.add(Box.createVerticalStrut(20));
        return nav;
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
        
        // Use attendanceDao to fetch logs
        Object[][] data = attendanceDao.getAttendanceByMonth(currentUser.getEmpNo(), selectedMonth);
        
        boolean alreadyCheckedInToday = false;
        boolean alreadyCheckedOutToday = false;
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        if (data != null) {
            for (Object[] row : data) {
                model.addRow(row);
                
                // Logic to check today's status for grey-out effect
                if (row[0] != null && row[0].toString().equals(todayStr)) {
                    if (row[1] != null && !row[1].toString().isEmpty()) alreadyCheckedInToday = true;
                    if (row[2] != null && !row[2].toString().isEmpty()) alreadyCheckedOutToday = true;
                }
            }
        }

        // Grey out logic: 
        // If not checked in yet -> Enable In, Disable Out
        // If checked in but not out -> Disable In, Enable Out
        // If both done -> Disable both
        btnIn.setEnabled(!alreadyCheckedInToday);
        btnOut.setEnabled(alreadyCheckedInToday && !alreadyCheckedOutToday);
    };

    btnIn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to Check In?", "Confirm Check In", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-in");
            refresh.run();
        }
    });

    btnOut.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to Check Out?", "Confirm Check Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            attendanceDao.recordAttendance(currentUser.getEmpNo(), "Check-out");
            refresh.run(); 
        }
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

    // Form Panel Setup (UI remains exactly as you provided)
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
    txtReason.setLineWrap(true);
    txtReason.setWrapStyleWord(true);
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

    // --- UPDATED LOGIC FOR 9 COLUMNS ---
    // We must define all 9 columns so the table doesn't crash reading the 9-column CSV
    String[] cols = {"Leave ID", "Emp ID", "Last Name", "First Name", "Type", "Start Date", "End Date", "Reason", "Status"};
    
    // Status Renderer - Updated to index 8 (the last column in the 9-column set)
    javax.swing.table.DefaultTableCellRenderer statusRenderer = new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (value != null) ? value.toString().trim() : "";
            c.setFont(c.getFont().deriveFont(java.awt.Font.BOLD));
            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            if (status.equalsIgnoreCase("APPROVED")) c.setForeground(new java.awt.Color(0, 128, 0));
            else if (status.equalsIgnoreCase("DECLINED") || status.equalsIgnoreCase("REJECTED")) c.setForeground(java.awt.Color.RED);
            else c.setForeground(java.awt.Color.BLUE);
            return c;
        }
    };

    DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    
    JTable table = new JTable(model);
    table.setRowHeight(25);
    


    
    Runnable refreshTable = () -> {
    Object[][] data = leaveService.getLeaveHistory(currentUser.getEmpNo());
    
    
    System.out.println("UI Loaded " + data.length + " leave records.");
    
    model.setDataVector(data, cols);
    
    table.getColumnModel().getColumn(8).setCellRenderer(statusRenderer);
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            leaveService.submitLeave(
                currentUser.getEmpNo(), 
                (String) comboType.getSelectedItem(), 
                LocalDate.parse(txtStart.getText(), formatter), 
                LocalDate.parse(txtEnd.getText(), formatter), 
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


    private void showCurrentPayslip() {
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        if (latestData == null) return;
        JOptionPane.showMessageDialog(this, "Payslip for " + latestData.getFirstName() + " " + latestData.getLastName() + "\nBasic Salary: " + latestData.getBasicSalary());
    }

    private void exportPayslipToPDF() {
        JOptionPane.showMessageDialog(this, "Payslip exported to text file.");
    }

    private JPanel createMasterlistPanel() {
    masterlistLayout = new CardLayout();
    masterlistContainer = new JPanel(masterlistLayout);
    JPanel tablePanel = new JPanel(new BorderLayout());
    String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
    empTableModel = new DefaultTableModel(columns, 0);
    empTable = new JTable(empTableModel);
    
    JButton btnAddNew = new JButton("Add");
    JButton btnView = new JButton("View Details");
    JButton btnDelete = new JButton("Delete");
    
    styleButton(btnAddNew, new Color(0, 82, 204));
    styleButton(btnView, new Color(70, 130, 180));
    styleButton(btnDelete, new Color(255, 0, 0));

    btnAddNew.addActionListener(e -> masterlistLayout.show(masterlistContainer, "FORM"));

   
    btnView.addActionListener(e -> {
        int row = empTable.getSelectedRow();
        if (row != -1) {
            try {
                // 1. Get ID from the first column of the selected row
                int empId = Integer.parseInt(empTable.getValueAt(row, 0).toString());
                
                // 2. Call the method you moved to employeeManagementService
                Object[] details = employeeManagementService.getEmployeeDetailsForForm(empId);
                
                // 3. Open the form if data was found
                if (details != null) {
                    new EmployeeDetailForm(details);
                } else {
                    JOptionPane.showMessageDialog(this, "Employee data not found.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading details: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee first.");
        }
    });
  
    btnDelete.addActionListener(e -> {
        int row = empTable.getSelectedRow();
        if (row != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int empId = Integer.parseInt(empTable.getValueAt(row, 0).toString());
                boolean success = employeeManagementService.removeEmployee((IAdminOperations)currentUser, empId);
                if (success) { refreshTable(); JOptionPane.showMessageDialog(this, "Deleted."); }
                else { JOptionPane.showMessageDialog(this, "Action Denied (cannot delete CEO)."); }
            }
        }
    });

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    btnPanel.add(btnAddNew); 
    btnPanel.add(btnView);  
    
    tablePanel.add(new JScrollPane(empTable), BorderLayout.CENTER);
    tablePanel.add(btnPanel, BorderLayout.SOUTH);

    masterlistContainer.add(tablePanel, "TABLE");
    masterlistContainer.add(createNewHireFormPanel(), "FORM");
    
    return masterlistContainer;
}



private void setupPlaceholder(JTextField field, String hint) {
    field.setText(hint);
    field.setForeground(Color.GRAY);

    field.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
            if (field.getText().equals(hint)) {
                field.setText("");
                field.setForeground(Color.BLACK);
            }
        }
        @Override
        public void focusLost(java.awt.event.FocusEvent e) {
            if (field.getText().isEmpty()) {
                field.setForeground(Color.GRAY);
                field.setText(hint);
            }
        }
    });
}


private String getActualValue(JTextField field, String hint) {
    String val = field.getText().trim();
    return val.equals(hint) ? "" : val;
}

  private JPanel createNewHireFormPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    
    JPanel formContainer = new JPanel();
    formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

    
    JPanel personalPanel = createFormSection("Personal Information", 0, 2);
    JTextField fName = new JTextField(); 
    JTextField lName = new JTextField();
    JTextField bday = new JTextField("MM/dd/yyyy");
    JButton btnPickBday = new JButton("📅 Pick Date");
    btnPickBday.addActionListener(e -> bday.setText(new DatePicker(this).setPickedDate()));
    JTextField address = new JTextField();
    JTextField phone = new JTextField();
    
    
    ((javax.swing.text.AbstractDocument) phone.getDocument()).setDocumentFilter(new util.MaskFormatterFilter("###-###-###"));
    
    
    setupPlaceholder(phone, "000-000-000");

    JComboBox<String> gender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    addFormField(personalPanel, "<html>First Name <font color='red'>*</font></html>", fName);
    addFormField(personalPanel, "<html>Last Name <font color='red'>*</font></html>", lName);
    personalPanel.add(new JLabel("<html>Birthday <font color='red'>*</font></html>")); 
    JPanel bw = new JPanel(new BorderLayout()); bw.add(bday); bw.add(btnPickBday, BorderLayout.EAST);
    personalPanel.add(bw);
    addFormField(personalPanel, "Gender:", gender);
    addFormField(personalPanel, "Address:", address);
    addFormField(personalPanel, "Phone Number:", phone);

   
    JPanel govPanel = createFormSection("Identification & Status", 0, 2);
    JTextField sss = new JTextField(); 
    JTextField phil = new JTextField();
    JTextField tin = new JTextField(); 
    JTextField pagibig = new JTextField();

  
    ((javax.swing.text.AbstractDocument) sss.getDocument()).setDocumentFilter(new util.MaskFormatterFilter("##-#######-#"));
    ((javax.swing.text.AbstractDocument) tin.getDocument()).setDocumentFilter(new util.MaskFormatterFilter("###-###-###-###"));
    
    
    ((javax.swing.text.AbstractDocument) phil.getDocument()).setDocumentFilter(new util.NumericLimitFilter(12));
    ((javax.swing.text.AbstractDocument) pagibig.getDocument()).setDocumentFilter(new util.NumericLimitFilter(12));

    
    setupPlaceholder(sss, "00-0000000-0");
    setupPlaceholder(phil, "000000000000"); // 12 digits numeric
    setupPlaceholder(tin, "000-000-000-000");
    setupPlaceholder(pagibig, "000000000000"); // 12 digits numeric

    JComboBox<String> status = new JComboBox<>(new String[]{"Regular", "Probationary"});
    addFormField(govPanel, "SSS #:", sss);
    addFormField(govPanel, "Philhealth #:", phil);
    addFormField(govPanel, "TIN #:", tin);
    addFormField(govPanel, "Pag-ibig #:", pagibig);
    addFormField(govPanel, "Status:", status);

    // --- Employment & Finance (Unchanged) ---
    JPanel jobPanel = createFormSection("Employment", 0, 2);
    JTextField pos = new JTextField();
    JComboBox<String> superv = new JComboBox<>(new String[]{"N/A", "Garcia, Manuel III", "Lim, Antonio", "Villanueva, Andrea"}); 
    addFormField(jobPanel, "Position:", pos);
    addFormField(jobPanel, "Supervisor:", superv);

    JPanel financePanel = createFormSection("Financial Information", 0, 4);
    JTextField salary = new JTextField("0.00");
    JTextField rice = new JTextField("0.00");
    JTextField pallow = new JTextField("0.00");
    JTextField cloth = new JTextField("0.00");
    JTextField hourly = new JTextField("0.00");
    addFormField(financePanel, "Salary:", salary);
    addFormField(financePanel, "Rice:", rice);
    addFormField(financePanel, "Phone:", pallow);
    addFormField(financePanel, "Clothing:", cloth);
    addFormField(financePanel, "Hourly:", hourly);

    formContainer.add(personalPanel); formContainer.add(govPanel); formContainer.add(jobPanel); formContainer.add(financePanel);

    JButton btnSave = new JButton("Confirm Hire");
    styleButton(btnSave, new Color(34, 139, 34));
    
    btnSave.addActionListener(e -> {
    try {
        Employee newEmp = new RegularStaff(); 
        newEmp.setFirstName(fName.getText().trim());
        newEmp.setLastName(lName.getText().trim());
        newEmp.setAddress(address.getText().trim());
        newEmp.setPhone(getActualValue(phone, "000-000-000"));
        newEmp.setSss(getActualValue(sss, "00-0000000-0"));
        newEmp.setTin(getActualValue(tin, "000-000-000-000"));
        newEmp.setPhilhealth(getActualValue(phil, "000000000000"));
        newEmp.setPagibig(getActualValue(pagibig, "000000000000"));
        newEmp.setStatus((String)status.getSelectedItem());
        newEmp.setPosition(pos.getText().trim());
        newEmp.setSupervisor((String)superv.getSelectedItem());
        
        // Financials (Model)
        newEmp.setBasicSalary(Double.parseDouble(salary.getText()));
        newEmp.setRiceSubsidy(Double.parseDouble(rice.getText()));
        newEmp.setPhoneAllowance(Double.parseDouble(pallow.getText()));
        newEmp.setClothingAllowance(Double.parseDouble(cloth.getText()));

        
        boolean success = employeeManagementService.registerEmployee((IAdminOperations)currentUser, newEmp);

        if (success) {
            JOptionPane.showMessageDialog(this, "Employee Registered Successfully!");
            refreshTable(); 
            masterlistLayout.show(masterlistContainer, "TABLE"); // Navigate back
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Validation Error: " + ex.getMessage());
    }
});

    JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnBack = new JButton("Cancel"); 
    btnBack.addActionListener(e -> masterlistLayout.show(masterlistContainer, "TABLE"));
    bp.add(btnBack); bp.add(btnSave);
    
    mainPanel.add(new JScrollPane(formContainer), BorderLayout.CENTER);
    mainPanel.add(bp, BorderLayout.SOUTH);
    return mainPanel;
}
    private double parseInput(String val) {
        try { return Double.parseDouble(val.trim().replace(",", "")); } catch (Exception e) { return 0.0; }
    }
    private JTextField addField(JPanel p, String label, boolean edit) {
        p.add(new JLabel(label)); JTextField f = createField(edit); p.add(f); return f;
    }
    private void addFormField(JPanel p, String label, JComponent f) {
        p.add(new JLabel(label)); p.add(f);
    }
    private JPanel createGridPanel(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(r, c, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder(title)); return p;
    }
    private JTextField createField(boolean editable) {
        JTextField f = new JTextField(); f.setEditable(editable);
        if(!editable) f.setBackground(new Color(245, 245, 245));
        return f;
    }
    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text); btn.setMaximumSize(new Dimension(200, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.addActionListener(al);
        nav.add(btn); nav.add(Box.createVerticalStrut(10));
    }
    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(l);
    }
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(bg.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        btn.setOpaque(true); btn.setBorderPainted(false); btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }
    private JPanel createFormSection(String title, int rows, int cols) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14)));
        return p;
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
            String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            p1.setPreferredSize(new Dimension(430, 120));
            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton(); button[x].setFocusPainted(false);
                button[x].setBackground(Color.WHITE);
                if (x > 6) { button[x].addActionListener(e -> { day = button[selection].getActionCommand(); d.dispose(); }); }
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
            for (int x = 6 + dayOfWeek, d = 1; d <= daysInMonth; x++, d++) button[x].setText("" + d);
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