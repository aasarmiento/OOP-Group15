package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import model.Employee;
import model.IAdminOperations;
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

    public EmployeeDatabase(EmployeeManagementService service, Employee user) {
        this.employeeManagementService = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        add(createMasterlistPanel());
        refreshTable();
    }

    private JPanel createMasterlistPanel() {
        masterlistLayout = new CardLayout();
        masterlistContainer = new JPanel(masterlistLayout);
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));

        // --- NEW SEARCH BAR UI ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 30));
        setupPlaceholder(searchField, "Search by Name, ID, or Position...");
        
        JLabel searchIcon = new JLabel("🔍 Search: ");
        searchIcon.setFont(new Font("Tahoma", Font.BOLD, 12));
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // --- TABLE SETUP ---
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        empTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        empTable = new JTable(empTableModel);
        
        // --- SEARCH LOGIC ---
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

        JButton btnAddNew = new JButton("Add");
        JButton btnView = new JButton("View Details");
        JButton btnDelete = new JButton("Delete");
        
        styleButton(btnAddNew, new Color(0, 82, 204));
        styleButton(btnView, new Color(70, 130, 180));
        styleButton(btnDelete, new Color(255, 0, 0));

        btnAddNew.addActionListener(e -> masterlistLayout.show(masterlistContainer, "FORM"));

       // Inside EmployeeDatabase.java -> createMasterlistPanel()
