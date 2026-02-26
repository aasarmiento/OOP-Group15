package ui;

import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import model.RegularStaff;
import service.LeaveStuff;

public class RegularStaffDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private Employee currentUser;
    private final LeaveStuff leaveService; 
    
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private Employee employeeBackup;
    private JButton btnUndo; // Moved to class level to fix "cannot be resolved"

    // UI Fields
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    public RegularStaffDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;
        this.leaveService = new LeaveStuff(dao);

        setTitle("MotorPH Portal - " + user.getFirstName());
        setSize(1300, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE");

        add(cardPanel, BorderLayout.CENTER);
        loadEmployeeDetails();
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
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> { 
            int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to log out?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                new LoginPanel().setVisible(true); 
                this.dispose(); 
            }
        });

        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));
        return nav;
    }

    private void showCurrentPayslip() {
        Employee latestData = employeeDao.findById(currentUser.getEmpNo());
        double totalGross = latestData.getBasicSalary() + latestData.getRiceSubsidy() + 
                            latestData.getPhoneAllowance() + latestData.getClothingAllowance();
        
        String payslip = String.format(
            "MOTORPH PAYSLIP\n------------------------------\n" +
            "Employee: %s %s\nBasic Salary: %.2f\nRice Subsidy: %.2f\n" +
            "Phone Allowance: %.2f\nClothing Allowance: %.2f\n" +
            "------------------------------\nGross Rate: %.2f",
            latestData.getFirstName(), latestData.getLastName(),
            latestData.getBasicSalary(), latestData.getRiceSubsidy(),
            latestData.getPhoneAllowance(), latestData.getClothingAllowance(), totalGross
        );
        JOptionPane.showMessageDialog(this, payslip, "My Payslip", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportPayslipToPDF() {
        String fileName = "Payslip_EMP" + currentUser.getEmpNo() + ".txt";
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("MOTORPH OFFICIAL PAYSLIP");
            writer.println("Employee ID: " + currentUser.getEmpNo());
            writer.println("Name: " + currentUser.getFirstName() + " " + currentUser.getLastName());
            writer.println("Basic Salary: " + currentUser.getBasicSalary());
            writer.println("Gross Rate: " + currentUser.getGrossRate());
            writer.flush();
            JOptionPane.showMessageDialog(this, "Exported to " + fileName);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTimeTrackingPanel() {
        JPanel timePanel = new JPanel(new BorderLayout(15, 15));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        String[] months = {"All", "January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthPicker = new JComboBox<>(months);
        header.add(new JLabel("View Month:"));
        header.add(monthPicker);

        JButton btnIn = new JButton("Check In");
        JButton btnOut = new JButton("Check Out");
        styleButton(btnIn, new Color(34, 139, 34)); 
        styleButton(btnOut, new Color(178, 34, 34)); 
        
        btnOut.setEnabled(false);
        header.add(btnIn); header.add(btnOut);

        String[] columns = {"Date", "Log In", "Log Out"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        Runnable refreshTable = () -> {
            String selectedMonth = (String) monthPicker.getSelectedItem();
            Object[][] newData = employeeDao.getAttendanceByMonth(currentUser.getEmpNo(), selectedMonth);
            tableModel.setRowCount(0); 
            if (newData != null) {
                for (Object[] row : newData) tableModel.addRow(row);
            }
        };

        btnIn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Confirm Check In?", "Attendance", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-in");
                btnIn.setEnabled(false); btnOut.setEnabled(true);  
                refreshTable.run();      
            }
        });

        btnOut.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Confirm Check Out?", "Attendance", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-out");
                btnOut.setEnabled(false); btnIn.setEnabled(true);   
                refreshTable.run();      
            }
        });

        monthPicker.addActionListener(e -> refreshTable.run());
        refreshTable.run();
        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }

    private JPanel createLeaveApplicationPanel() {
        JPanel leavePanel = new JPanel(new BorderLayout(20, 20));
        leavePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder("Request New Leave"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
        JComboBox<String> comboType = new JComboBox<>(leaveTypes);
        JTextField txtStart = createField(false);
        JTextField txtEnd = createField(false);
        JTextArea txtReason = new JTextArea(4, 25);
        txtReason.setLineWrap(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnPickStart = new JButton("📅 Select Start Date");
        JButton btnPickEnd = new JButton("📅 Select End Date");
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(0, 51, 102));

        gbc.gridx = 0; gbc.gridy = 0; formContainer.add(new JLabel("Type of Leave:"), gbc);
        gbc.gridy = 1; formContainer.add(comboType, gbc);
        gbc.gridy = 2; formContainer.add(new JLabel("Start Date:"), gbc);
        gbc.gridy = 3; formContainer.add(txtStart, gbc);
        gbc.gridy = 4; formContainer.add(btnPickStart, gbc);
        gbc.gridy = 5; formContainer.add(new JLabel("End Date:"), gbc);
        gbc.gridy = 6; formContainer.add(txtEnd, gbc);
        gbc.gridy = 7; formContainer.add(btnPickEnd, gbc);
        gbc.gridy = 8; formContainer.add(new JLabel("Reason:"), gbc);
        gbc.gridy = 9; formContainer.add(new JScrollPane(txtReason), gbc);
        gbc.gridy = 10; formContainer.add(btnSubmit, gbc);

        String[] columns = {"Type", "Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(leaveService.getLeaveHistory(currentUser.getEmpNo()), columns);
        JTable table = new JTable(model);
        table.setRowHeight(30);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (value != null) ? value.toString().trim().toUpperCase() : "PENDING";
                label.setText(status);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Tahoma", Font.BOLD, 11));
                if (status.contains("APPROVED")) label.setForeground(new Color(0, 128, 0));
                else if (status.contains("DECLINED") || status.contains("REJECTED")) label.setForeground(Color.RED);
                else label.setForeground(new Color(255, 140, 0));
                return label;
            }
        });

        btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
        btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));

        btnSubmit.addActionListener(e -> {
            if (txtStart.getText().isEmpty() || txtEnd.getText().isEmpty() || txtReason.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please complete all fields.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Submit request?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                leaveService.submitLeave(currentUser.getEmpNo(), (String) comboType.getSelectedItem(), txtStart.getText(), txtEnd.getText(), txtReason.getText().trim());
                model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), columns);
                txtStart.setText(""); txtEnd.setText(""); txtReason.setText("");
                JOptionPane.showMessageDialog(this, "Leave Request Sent!");
            }
        });

        leavePanel.add(formContainer, BorderLayout.WEST);
        leavePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return leavePanel;
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

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

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnUpdate = new JButton("Update Personal Info");
        styleButton(btnUpdate, new Color(34, 139, 34));
        btnUpdate.addActionListener(e -> updatePersonalInfo());

        btnUndo = new JButton("Undo Changes");
        styleButton(btnUndo, new Color(178, 34, 34)); 
        btnUndo.setEnabled(false); // Only enable after an update
        btnUndo.addActionListener(e -> undoChanges());

        JButton btnViewPayslip = new JButton("View Current Payslip");
        styleButton(btnViewPayslip, new Color(0, 32, 77));
        btnViewPayslip.addActionListener(e -> showCurrentPayslip());

        JButton btnExportPDF = new JButton("Export to PDF/Print");
        styleButton(btnExportPDF, new Color(64, 64, 64));
        btnExportPDF.addActionListener(e -> exportPayslipToPDF());

        actionPanel.add(btnUpdate);
        actionPanel.add(btnViewPayslip); 
        actionPanel.add(btnExportPDF);
        actionPanel.add(btnUndo);

        mainPanel.add(infoPanel); mainPanel.add(personalPanel); mainPanel.add(financePanel);
        mainPanel.add(Box.createVerticalStrut(15)); mainPanel.add(actionPanel);

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(200, 40)); btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEtchedBorder()); btn.setContentAreaFilled(true);
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

    private void updatePersonalInfo() {
        // Create backup before editing
        this.employeeBackup = new RegularStaff(
            currentUser.getEmpNo(), currentUser.getLastName(), 
            currentUser.getFirstName(), currentUser.getBirthday(), currentUser.getBasicSalary()
        );
        employeeBackup.setAddress(currentUser.getAddress());
        employeeBackup.setPhone(currentUser.getPhone());

        
        currentUser.setAddress(txtAddress.getText().trim());
        currentUser.setPhone(txtPhone.getText().trim());
        
        if (leaveService.updateEmployeeProfile(currentUser)) {
            JOptionPane.showMessageDialog(this, "Update Successful!");
            btnUndo.setEnabled(true); 
        } else {
            JOptionPane.showMessageDialog(this, "Update Failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void undoChanges() {
        if (employeeBackup != null && JOptionPane.showConfirmDialog(this, "Discard changes?", "Undo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            currentUser.setAddress(employeeBackup.getAddress());
            currentUser.setPhone(employeeBackup.getPhone());
            leaveService.updateEmployeeProfile(currentUser);
            loadEmployeeDetails(); 
            btnUndo.setEnabled(false);
            employeeBackup = null;
        }
    }

    private JTextField createField(boolean editable) {
        JTextField field = new JTextField();
        field.setEditable(editable);
        if (!editable) field.setBackground(new Color(245, 245, 245));
        return field;
    }

    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(190, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        nav.add(btn); nav.add(Box.createVerticalStrut(10));
    }

    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE); label.setFont(new Font("Tahoma", style, size));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(label);
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
            for (int x = 6 + dayOfWeek, day = 1; day <= daysInMonth; x++, day++)
                button[x].setText("" + day);
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