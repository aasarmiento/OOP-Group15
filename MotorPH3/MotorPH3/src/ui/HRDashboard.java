package ui;

import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.HRSerbisyo;
import service.LeaveStuff;


public class HRDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    private final LeaveStuff leaveService;

    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    
    private JTable empTable; // Added this to track selections
    private HRSerbisyo hrService; // Your Service Layer
    
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;
    
    private DefaultTableModel empTableModel;
    private DefaultTableModel leaveApprovalModel;
    private JTable leaveTable;

    public HRDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;
        this.leaveService = new LeaveStuff(dao);
        this.hrService = new HRSerbisyo(dao);   

        setTitle("MotorPH HR Portal - " + user.getFirstName());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createMasterlistPanel(), "MASTERLIST");
        cardPanel.add(createLeaveApprovalPanel(), "LEAVE_APPROVALS");

        add(cardPanel, BorderLayout.CENTER);

        // Load data AFTER components are initialized
        loadPersonalDetails();     
        refreshMasterlist();       
        refreshApprovalTable();
        refreshTable();
         
    }

   private JPanel createSidebar() {
    JPanel nav = new JPanel();
    nav.setBackground(new Color(128, 0, 0));
    nav.setPreferredSize(new Dimension(220, getHeight()));
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

    nav.add(Box.createVerticalStrut(30));
    addSidebarLabel(nav, "Welcome HR,", 12, Font.PLAIN);
    addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);

    nav.add(Box.createVerticalStrut(40));
    addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
    addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
    addNavButton(nav, "Apply Leave", e -> cardLayout.show(cardPanel, "LEAVE_APP"));
    
    nav.add(Box.createVerticalStrut(20));
    nav.add(new JSeparator(JSeparator.HORIZONTAL));
    nav.add(Box.createVerticalStrut(20));

    // CONNECTED: Employee Masterlist
    addNavButton(nav, "Employee Masterlist", e -> {
        refreshTable(); // This pulls fresh data from CSV via HRSerbisyo
        cardLayout.show(cardPanel, "MASTERLIST");
    });

    // CONNECTED: Leave Application (Approvals)
    addNavButton(nav, "Leave Application", e -> {
        refreshLeaveTable(); // This pulls fresh leave requests from CSV
        cardLayout.show(cardPanel, "LEAVE_APPROVALS");
    });

    nav.add(Box.createVerticalGlue());
    
    // Logout Logic
    JButton btnLogout = new JButton("Log out");
    btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnLogout.addActionListener(e -> { 
        new ui.LoginPanel().setVisible(true); // Ensure this matches your login class name
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

        // Sections (Info, Personal, Finance)
        JPanel infoPanel = new JPanel(new GridLayout(3, 4, 10, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Employee Information"));
        txtEmpNo = createField(false); txtLastName = createField(false);
        txtFirstName = createField(false); txtStatus = createField(false);
        txtPosition = createField(false); txtSupervisor = createField(false);
        infoPanel.add(new JLabel("EmployeeNo:")); infoPanel.add(txtEmpNo);
        infoPanel.add(new JLabel("LastName:")); infoPanel.add(txtLastName);
        infoPanel.add(new JLabel("FirstName:")); infoPanel.add(txtFirstName);
        infoPanel.add(new JLabel("Status:")); infoPanel.add(txtStatus);
        infoPanel.add(new JLabel("Position:")); infoPanel.add(txtPosition);
        infoPanel.add(new JLabel("Supervisor:")); infoPanel.add(txtSupervisor);

        JPanel personalPanel = new JPanel(new GridLayout(7, 2, 10, 5));
        personalPanel.setBorder(BorderFactory.createTitledBorder("Personal Information"));
        txtBirthday = createField(false); txtAddress = createField(true); 
        txtPhone = createField(true); txtSss = createField(false); 
        txtPhilHealth = createField(false); txtTin = createField(false); txtPagibig = createField(false);
        personalPanel.add(new JLabel("Birthday:")); personalPanel.add(txtBirthday);
        personalPanel.add(new JLabel("Address:")); personalPanel.add(txtAddress);
        personalPanel.add(new JLabel("Phone:")); personalPanel.add(txtPhone);
        personalPanel.add(new JLabel("SSS:")); personalPanel.add(txtSss);
        personalPanel.add(new JLabel("PhilHealth:")); personalPanel.add(txtPhilHealth);
        personalPanel.add(new JLabel("TIN:")); personalPanel.add(txtTin);
        personalPanel.add(new JLabel("Pagibig:")); personalPanel.add(txtPagibig);

        JPanel financePanel = new JPanel(new GridLayout(3, 4, 10, 5));
        financePanel.setBorder(BorderFactory.createTitledBorder("Financial Information"));
        txtSalary = createField(false); txtRice = createField(false);
        txtPhoneAllowance = createField(false); txtClothing = createField(false);
        txtGross = createField(false); txtHourly = createField(false);
        financePanel.add(new JLabel("Basic Salary:")); financePanel.add(txtSalary);
        financePanel.add(new JLabel("Rice Subsidy:")); financePanel.add(txtRice);
        financePanel.add(new JLabel("Phone Allowance:")); financePanel.add(txtPhoneAllowance);
        financePanel.add(new JLabel("Clothing Allowance:")); financePanel.add(txtClothing);
        financePanel.add(new JLabel("Gross Rate:")); financePanel.add(txtGross);
        financePanel.add(new JLabel("Hourly Rate:")); financePanel.add(txtHourly);

        // Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnUpdate = new JButton("Update Personal Info");
        styleButton(btnUpdate, new Color(34, 139, 34));
        btnUpdate.addActionListener(e -> handleUpdate());
        
        JButton btnViewPayslip = new JButton("View Current Payslip");
        styleButton(btnViewPayslip, new Color(0, 32, 77));
        btnViewPayslip.addActionListener(e -> showPayslip());

        JButton btnExportPDF = new JButton("Export to PDF/Print");
        styleButton(btnExportPDF, new Color(64, 64, 64));
        btnExportPDF.addActionListener(e -> exportPayslipToPDF());

        actionPanel.add(btnUpdate); actionPanel.add(btnViewPayslip); actionPanel.add(btnExportPDF);

        mainPanel.add(infoPanel); mainPanel.add(personalPanel); mainPanel.add(financePanel);
        mainPanel.add(Box.createVerticalStrut(15)); mainPanel.add(actionPanel);

        



        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private JPanel createTimeTrackingPanel() {
        JPanel timePanel = new JPanel(new BorderLayout(15, 15));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        String[] months = {"All", "January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthPicker = new JComboBox<>(months);
        header.add(new JLabel("View Month:")); header.add(monthPicker);

        JButton btnIn = new JButton("Check In");
        JButton btnOut = new JButton("Check Out");
        styleButton(btnIn, new Color(34, 139, 34)); 
        styleButton(btnOut, new Color(178, 34, 34)); 
        header.add(btnIn); header.add(btnOut);

        String[] columns = {"Date", "Log In", "Log Out"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        Runnable refreshTable = () -> {
            Object[][] newData = employeeDao.getAttendanceByMonth(currentUser.getEmpNo(), (String)monthPicker.getSelectedItem());
            tableModel.setRowCount(0); 
            if (newData != null) for (Object[] row : newData) tableModel.addRow(row);
        };

        btnIn.addActionListener(e -> { employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-in"); refreshTable.run(); });
        btnOut.addActionListener(e -> { employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-out"); refreshTable.run(); });
        monthPicker.addActionListener(e -> refreshTable.run());

        refreshTable.run();
        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }

    private void handleUpdate() {
        currentUser.setAddress(txtAddress.getText().trim());
        currentUser.setPhone(txtPhone.getText().trim());
        if (employeeDao.updateEmployee(currentUser)) {
            JOptionPane.showMessageDialog(this, "Update Successful!");
        }
    }

   private void showPayslip() {
    double totalGross = currentUser.getBasicSalary() + currentUser.getRiceSubsidy() + 
                        currentUser.getPhoneAllowance() + currentUser.getClothingAllowance();

    String payslipMsg = String.format(
        "MOTORPH PAYSLIP\n" +
        "-------------------------------------\n" +
        "Employee: %s %s\n" +
        "Basic Salary: %.2f\n" +
        "Rice Subsidy: %.2f\n" +
        "Phone Allowance: %.2f\n" +
        "Clothing Allowance: %.2f\n" +
        "-------------------------------------\n" +
        "Gross Rate: %.2f",
        currentUser.getFirstName(), currentUser.getLastName(),
        currentUser.getBasicSalary(), currentUser.getRiceSubsidy(), 
        currentUser.getPhoneAllowance(), currentUser.getClothingAllowance(),
        totalGross
    );

    JOptionPane.showMessageDialog(this, payslipMsg, "My Payslip", JOptionPane.INFORMATION_MESSAGE);
}

    private void exportPayslipToPDF() {
        String fileName = "HR_Payslip_EMP" + currentUser.getEmpNo() + ".txt";
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("MotorPH HR Profile Export - " + currentUser.getLastName());
            writer.println("Basic Salary: " + currentUser.getBasicSalary());
            JOptionPane.showMessageDialog(this, "Exported to " + fileName);
        } catch (FileNotFoundException ex) { ex.printStackTrace(); }
    }

  private void loadPersonalDetails() {
    // Basic Information
    txtEmpNo.setText(String.valueOf(currentUser.getEmpNo()));
    txtLastName.setText(currentUser.getLastName());
    txtFirstName.setText(currentUser.getFirstName());
    txtStatus.setText(currentUser.getStatus());
    txtPosition.setText(currentUser.getPosition());
    txtSupervisor.setText(currentUser.getSupervisor());

    // Personal Information - Fixes the LocalDate conversion error
    if (currentUser.getBirthday() != null) {
        // Convert LocalDate to String for the JTextField
        txtBirthday.setText(currentUser.getBirthday().toString()); 
    } else {
        txtBirthday.setText("");
    }

    txtAddress.setText(currentUser.getAddress());
    txtPhone.setText(currentUser.getPhone());     
    txtSss.setText(currentUser.getSss());         
    
    // Use formatLongID to prevent 9.18E+11 scientific notation
    txtPhilHealth.setText(formatLongID(currentUser.getPhilhealth())); 
    txtTin.setText(currentUser.getTin());         
    txtPagibig.setText(formatLongID(currentUser.getPagibig())); 

    // Financial Information
    txtSalary.setText(String.format("%.2f", currentUser.getBasicSalary()));
    txtRice.setText(String.format("%.2f", currentUser.getRiceSubsidy()));
    txtPhoneAllowance.setText(String.format("%.2f", currentUser.getPhoneAllowance()));
    txtClothing.setText(String.format("%.2f", currentUser.getClothingAllowance()));
    
    // Hourly Rate from CSV
    txtHourly.setText(String.format("%.2f", currentUser.getHourlyRate()));

    // Gross Rate Calculation
    double totalGross = currentUser.getBasicSalary() + 
                        currentUser.getRiceSubsidy() + 
                        currentUser.getPhoneAllowance() + 
                        currentUser.getClothingAllowance();
    txtGross.setText(String.format("%.2f", totalGross));

    
}

    private String formatLongID(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        try { return new BigDecimal(value.trim().replace("\"", "")).toPlainString(); }
        catch (Exception e) { return value; }
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(200, 40)); btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEtchedBorder());
    }

    // --- (Rest of the refreshMasterlist, refreshApproval, and createPanel methods remain the same) ---
    private void refreshMasterlist() {
        if (empTableModel == null) return;
        empTableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAll()) {
            empTableModel.addRow(new Object[]{ emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getStatus(), emp.getPosition(), emp.getSupervisor() });
        }
    }

    private void refreshApprovalTable() {
        if (leaveApprovalModel == null) return;
        leaveApprovalModel.setRowCount(0);
        Object[][] allLeaves = leaveService.getAllPendingLeaves();
        if (allLeaves != null) {
            for (Object[] row : allLeaves) {
                if (row.length >= 8 && row[7].toString().trim().equalsIgnoreCase("Pending")) {
                    leaveApprovalModel.addRow(new Object[]{row[0], row[3], row[4], row[5], row[7]});
                }
            }
        }
    }

 private JPanel createMasterlistPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // 1. Table Setup
    String[] cols = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
    empTableModel = new DefaultTableModel(cols, 0);
    empTable = new JTable(empTableModel);
    empTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    panel.add(new JScrollPane(empTable), BorderLayout.CENTER);

    // 2. Button Panel Setup
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    
    JButton btnAdd = new JButton("Create");
    JButton btnEdit = new JButton("Edit Selected");
    JButton btnDelete = new JButton("Delete");

    // Styling
    btnAdd.setBackground(new Color(34, 139, 34));      // Green
    btnAdd.setForeground(Color.BLACK);
    btnEdit.setBackground(new Color(0, 102, 204));     // Blue
    btnEdit.setForeground(Color.BLACK);
    btnDelete.setBackground(new Color(178, 34, 34));   // Red
    btnDelete.setForeground(Color.BLACK);

    // 3. ADD Action
    btnAdd.addActionListener(e -> showAddEmployeeDialog());

    // 4. EDIT Action
    btnEdit.addActionListener(e -> {
        int selectedRow = empTable.getSelectedRow();
        if (selectedRow != -1) {
            int empId = (int) empTable.getValueAt(selectedRow, 0);
            Employee empToEdit = hrService.getEmployeeById(empId);
            if (empToEdit != null) {
                showEditEmployeeDialog(empToEdit);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.");
        }
    });

    // 5. DELETE Action
    btnDelete.addActionListener(e -> {
        int selectedRow = empTable.getSelectedRow();
        if (selectedRow != -1) {
            int empId = (int) empTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete Employee #" + empId + "?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (hrService.removeEmployee(empId)) {
                    refreshTable(); 
                    JOptionPane.showMessageDialog(this, "Employee removed successfully.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
        }
    });

    // Add buttons to panel in logical order
    actionPanel.add(btnAdd);
    actionPanel.add(btnEdit);
    actionPanel.add(btnDelete);
    
    panel.add(actionPanel, BorderLayout.SOUTH);

    return panel;
}


private void showAddEmployeeDialog() {
    // Create a simple panel with input fields
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    JTextField idField = new JTextField();
    JTextField lastField = new JTextField();
    JTextField firstField = new JTextField();
    JTextField posField = new JTextField();

    panel.add(new JLabel("Employee ID:")); panel.add(idField);
    panel.add(new JLabel("Last Name:")); panel.add(lastField);
    panel.add(new JLabel("First Name:")); panel.add(firstField);
    panel.add(new JLabel("Position:")); panel.add(posField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Add New Employee",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        try {
            // 1. Create the Model using the concrete class RegularStaff
            // This fixes the "Cannot instantiate the type Employee" error
            Employee newEmp = new model.RegularStaff(); 
            
            newEmp.setEmpNo(Integer.parseInt(idField.getText().trim()));
            newEmp.setLastName(lastField.getText().trim());
            newEmp.setFirstName(firstField.getText().trim());
            newEmp.setPosition(posField.getText().trim());
            newEmp.setStatus("Regular"); // Default status

            // 2. Pass Model to Service -> DAO
            if (hrService.registerNewEmployee(newEmp)) {
                refreshTable(); // Update the UI table
                JOptionPane.showMessageDialog(this, "Employee Added Successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add employee to CSV.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter numbers only.");
        }
    }
}

private void showEditEmployeeDialog(Employee emp) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    
    // Pre-fill fields with the data from the Employee Model
    JTextField lastField = new JTextField(emp.getLastName());
    JTextField firstField = new JTextField(emp.getFirstName());
    JTextField posField = new JTextField(emp.getPosition());
    JTextField statusField = new JTextField(emp.getStatus());

    panel.add(new JLabel("Last Name:")); panel.add(lastField);
    panel.add(new JLabel("First Name:")); panel.add(firstField);
    panel.add(new JLabel("Position:")); panel.add(posField);
    panel.add(new JLabel("Status:")); panel.add(statusField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Edit Employee #" + emp.getEmpNo(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        // Update the Model object values
        emp.setLastName(lastField.getText());
        emp.setFirstName(firstField.getText());
        emp.setPosition(posField.getText());
        emp.setStatus(statusField.getText());

        // UI -> Service -> DAO
        if (hrService.updateEmployeeRecord(emp)) {
            refreshTable(); 
            JOptionPane.showMessageDialog(this, "Updated Successfully!");
        }
    }
}
private JPanel createLeaveApprovalPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // 1. Table Setup
    String[] cols = {"Emp ID", "Last Name", "First Name", "Type", "Start", "End", "Reason", "Status"};
    leaveApprovalModel = new DefaultTableModel(cols, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; 
        }
    };

    leaveTable = new JTable(leaveApprovalModel);
    panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

    // 2. Action Buttons Panel (The missing part)
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    JButton btnApprove = new JButton("Approve");
    JButton btnDecline = new JButton("Decline");

    // Styling
    btnApprove.setBackground(new Color(34, 139, 34)); // Green
    btnApprove.setForeground(Color.WHITE);
    btnDecline.setBackground(new Color(178, 34, 34)); // Red
    btnDecline.setForeground(Color.WHITE);

    // 3. Logic for Approve
    btnApprove.addActionListener(e -> handleLeaveAction("Approved"));

    // 4. Logic for Decline
    btnDecline.addActionListener(e -> handleLeaveAction("Declined"));

    actionPanel.add(btnDecline);
    actionPanel.add(btnApprove);
    
    panel.add(actionPanel, BorderLayout.SOUTH); // Add buttons to the bottom
    
    return panel;
}

private void handleLeaveAction(String newStatus) {
    int selectedRow = leaveTable.getSelectedRow();
    
    if (selectedRow != -1) {
        // 1. Pull data from the selected row
        // Columns: 0=ID, 1=Last Name, 2=First Name, 3=Type, 4=Start
        int empId = Integer.parseInt(leaveTable.getValueAt(selectedRow, 0).toString());
        String lastName = leaveTable.getValueAt(selectedRow, 1).toString();
        String startDate = leaveTable.getValueAt(selectedRow, 4).toString(); 

        // 2. Confirmation Dialog
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to set Leave for " + lastName + " to " + newStatus + "?", 
            "Confirm Action", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 3. UI -> Service -> DAO
            if (hrService.updateLeaveStatus(empId, startDate, newStatus)) {
                refreshLeaveTable(); // This refreshes the table and notification count
                JOptionPane.showMessageDialog(this, "Leave request has been " + newStatus + ".");
            } else {
                JOptionPane.showMessageDialog(this, "Error updating status in CSV.");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a leave request from the table first.");
    }
}
    

    private void refreshTable() {
    // 1. Clear the current rows in the UI table
    empTableModel.setRowCount(0);

    // 2. Ask the DAO (through the service or directly) for the latest list
    // Assuming your DAO has a getAll() method that returns List<Employee>
    java.util.List<model.Employee> latestList = employeeDao.getAll();

    // 3. Loop through the Model objects and add them back to the UI
    for (model.Employee emp : latestList) {
        empTableModel.addRow(new Object[]{
            emp.getEmpNo(),
            emp.getLastName(),
            emp.getFirstName(),
            emp.getStatus(),
            emp.getPosition(),
            emp.getSupervisor()
        });
    }
}

private void updateLeaveNotification() {
    // Get count from Service
    int pendingCount = hrService.getPendingLeaveCount();
    
    // Find the button in your sidebar to update its text
    // If you didn't save the button as a variable, you can skip the text update 
    // or just use this method to refresh a specific JLabel.
    if (pendingCount > 0) {
        System.out.println("Notification: " + pendingCount + " pending leaves.");
    }
}
private void refreshLeaveTable() {
    // 1. Clear existing rows
    leaveApprovalModel.setRowCount(0);

    // 2. Pull fresh data from Service
    Object[][] data = hrService.getAllLeaveRequestsForTable();

    // 3. Populate the Table
    for (Object[] row : data) {
        leaveApprovalModel.addRow(row);
    }
    
    // 4. Update the notification badge
    updateLeaveNotification();
}


    private JPanel createLeaveApplicationPanel() { return new JPanel(); /* Placeholder */ }
    private JTextField createField(boolean editable) { JTextField f = new JTextField(); f.setEditable(editable); if(!editable) f.setBackground(new Color(245, 245, 245)); return f; }
    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) { JButton btn = new JButton(text); btn.setMaximumSize(new Dimension(190, 40)); btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.addActionListener(al); nav.add(btn); nav.add(Box.createVerticalStrut(10)); }
    private void addSidebarLabel(JPanel nav, String text, int size, int style) { JLabel l = new JLabel(text); l.setForeground(Color.WHITE); l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT); nav.add(l); }
}