btnView.addActionListener(e -> {
    int row = empTable.getSelectedRow();
    if (row != -1) {
        int modelRow = empTable.convertRowIndexToModel(row); 
        try {
            int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
            
            // Requesting the Model data through the Service Layer
            Object[] details = employeeManagementService.getEmployeeDetailsForForm(empId);
            
            if (details != null) {
                // Launching the UI (Detail Form) and passing the Service + User
                new EmployeeDetailForm(details, employeeManagementService, currentUser).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
});
      
        btnDelete.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row != -1) {
                int modelRow = empTable.convertRowIndexToModel(row);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
                    if (employeeManagementService.removeEmployee((IAdminOperations)currentUser, empId)) { 
                        refreshTable(); 
                        JOptionPane.showMessageDialog(this, "Deleted."); 
                    }
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.add(btnAddNew); 
        btnPanel.add(btnView);  
        btnPanel.add(btnDelete);
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(empTable), BorderLayout.CENTER);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);

        masterlistContainer.add(tablePanel, "TABLE");
        masterlistContainer.add(createNewHireFormPanel(), "FORM");
        
        return masterlistContainer;
    }

 private JPanel createNewHireFormPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    
    JPanel formContainer = new JPanel();
    formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

    // --- SECTION 1: Personal Information ---
    JPanel personalPanel = createFormSection("Personal Information", 0, 2);
    JTextField fName = new JTextField(); 
    setupPlaceholder(fName, "Juan");

    JTextField lName = new JTextField();
    setupPlaceholder(lName, "Delacruz");

    JTextField bday = new JTextField();
    setupPlaceholder(bday, "MM/dd/yyyy");

    JButton btnPickBday = new JButton("📅 Pick Date");
    btnPickBday.addActionListener(e -> {
        DatePicker picker = new DatePicker(mainPanel); 
        String pickedValue = picker.setPickedDate(); 
        if (pickedValue != null && !pickedValue.isEmpty()) {
            bday.setText(pickedValue);
            bday.setForeground(Color.BLACK);
            bday.setFont(bday.getFont().deriveFont(Font.PLAIN));
        }
    });

    JTextField address = new JTextField();
    setupPlaceholder(address, "e.g. 123 Street, City, Province");

    JTextField phone = new JTextField();
    applyFilters(5, phone); 
    setupPlaceholder(phone, "000-000-000");

    addFormField(personalPanel, "First Name *", fName);
    addFormField(personalPanel, "Last Name *", lName);
    personalPanel.add(new JLabel("Birthday *")); 
    JPanel bw = new JPanel(new BorderLayout()); bw.add(bday); bw.add(btnPickBday, BorderLayout.EAST);
    personalPanel.add(bw);
    addFormField(personalPanel, "Address:", address);
    addFormField(personalPanel, "Phone Number:", phone);

    // --- SECTION 2: Identification & Status ---
    JPanel govPanel = createFormSection("Identification & Status", 0, 2);
    JTextField sss = new JTextField(); 
    applyFilters(6, sss);
    setupPlaceholder(sss, "00-0000000-0");

    JTextField phil = new JTextField(); 
    applyFilters(7, phil);
    setupPlaceholder(phil, "000000000000");

    JTextField tin = new JTextField(); 
    applyFilters(8, tin);
    setupPlaceholder(tin, "000-000-000-000");

    JTextField pagibig = new JTextField(); 
    applyFilters(9, pagibig);
    setupPlaceholder(pagibig, "000000000000");
    
    JComboBox<String> status = new JComboBox<>(new String[]{"Regular", "Probationary"});
    addFormField(govPanel, "SSS #:", sss);
    addFormField(govPanel, "Philhealth #:", phil);
    addFormField(govPanel, "TIN #:", tin);
    addFormField(govPanel, "Pag-ibig #:", pagibig);
    addFormField(govPanel, "Status:", status);

    // --- SECTION 3: Employment ---
    JPanel jobPanel = createFormSection("Employment", 0, 2);
    
    String[] positions = {
        "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer", 
        "Chief Marketing Officer", "IT Operations and Systems", "HR Manager", 
        "HR Team Leader", "HR Rank and File", "Accounting Head", "Payroll Manager", 
        "Payroll Team Leader", "Payroll Rank and File", "Account Manager", 
        "Account Team Leader", "Account Rank and File", "Sales & Marketing", 
        "Supply Chain and Logistics", "Customer Service and Relations"
    };
    JComboBox<String> posCombo = new JComboBox<>(positions);
    JComboBox<String> supervCombo = new JComboBox<>(new String[]{"N/A"});

    posCombo.addActionListener(e -> {
        String selectedPos = (String) posCombo.getSelectedItem();
        String[] availableSupervisors = employeeManagementService.getSupervisorsForPosition(selectedPos);
        supervCombo.removeAllItems();
        for (String s : availableSupervisors) {
            supervCombo.addItem(s);
        }
    });

    addFormField(jobPanel, "Position:", posCombo);
    addFormField(jobPanel, "Supervisor:", supervCombo);

    // --- SECTION 4: Financial Information ---
    JPanel financePanel = createFormSection("Financial Information", 0, 4);
    JTextField salary = new JTextField("0"); applyFilters(13, salary);
    JTextField rice = new JTextField("0"); applyFilters(14, rice);
    JTextField pallow = new JTextField("0"); applyFilters(15, pallow);
    JTextField cloth = new JTextField("0"); applyFilters(16, cloth);

    addFormField(financePanel, "Salary:", salary);
    addFormField(financePanel, "Rice:", rice);
    addFormField(financePanel, "Phone:", pallow);
    addFormField(financePanel, "Clothing:", cloth);

    formContainer.add(personalPanel); formContainer.add(govPanel); formContainer.add(jobPanel); formContainer.add(financePanel);

    // --- ACTION BUTTONS ---
    JButton btnSave = new JButton("Confirm Hire");
    styleButton(btnSave, new Color(34, 139, 34));
    
    btnSave.addActionListener(e -> {
        JTextField[] fieldsForService = new JTextField[17]; 
        fieldsForService[0] = new JTextField("0"); 
        fieldsForService[1] = lName;
        fieldsForService[2] = fName;
        fieldsForService[3] = bday;
        fieldsForService[4] = address;
        fieldsForService[5] = phone;
        fieldsForService[6] = sss;
        fieldsForService[7] = phil;
        fieldsForService[8] = tin;
        fieldsForService[9] = pagibig;
        fieldsForService[10] = new JTextField(status.getSelectedItem().toString());
        fieldsForService[11] = new JTextField(posCombo.getSelectedItem().toString());
        fieldsForService[12] = new JTextField(supervCombo.getSelectedItem().toString());
        fieldsForService[13] = salary;
        fieldsForService[14] = rice;
        fieldsForService[15] = pallow;
        fieldsForService[16] = cloth;

        if (employeeManagementService.saveOrUpdateFromUI((Employee)currentUser, fieldsForService, true)) {
            JOptionPane.showMessageDialog(this, "Employee Registered Successfully!");
            refreshTable(); 
            masterlistLayout.show(masterlistContainer, "TABLE"); 
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


private void applyFilters(int index, JTextField field) {
    AbstractDocument doc = (AbstractDocument) field.getDocument();
    
    switch (index) {
        case 5: // Phone #
            doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###"));
            break;
        case 6: // SSS # (##-#######-#)
            doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#"));
            break;
        case 7: // Philhealth (12 digits raw)
        case 9: // Pag-ibig (12 digits raw)
            doc.setDocumentFilter(new util.NumericLimitFilter(12));
            break;
        case 8: // TIN # (###-###-###-###)
            doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###-###"));
            break;
        case 13: // Salary
        case 14: // Rice
        case 15: // Phone
        case 16: // Clothing
            // Block all non-digits and limit to 7 digits
            doc.setDocumentFilter(new util.NumericLimitFilter(7));
            break;
    }
}
    // --- DATE PICKER CLASS ---
    public class DatePicker {
    private int month, year;
    private int dayInt = 0; // Use an int to store the final selection
    private JDialog d;
    private JButton[] button = new JButton[42];

    public DatePicker(Component parent) {
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        d = new JDialog(parentWindow instanceof Frame ? (Frame)parentWindow : null); 
        d.setModal(true); 
        d.setTitle("Select Date");
        d.setLayout(new BorderLayout());

        java.util.Calendar now = java.util.Calendar.getInstance();
        month = now.get(java.util.Calendar.MONTH);
        year = now.get(java.util.Calendar.YEAR) - 18;

        JPanel p1 = new JPanel(new GridLayout(6, 7)); // Adjusted grid for 42 buttons
        String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

        // Create the calendar grid
        for (int x = 0; x < button.length; x++) {
            final int selection = x;
            button[x] = new JButton();
            
            // Header buttons (Sun-Sat)
            if (x < 7) {
                button[x].setText(header[x]);
                button[x].setForeground(Color.RED);
                button[x].setEnabled(false);
                button[x].setBorderPainted(false);
            } else {
                button[x].addActionListener(e -> {
                    String btnText = button[selection].getText();
                    if (!btnText.isEmpty()) {
                        // CONVERSION: Turn the String "15" into an int 15
                        dayInt = Integer.parseInt(btnText);
                        d.dispose(); // Close the dialog
                    }
                });
            }
            p1.add(button[x]);
        }

        // --- Selection Logic (Months and Years) ---
        JPanel p2 = new JPanel(new FlowLayout());
        JComboBox<String> monthCombo = new JComboBox<>(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        monthCombo.setSelectedIndex(month);
        
        Integer[] yearsArr = new Integer[80]; 
        int startYear = now.get(java.util.Calendar.YEAR) - 18;
        for (int i = 0; i < 80; i++) yearsArr[i] = startYear - i;
        JComboBox<Integer> yearCombo = new JComboBox<>(yearsArr);
        yearCombo.setSelectedItem(year);

        monthCombo.addActionListener(e -> { month = monthCombo.getSelectedIndex(); displayDate(); });
        yearCombo.addActionListener(e -> { year = (Integer) yearCombo.getSelectedItem(); displayDate(); });

        p2.add(monthCombo); p2.add(yearCombo);
        d.add(p2, BorderLayout.NORTH); 
        d.add(p1, BorderLayout.CENTER);
        d.pack(); 
        d.setLocationRelativeTo(parent);
        displayDate();
        d.setVisible(true); // This blocks code execution until d.dispose() is called
    }

    public void displayDate() {
        for (int x = 7; x < button.length; x++) button[x].setText("");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, 1);
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        
        // Populate numbers in the grid
        for (int x = 6 + dayOfWeek, dayNum = 1; dayNum <= daysInMonth; x++, dayNum++) {
            if (x < button.length) {
                button[x].setText("" + dayNum);
            }
        }
    }

    /**
     * N-TIER UI METHOD: 
     * This prepares the final 'ichura' (format) of the date 
     * for the text field.
     */
    public String setPickedDate() {
        if (dayInt == 0) return ""; 

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        // No more error: all arguments are now 'int'
        cal.set(year, month, dayInt); 
        
        return sdf.format(cal.getTime());
    }
}
    public final void refreshTable() {
        if (empTableModel == null) return;
        empTableModel.setRowCount(0);
        List<Employee> list = employeeManagementService.getAll();
        for (Employee emp : list) {
            empTableModel.addRow(new Object[]{ emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getStatus(), emp.getPosition(), emp.getSupervisor() });
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }

    private void setupPlaceholder(JTextField field, String hint) {
        field.setText(hint); field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { if (field.getText().equals(hint)) { field.setText(""); field.setForeground(Color.BLACK); } }
            @Override public void focusLost(java.awt.event.FocusEvent e) { if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(hint); } }
        });
    }

    private String getActualValue(JTextField field, String hint) {
        String val = field.getText().trim();
        return val.equals(hint) ? "" : val;
    }

    private void addFormField(JPanel p, String label, JComponent f) {
        p.add(new JLabel(label)); p.add(f);
    }

    private JPanel createFormSection(String title, int rows, int cols) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
        return p;
    }



    
}