package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    // UI Styling Constants
    private final Color ACCENT_COLOR = new Color(0, 82, 204);
    private final Font INTER_BOLD = new Font("Tahoma", Font.BOLD, 13);
    private final Font INTER_REGULAR = new Font("Tahoma", Font.PLAIN, 12);

    public EmployeeDatabase(EmployeeManagementService service, Employee user) {
        this.employeeManagementService = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(createMasterlistPanel());
        refreshTable();
    }

    private JPanel createMasterlistPanel() {
        masterlistLayout = new CardLayout();
        masterlistContainer = new JPanel(masterlistLayout);
        masterlistContainer.setOpaque(false);

        JPanel tablePanel = new JPanel(new BorderLayout(15, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- SEARCH BAR ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));
        setupPlaceholder(searchField, "Search by Name, ID, or Position...");
        
        JLabel searchIcon = new JLabel("🔍 Search: ");
        searchIcon.setFont(INTER_BOLD);
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // --- TABLE ---
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        empTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        empTable = new JTable(empTableModel);
        empTable.setRowHeight(30);
        
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

        // --- BUTTONS ---
        JButton btnAddNew = new JButton("Add New Hire");
        JButton btnView = new JButton("View Details");
        JButton btnDelete = new JButton("Delete");
        
        styleButton(btnAddNew, ACCENT_COLOR);
        styleButton(btnView, new Color(70, 130, 180));
        styleButton(btnDelete, new Color(220, 53, 69));

       btnAddNew.addActionListener(e -> {
    // Call a clear function or simply recreate the form panel
    masterlistContainer.add(createNewHireFormPanel(), "FORM"); 
    masterlistLayout.show(masterlistContainer, "FORM");
});
        
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
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row != -1) {
                int modelRow = empTable.convertRowIndexToModel(row);
                if (JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
                    if (employeeManagementService.removeEmployee((IAdminOperations)currentUser, empId)) {
                        refreshTable();
                    }
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnView); btnPanel.add(btnDelete); btnPanel.add(btnAddNew); 
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(empTable), BorderLayout.CENTER);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);

        masterlistContainer.add(tablePanel, "TABLE");
        masterlistContainer.add(createNewHireFormPanel(), "FORM");
        
        return masterlistContainer;
    }
