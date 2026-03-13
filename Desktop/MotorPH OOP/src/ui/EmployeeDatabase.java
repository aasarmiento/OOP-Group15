package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import model.Employee;
import service.EmployeeManagementService;
import util.EmployeeDetailForm;

public class EmployeeDatabase extends JPanel {
    private CardLayout masterlistLayout;
    private JPanel masterlistContainer;
    private DefaultTableModel empTableModel;
    private JTable empTable;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private final EmployeeManagementService employeeManagementService;
    private final Employee currentUser;

    // MotorPH Branding
    private final Color MOTORPH_MAROON = new Color(128, 0, 0);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Font titleFont = new Font("DM Sans Bold", Font.BOLD, 20);
    private final Font headerFont = new Font("DM Sans Bold", Font.BOLD, 12);
    private final Font bodyFont = new Font("DM Sans Regular", Font.PLAIN, 13);

    public EmployeeDatabase(EmployeeManagementService service, Employee user) {
        this.employeeManagementService = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        add(createMasterlistPanel());
        refreshTable();
    }

    private JPanel createMasterlistPanel() {
        masterlistLayout = new CardLayout();
        masterlistContainer = new JPanel(masterlistLayout);
        masterlistContainer.setOpaque(false);

        // --- TABLE VIEW PANEL ---
        JPanel tableView = new JPanel(new BorderLayout(20, 20));
        tableView.setOpaque(false);
        tableView.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Top Row: Title and Search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Employee Masterlist");
        lblTitle.setFont(titleFont);
        lblTitle.setForeground(new Color(45, 45, 45));

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));
        setupPlaceholder(searchField, "Search by Name, ID, or Position...");
        
        // Search bar with rounded border
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new util.RoundedBorder(15, new Color(210, 210, 210)), // 15 is the radius for a smooth look
    BorderFactory.createEmptyBorder(0, 10, 0, 10)       // Inner padding for text
        ));

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.EAST);

        // Center: Styled Table Container
        JPanel tableContainer = createStyledTile();
        tableContainer.setLayout(new BorderLayout());

        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        empTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        empTable = new JTable(empTableModel);
        
        empTable.setRowHeight(35);
        empTable.setFont(bodyFont);
        empTable.setShowGrid(false);
        empTable.setIntercellSpacing(new Dimension(0, 0));
        empTable.setSelectionBackground(MOTORPH_MAROON);

        JTableHeader tableHeader = empTable.getTableHeader();
        tableHeader.setFont(headerFont);
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setForeground(Color.GRAY);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        ((DefaultTableCellRenderer)tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        JScrollPane scrollPane = new JScrollPane(empTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnAddNew = new JButton("Add New");
        JButton btnView = new JButton("View Details");
        JButton btnDelete = new JButton("Delete");
        
        styleButton(btnAddNew, new Color(45, 45, 45)); 
        styleButton(btnView, MOTORPH_MAROON);        
        styleButton(btnDelete, new Color(180, 0, 0));  

        btnPanel.add(btnAddNew); 
        btnPanel.add(btnView);  
        btnPanel.add(btnDelete);

        tableView.add(topPanel, BorderLayout.NORTH);
        tableView.add(tableContainer, BorderLayout.CENTER);
        tableView.add(btnPanel, BorderLayout.SOUTH);

        // --- LOGIC BINDING ---
        rowSorter = new TableRowSorter<>(empTableModel);
        empTable.setRowSorter(rowSorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().isEmpty() || text.equals("Search by Name, ID, or Position...")) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        btnAddNew.addActionListener(e -> masterlistLayout.show(masterlistContainer, "FORM"));

       // Updated View Details Logic
btnView.addActionListener(e -> {
    int row = empTable.getSelectedRow();
    if (row != -1) {
        int modelRow = empTable.convertRowIndexToModel(row); 
        try {
            int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
            Object[] rawDetails = employeeManagementService.getEmployeeDetailsForForm(empId);
            if (rawDetails != null) {
                new EmployeeDetailForm(rawDetails, employeeManagementService, currentUser).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening details.", "System Error", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        // Validation Dialog when no row is selected
        JOptionPane.showMessageDialog(this, 
            "Please select an employee first to view details.", 
            "Selection Required", 
            JOptionPane.WARNING_MESSAGE);
    }
});

// Updated Delete Logic
btnDelete.addActionListener(e -> {
    int row = empTable.getSelectedRow();
    if (row != -1) {
        int modelRow = empTable.convertRowIndexToModel(row);
        
        // Security Verification Dialog
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this employee record?", 
            "Security Verification", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Get Employee ID from column 0
            int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
            
            // FIX: Changed removeEmployee to deleteEmployee to match Service Layer
            if (employeeManagementService.deleteEmployee((model.IAdminOperations)currentUser, empId)) { 
                refreshTable(); 
                JOptionPane.showMessageDialog(this, "Record successfully removed."); 
            } else {
                // This triggers if the ID is 10001 or the service returns false
                JOptionPane.showMessageDialog(this, "Delete failed: Record is protected or access denied.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } else {
        // Validation Dialog when no row is selected
        JOptionPane.showMessageDialog(this, 
            "Please select an employee first to delete.", 
            "Selection Required", 
            JOptionPane.WARNING_MESSAGE);
    }
});

        masterlistContainer.add(tableView, "TABLE");
        masterlistContainer.add(createNewHireFormPanel(), "FORM");
        
        return masterlistContainer;
    }

private JPanel createNewHireFormPanel() {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBackground(BACKGROUND_COLOR);
    wrapper.setBorder(new EmptyBorder(30, 40, 30, 40));

    JLabel formTitle = new JLabel("New Employee Registration");
    formTitle.setFont(titleFont);
    formTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

    JPanel formScrollContainer = new JPanel();
    formScrollContainer.setLayout(new BoxLayout(formScrollContainer, BoxLayout.Y_AXIS));
    formScrollContainer.setOpaque(false);

    // --- Financial Information (FORCED BLACK TEXT & READ-ONLY) ---
    JTextField basicSalaryInput = createStyledTextField("0.00"); applyFilters(13, basicSalaryInput);
    basicSalaryInput.setForeground(Color.BLACK);
    
    // Rice Subsidy
    JTextField rice = createStyledTextField("0"); applyFilters(14, rice);
    rice.setFocusable(false); // Prevents clicking/typing
    rice.setForeground(Color.BLACK); // Keeps text pitch black
    rice.setBackground(Color.WHITE); 

    // Phone Allowance
    JTextField pallow = createStyledTextField("0"); applyFilters(15, pallow);
    pallow.setFocusable(false);
    pallow.setForeground(Color.BLACK);
    pallow.setBackground(Color.WHITE);

    // Clothing Allowance
    JTextField cloth = createStyledTextField("0"); applyFilters(16, cloth);
    cloth.setFocusable(false);
    cloth.setForeground(Color.BLACK);
    cloth.setBackground(Color.WHITE);

    // --- Personal Information ---
    JPanel personalPanel = createFormSection("Personal Information", 3, 2);
    JTextField fName = createStyledTextField("Juan");
    applyFilters(100, fName); 
    
    JTextField lName = createStyledTextField("Delacruz");
    applyFilters(100, lName); 

    JTextField bday = createStyledTextField("MM/dd/yyyy");
    bday.setEditable(false); 
    bday.setBackground(Color.WHITE); 
    bday.setForeground(Color.BLACK);

    JButton btnPickBday = new JButton("📅");
    btnPickBday.addActionListener(e -> {
        DatePicker picker = new DatePicker(this); 
        String pickedValue = picker.setPickedDate(); 
        if (pickedValue != null && !pickedValue.isEmpty()) { bday.setText(pickedValue); bday.setForeground(Color.BLACK); }
    });
    JPanel bdayWrapper = new JPanel(new BorderLayout(5, 0)); bdayWrapper.setOpaque(false);
    bdayWrapper.add(bday, BorderLayout.CENTER); bdayWrapper.add(btnPickBday, BorderLayout.EAST);

    JTextField address = createStyledTextField("Address line...");
    JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
    JTextField phone = createStyledTextField("000-000-000");
    applyFilters(5, phone);

    addFormField(personalPanel, "First Name", fName);
    addFormField(personalPanel, "Last Name", lName);
    addFormField(personalPanel, "Birthday", bdayWrapper);
    addFormField(personalPanel, "Address:", address);
    addFormField(personalPanel, "Gender:", genderCombo); 
    addFormField(personalPanel, "Phone Number:", phone);

    // --- Identification ---
    JPanel govPanel = createFormSection("Identification & Status", 3, 2);
    JTextField sss = createStyledTextField("00-0000000-0"); applyFilters(6, sss);
    JTextField phil = createStyledTextField("000000000000"); applyFilters(7, phil);
    JTextField tin = createStyledTextField("000-000-000-000"); applyFilters(8, tin);
    JTextField pagibig = createStyledTextField("000000000000"); applyFilters(9, pagibig);
    JComboBox<String> status = new JComboBox<>(new String[]{"Regular", "Probationary"});
    
    addFormField(govPanel, "SSS #:", sss);
    addFormField(govPanel, "Philhealth #:", phil);
    addFormField(govPanel, "TIN #:", tin);
    addFormField(govPanel, "Pag-ibig #:", pagibig);
    addFormField(govPanel, "Status:", status);

    // --- Employment ---
    JPanel jobPanel = createFormSection("Employment Details", 1, 2);
    String[] positions = {"Chief Operating Officer", "Chief Finance Officer", "Chief Marketing Officer", "IT Operations and Systems", "HR Manager", "Accounting Head", "Payroll Manager", "Account Manager", "Sales & Marketing", "HR Team Leader", "Payroll Team Leader", "Account Team Leader"};
    JComboBox<String> posCombo = new JComboBox<>(positions);
    JComboBox<String> supervCombo = new JComboBox<>(new String[]{"N/A"});
    
    posCombo.addActionListener(e -> {
        String selectedPos = (String)posCombo.getSelectedItem();
        String[] available = employeeManagementService.getSupervisorsForPosition(selectedPos);
        supervCombo.removeAllItems();
        for (String s : available) supervCombo.addItem(s);

        double[] allowances = employeeManagementService.getStandardAllowances(selectedPos);
        rice.setText(String.format("%.0f", allowances[0]));
        pallow.setText(String.format("%.0f", allowances[1]));
        cloth.setText(String.format("%.0f", allowances[2]));
    });

    if (posCombo.getItemCount() > 0) {
        posCombo.setSelectedIndex(0);
    }
    
    addFormField(jobPanel, "Position:", posCombo);
    addFormField(jobPanel, "Supervisor:", supervCombo);

    // --- Financial Panel Assembly ---
    JPanel financePanel = createFormSection("Financial Information", 2, 2);
    addFormField(financePanel, "Monthly Basic Salary:", basicSalaryInput);
    addFormField(financePanel, "Rice Subsidy:", rice);
    addFormField(financePanel, "Phone Allowance:", pallow);
    addFormField(financePanel, "Clothing Allowance:", cloth);

    formScrollContainer.add(personalPanel);
    formScrollContainer.add(Box.createVerticalStrut(15));
    formScrollContainer.add(govPanel);
    formScrollContainer.add(Box.createVerticalStrut(15));
    formScrollContainer.add(jobPanel);
    formScrollContainer.add(Box.createVerticalStrut(15));
    formScrollContainer.add(financePanel);
    formScrollContainer.add(Box.createVerticalGlue());

    JScrollPane scroll = new JScrollPane(formScrollContainer);
    scroll.setBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);

    // --- Buttons ---
    JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
    bp.setOpaque(false);
    JButton btnBack = new JButton("Cancel"); 
    JButton btnSave = new JButton("Confirm Hire");
    styleButton(btnSave, new Color(0, 102, 51));
    
    btnBack.addActionListener(e -> {
        clearFormFields(fName, lName, bday, address, phone, sss, phil, tin, pagibig, basicSalaryInput, rice, pallow, cloth);
        masterlistLayout.show(masterlistContainer, "TABLE");
    });

    btnSave.addActionListener(e -> {
        // --- VALIDATION BLOCK ---
        if (fName.getText().trim().isEmpty() || fName.getText().equals("Juan") ||
            lName.getText().trim().isEmpty() || lName.getText().equals("Delacruz") ||
            bday.getText().equals("MM/dd/yyyy") ||
            address.getText().trim().isEmpty() || address.getText().equals("Address line...") ||
            phone.getText().trim().isEmpty() || phone.getText().equals("000-000-000") ||
            sss.getText().trim().isEmpty() || phil.getText().trim().isEmpty() ||
            tin.getText().trim().isEmpty() || pagibig.getText().trim().isEmpty() ||
            basicSalaryInput.getText().trim().isEmpty() || basicSalaryInput.getText().equals("0.00")) {
            
            JOptionPane.showMessageDialog(this, "All fields are required. Please fill in all missing information.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // STOP EXECUTION
        }

        model.Employee newEmp = new model.RegularStaff();
        newEmp.setFirstName(fName.getText().trim());
        newEmp.setLastName(lName.getText().trim());
        newEmp.setGender(genderCombo.getSelectedItem().toString());
        newEmp.setAddress(address.getText().trim());
        newEmp.setPhone(phone.getText().trim());
        newEmp.setSss(sss.getText().trim());
        newEmp.setPhilhealth(phil.getText().trim());
        newEmp.setTin(tin.getText().trim());
        newEmp.setPagibig(pagibig.getText().trim());
        newEmp.setStatus(status.getSelectedItem().toString());
        newEmp.setPosition(posCombo.getSelectedItem().toString());
        newEmp.setSupervisor(supervCombo.getSelectedItem().toString());

        try {
            String bdayStr = bday.getText().trim();
            if(!bdayStr.equals("MM/dd/yyyy")) {
                newEmp.setBirthday(java.time.LocalDate.parse(bdayStr, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
        } catch (Exception ex) { }
        
        try {
            double salary = Double.parseDouble(basicSalaryInput.getText().replaceAll("[^\\d.]", ""));
            newEmp.setBasicSalary(salary);
            newEmp.setRiceSubsidy(Double.parseDouble(rice.getText().replaceAll("[^\\d.]", "")));
            newEmp.setPhoneAllowance(Double.parseDouble(pallow.getText().replaceAll("[^\\d.]", "")));
            newEmp.setClothingAllowance(Double.parseDouble(cloth.getText().replaceAll("[^\\d.]", "")));
            
            if (employeeManagementService.registerEmployee((model.IAdminOperations)currentUser, newEmp)) {
                JOptionPane.showMessageDialog(this, "Employee Added Successfully! ID: " + newEmp.getEmpNo());
                clearFormFields(fName, lName, bday, address, phone, sss, phil, tin, pagibig, basicSalaryInput, rice, pallow, cloth);
                refreshTable(); 
                masterlistLayout.show(masterlistContainer, "TABLE"); 
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for financial fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    bp.add(btnBack); bp.add(btnSave);
    wrapper.add(formTitle, BorderLayout.NORTH);
    wrapper.add(scroll, BorderLayout.CENTER);
    wrapper.add(bp, BorderLayout.SOUTH);
    return wrapper;
}

  private void clearFormFields(JTextField fName, JTextField lName, JTextField bday, 
                             JTextField address, JTextField phone, JTextField sss, 
                             JTextField phil, JTextField tin, JTextField pagibig, 
                             JTextField hourlyRate, JTextField rice, JTextField pallow, JTextField cloth) {
    
    // Clear text fields and restore placeholders using your existing setupPlaceholder method
    setupPlaceholder(fName, "Juan");
    setupPlaceholder(lName, "Delacruz");
    setupPlaceholder(bday, "MM/dd/yyyy");
    setupPlaceholder(address, "Address line...");
    setupPlaceholder(phone, "000-000-000");
    setupPlaceholder(sss, "00-0000000-0");
    setupPlaceholder(phil, "000000000000");
    setupPlaceholder(tin, "000-000-000-000");
    setupPlaceholder(pagibig, "000000000000");
    setupPlaceholder(hourlyRate, "0.00");
    setupPlaceholder(rice, "0");
    setupPlaceholder(pallow, "0");
    setupPlaceholder(cloth, "0");
}

    // --- REFINED UI HELPERS ---

    private JPanel createStyledTile() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.setColor(new Color(225, 225, 225));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JTextField createStyledTextField(String hint) {
        JTextField f = new JTextField();
        f.setFont(bodyFont);
        setupPlaceholder(f, hint);
        return f;
    }

    private JPanel createFormSection(String title, int r, int c) {
        JPanel p = createStyledTile();
        p.setLayout(new GridLayout(r, c, 20, 10));
        TitledBorder tb = BorderFactory.createTitledBorder(null, " " + title + " ", 0, 0, headerFont, MOTORPH_MAROON);
        p.setBorder(BorderFactory.createCompoundBorder(p.getBorder(), tb));
        return p;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(headerFont);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 20, 8, 20));
    }

    private void setupPlaceholder(JTextField field, String hint) {
        field.setText(hint); field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { if (field.getText().equals(hint)) { field.setText(""); field.setForeground(Color.BLACK); } }
            @Override public void focusLost(java.awt.event.FocusEvent e) { if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(hint); } }
        });
    }

    private void addFormField(JPanel p, String label, JComponent f) {
        JPanel container = new JPanel(new BorderLayout(0, 5));
        container.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("DM Sans Medium", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        container.add(lbl, BorderLayout.NORTH);
        container.add(f, BorderLayout.CENTER);
        p.add(container);
    }

    public final void refreshTable() {
        if (empTableModel == null) return;
        SwingUtilities.invokeLater(() -> {
            empTableModel.setRowCount(0);
            List<Employee> list = employeeManagementService.getAll();
            if (list != null) {
                for (Employee emp : list) {
                    empTableModel.addRow(new Object[]{ 
                        emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), 
                        emp.getStatus(), emp.getPosition(), emp.getSupervisor() 
                    });
                }
            }
        });
    }

    private void applyFilters(int index, JTextField field) {
    AbstractDocument doc = (AbstractDocument) field.getDocument();
    switch (index) {
        case 5: doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###")); break;
        case 6: doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#")); break;
        case 7: case 9: doc.setDocumentFilter(new util.NumericLimitFilter(12)); break;
        case 8: doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###-###")); break;
        case 13: case 14: case 15: case 16: doc.setDocumentFilter(new util.NumericLimitFilter(7)); break;
        case 100: // LETTERS ONLY (No numbers or special chars)
            doc.setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
                        throws javax.swing.text.BadLocationException {
                    if (text.matches("[a-zA-Z\\s]*")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });
            break;
    }
}

    // --- DATE PICKER ---
    public class DatePicker {
        private int month, year, dayInt = 0;
        private JDialog d;
        private JButton[] button = new JButton[42];

        public DatePicker(Component parent) {
            Window parentWindow = SwingUtilities.getWindowAncestor(parent);
            d = new JDialog(parentWindow instanceof Frame ? (Frame)parentWindow : null); 
            d.setModal(true); d.setTitle("Select Birthdate"); d.setLayout(new BorderLayout());
            java.util.Calendar now = java.util.Calendar.getInstance();
            month = now.get(java.util.Calendar.MONTH);
            year = now.get(java.util.Calendar.YEAR) - 18;
            JPanel p1 = new JPanel(new GridLayout(6, 7));
            for (int x = 0; x < button.length; x++) {
                final int selection = x; button[x] = new JButton();
                if (x < 7) {
                    String[] header = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
                    button[x].setText(header[x]); button[x].setForeground(Color.RED); button[x].setEnabled(false);
                } else {
                    button[x].addActionListener(e -> { if (!button[selection].getText().isEmpty()) { dayInt = Integer.parseInt(button[selection].getText()); d.dispose(); } });
                }
                p1.add(button[x]);
            }
            JComboBox<String> monthCombo = new JComboBox<>(new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"});
            monthCombo.setSelectedIndex(month);
            Integer[] yearsArr = new Integer[80]; int startYear = now.get(java.util.Calendar.YEAR) - 18;
            for (int i = 0; i < 80; i++) yearsArr[i] = startYear - i;
            JComboBox<Integer> yearCombo = new JComboBox<>(yearsArr);
            monthCombo.addActionListener(e -> { month = monthCombo.getSelectedIndex(); displayDate(); });
            yearCombo.addActionListener(e -> { year = (Integer) yearCombo.getSelectedItem(); displayDate(); });
            JPanel p2 = new JPanel(); p2.add(monthCombo); p2.add(yearCombo);
            d.add(p2, BorderLayout.NORTH); d.add(p1, BorderLayout.CENTER); d.pack(); d.setLocationRelativeTo(parent);
            displayDate(); d.setVisible(true);
        }

        public void displayDate() {
            for (int x = 7; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance(); cal.set(year, month, 1);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 6 + dayOfWeek, dayNum = 1; dayNum <= daysInMonth; x++, dayNum++) if (x < button.length) button[x].setText("" + dayNum);
        }


      

        public String setPickedDate() {
            if (dayInt == 0) return "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
            java.util.Calendar cal = java.util.Calendar.getInstance(); cal.set(year, month, dayInt);
            return sdf.format(cal.getTime());
        }
    }
}