private JPanel createNewHireFormPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
    mainPanel.setBackground(Color.WHITE);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    
    JPanel formContainer = new JPanel();
    formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
    formContainer.setOpaque(false);

    // --- Personal Info ---
    JPanel personalPanel = createFormSection("Personal Information", 0, 2);
    JTextField fName = new JTextField(); setupPlaceholder(fName, "Juan");
    JTextField lName = new JTextField(); setupPlaceholder(lName, "Delacruz");
    JTextField bday = new JTextField(); setupPlaceholder(bday, "MM/dd/yyyy");

    JButton btnPickBday = new JButton("📅 Pick Date");
    btnPickBday.addActionListener(e -> {
        DatePicker picker = new DatePicker(mainPanel); 
        String pickedValue = picker.setPickedDate(); 
        if (pickedValue != null && !pickedValue.isEmpty()) { 
            bday.setText(pickedValue); 
            bday.setForeground(Color.BLACK);
        }
    });

    JTextField address = new JTextField(); setupPlaceholder(address, "123 Street, City");
    JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
    JTextField phone = new JTextField(); applyFilters(5, phone); setupPlaceholder(phone, "000-000-000");

    addFormField(personalPanel, "First Name *", fName);
    addFormField(personalPanel, "Last Name *", lName);
    personalPanel.add(new JLabel("Birthday *"));
    JPanel bw = new JPanel(new BorderLayout()); bw.add(bday); bw.add(btnPickBday, BorderLayout.EAST);
    personalPanel.add(bw);
    addFormField(personalPanel, "Address:", address);
    addFormField(personalPanel, "Gender:", genderCombo); 
    addFormField(personalPanel, "Phone:", phone);

    // --- Identification ---
    JPanel govPanel = createFormSection("Identification & Status", 0, 2);
    JTextField sss = new JTextField(); applyFilters(6, sss); setupPlaceholder(sss, "00-0000000-0");
    JTextField phil = new JTextField(); applyFilters(7, phil); setupPlaceholder(phil, "000000000000");
    JTextField tin = new JTextField(); applyFilters(8, tin); setupPlaceholder(tin, "000-000-000-000");
    JTextField pagibig = new JTextField(); applyFilters(9, pagibig); setupPlaceholder(pagibig, "000000000000");
    JComboBox<String> status = new JComboBox<>(new String[]{"Regular", "Probationary"});
    
    addFormField(govPanel, "SSS #:", sss);
    addFormField(govPanel, "Philhealth #:", phil);
    addFormField(govPanel, "TIN #:", tin);
    addFormField(govPanel, "Pag-ibig #:", pagibig);
    addFormField(govPanel, "Status:", status);

    // --- Employment ---
    JPanel jobPanel = createFormSection("Employment", 0, 2);
    String[] positions = {"Chief Executive Officer", "Chief Operating Officer", "IT Operations and Systems", "HR Manager", "Accounting Head", "Sales & Marketing"};
    JComboBox<String> posCombo = new JComboBox<>(positions);
    JComboBox<String> supervCombo = new JComboBox<>(new String[]{"N/A"});
    posCombo.addActionListener(e -> {
        String[] svs = employeeManagementService.getSupervisorsForPosition((String)posCombo.getSelectedItem());
        supervCombo.removeAllItems();
        for (String s : svs) supervCombo.addItem(s);
    });
    addFormField(jobPanel, "Position:", posCombo);
    addFormField(jobPanel, "Supervisor:", supervCombo);

    // --- Financial ---
    JPanel financePanel = createFormSection("Financial Information", 0, 4);
    JTextField salary = new JTextField("0"); applyFilters(13, salary);
    JTextField rice = new JTextField("0"); applyFilters(14, rice);
    JTextField pallow = new JTextField("0"); applyFilters(15, pallow);
    JTextField cloth = new JTextField("0"); applyFilters(16, cloth);

    // Inside your createNewHireFormPanel, ensure this label is set:
addFormField(financePanel, "Hourly Rate:", salary);
    addFormField(financePanel, "Rice:", rice);
    addFormField(financePanel, "Phone:", pallow);
    addFormField(financePanel, "Clothing:", cloth);

    formContainer.add(personalPanel); formContainer.add(govPanel); formContainer.add(jobPanel); formContainer.add(financePanel);

    // --- SAVE LOGIC WITH VALIDATION ---
    JButton btnSave = new JButton("Confirm Hire");
    styleButton(btnSave, new Color(34, 139, 34));
    btnSave.addActionListener(e -> {
        
        // 1. STRICT VALIDATION CHECK
        boolean missingFields = 
            getActualValue(fName, "Juan").isEmpty() || 
            getActualValue(lName, "Delacruz").isEmpty() ||
            getActualValue(bday, "MM/dd/yyyy").isEmpty() ||
            getActualValue(address, "123 Street, City").isEmpty() ||
            getActualValue(phone, "000-000-000").isEmpty() ||
            getActualValue(sss, "00-0000000-0").isEmpty() ||
            getActualValue(phil, "000000000000").isEmpty() ||
            getActualValue(tin, "000-000-000-000").isEmpty() ||
            getActualValue(pagibig, "000000000000").isEmpty();

        if (missingFields) {
            JOptionPane.showMessageDialog(this, 
                "Please complete all required fields before proceeding.", 
                "Incomplete Information", 
                JOptionPane.WARNING_MESSAGE);
            return; // Stop the save process
        }

        // 2. DATA PREPARATION
        JTextField[] fields = new JTextField[18]; 
        fields[0] = new JTextField("0");
        fields[1] = lName; 
        fields[2] = fName;
        fields[3] = new JTextField(genderCombo.getSelectedItem().toString());
        fields[4] = bday; 
        fields[5] = address; 
        fields[6] = phone; 
        fields[7] = sss; 
        fields[8] = phil; 
        fields[9] = tin; 
        fields[10] = pagibig; 
        fields[11] = new JTextField(status.getSelectedItem().toString());
        fields[12] = new JTextField(posCombo.getSelectedItem().toString());
        fields[13] = new JTextField(supervCombo.getSelectedItem().toString());
        fields[14] = salary; 
        fields[15] = rice; 
        fields[16] = pallow; 
        fields[17] = cloth;

        // 3. EXECUTION
        if (employeeManagementService.updateEmployeeFromForm((Employee)currentUser, fields)) {
            JOptionPane.showMessageDialog(this, "Hire Successful!");
            
            // RESET FORM FOR NEXT USE
            resetForm(fields, new JComboBox[]{genderCombo, status, posCombo, supervCombo});
            
            refreshTable(); 
            masterlistLayout.show(masterlistContainer, "TABLE"); 
        }
    });

    JButton btnBack = new JButton("Cancel");
    btnBack.addActionListener(e -> masterlistLayout.show(masterlistContainer, "TABLE"));

    JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bp.setOpaque(false);
    bp.add(btnBack); bp.add(btnSave);
    
    mainPanel.add(new JScrollPane(formContainer), BorderLayout.CENTER);
    mainPanel.add(bp, BorderLayout.SOUTH);
    return mainPanel;
}


private double parseDouble(String val) {
    try {
        return Double.parseDouble(val.trim());
    } catch (Exception e) {
        return 0.0;
    }
}

    private void resetForm(JTextField[] fields, JComboBox<?>[] combos) {
    for (JTextField field : fields) {
        if (field != null) {
            field.setText("");
            // Re-apply placeholder color if you are using setupPlaceholder
            field.setForeground(Color.GRAY); 
        }
    }
    for (JComboBox<?> combo : combos) {
        if (combo != null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }
    // Specific defaults for salary fields if you want them to be "0"
    // fields[14], fields[15], fields[16], fields[17]
}

    // --- UTILITIES ---
    private void applyFilters(int index, JTextField field) {
        AbstractDocument doc = (AbstractDocument) field.getDocument();
        switch (index) {
            case 5: doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###")); break;
            case 6: doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#")); break;
            case 7: case 9: doc.setDocumentFilter(new util.NumericLimitFilter(12)); break;
            case 8: doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###-###")); break;
            case 13: case 14: case 15: case 16: doc.setDocumentFilter(new util.NumericLimitFilter(7)); break;
        }
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

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(INTER_BOLD);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupPlaceholder(JTextField field, String hint) {
        field.setText(hint); field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { 
                if (field.getText().equals(hint)) { field.setText(""); field.setForeground(Color.BLACK); } 
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) { 
                if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(hint); } 
            }
        });
    }

    private String getActualValue(JTextField field, String hint) {
        String val = field.getText().trim();
        return val.equals(hint) ? "" : val;
    }

    private void addFormField(JPanel p, String label, JComponent f) {
        JLabel lbl = new JLabel(label); lbl.setFont(INTER_BOLD);
        p.add(lbl); p.add(f);
    }

    private JPanel createFormSection(String title, int rows, int cols) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));
        p.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
        border.setTitleFont(INTER_BOLD);
        p.setBorder(border);
        return p;
    }

    // --- REINSTATED ORIGINAL DATE PICKER LOGIC ---
    public class DatePicker {
        private int month, year;
        private int dayInt = 0; 
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

            JPanel p1 = new JPanel(new GridLayout(6, 7));
            String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton();
                if (x < 7) {
                    button[x].setText(header[x]);
                    button[x].setForeground(Color.RED);
                    button[x].setEnabled(false);
                } else {
                    button[x].addActionListener(e -> {
                        String btnText = button[selection].getText();
                        if (!btnText.isEmpty()) {
                            dayInt = Integer.parseInt(btnText);
                            d.dispose(); 
                        }
                    });
                }
                p1.add(button[x]);
            }

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
            d.setVisible(true); // Blocks until closed
        }

        public void displayDate() {
            for (int x = 7; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 6 + dayOfWeek, dayNum = 1; dayNum <= daysInMonth; x++, dayNum++) {
                if (x < button.length) button[x].setText("" + dayNum);
            }
        }

        public String setPickedDate() {
            if (dayInt == 0) return ""; 
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayInt); 
            return sdf.format(cal.getTime());
        }
    }
}