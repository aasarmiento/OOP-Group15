package ui;

import dao.EmployeeDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;

public class AccountingDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    
    // UI Components
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;

    public AccountingDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;

        setTitle("MotorPH Accounting Portal - " + user.getFirstName());
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main Content Area
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(createTablePanel(), BorderLayout.CENTER);
        mainContent.add(createBottomForm(), BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);

        // Logic: Load data (Calling final method is safe in constructor)
        refreshFilteredTable();
    }
private JPanel createSidebar() {
    JPanel nav = new JPanel();
    nav.setBackground(new Color(128, 0, 0)); // Dark Red matching your screenshot
    nav.setPreferredSize(new Dimension(220, getHeight()));
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

    // Top Section: Welcome Text
    nav.add(Box.createVerticalStrut(30));
    JLabel welcomeLabel = new JLabel("Welcome to MOTORPH,");
    welcomeLabel.setForeground(Color.WHITE);
    welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
    welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nav.add(welcomeLabel);

    JLabel nameLabel = new JLabel(currentUser.getFirstName() + "!");
    nameLabel.setForeground(Color.WHITE);
    nameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nav.add(nameLabel);

    nav.add(Box.createVerticalStrut(10));

    JLabel portalTitle = new JLabel("Accounting Portal");
    portalTitle.setForeground(Color.WHITE);
    portalTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
    portalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
    nav.add(portalTitle);

    // This pushes everything below it to the bottom of the sidebar
    nav.add(Box.createVerticalGlue());

    // Logout Button
    JButton btnLogout = new JButton("Log out");
    btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnLogout.setMaximumSize(new Dimension(190, 40)); // Matches Admin button size
    btnLogout.setFocusable(false);
    btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    btnLogout.addActionListener(e -> {
        // Return to login screen
        new LoginPanel().setVisible(true); 
        this.dispose();
    });
    
    nav.add(btnLogout);
    nav.add(Box.createVerticalStrut(20)); // Margin at the very bottom

    return nav;
}

    private JPanel createTablePanel() {
        // Added 'Status' to columns to match the text fields below
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor", "Basic Salary", "Gross Rate"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        employeeTable = new JTable(tableModel);
        
        // Selection listener to fill ALL text fields including txtStatus
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = employeeTable.getSelectedRow();
                if (row != -1) {
                    txtEmpNo.setText(tableModel.getValueAt(row, 0).toString());
                    txtLastName.setText(tableModel.getValueAt(row, 1).toString());
                    txtFirstName.setText(tableModel.getValueAt(row, 2).toString());
                    txtStatus.setText(tableModel.getValueAt(row, 3).toString());
                    txtPosition.setText(tableModel.getValueAt(row, 4).toString());
                    txtSupervisor.setText(tableModel.getValueAt(row, 5).toString());
                }
            }
        });

        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        }};
    }

    private JPanel createBottomForm() {
        // Changed to 3 rows, 4 columns for a cleaner horizontal look
        JPanel form = new JPanel(new GridLayout(3, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Employee Information"));
        
        // Initialize ALL fields
        txtEmpNo = new JTextField(); 
        txtLastName = new JTextField();
        txtFirstName = new JTextField(); 
        txtStatus = new JTextField(); // Initialized to fix "not used" error
        txtPosition = new JTextField(); 
        txtSupervisor = new JTextField();

        // Add ALL fields to the layout
        form.add(new JLabel("EmployeeNo:")); form.add(txtEmpNo);
        form.add(new JLabel("LastName:")); form.add(txtLastName);
        form.add(new JLabel("FirstName:")); form.add(txtFirstName);
        form.add(new JLabel("Status:")); form.add(txtStatus); // Added to UI
        form.add(new JLabel("Position:")); form.add(txtPosition);
        form.add(new JLabel("Supervisor:")); form.add(txtSupervisor);

        return form;
    }

    // FIX: Method is 'final' to prevent "Overridable method call in constructor" error
    public final void refreshFilteredTable() {
        tableModel.setRowCount(0);
        
        String myNameInCSV = currentUser.getLastName() + ", " + currentUser.getFirstName();
        
        for (Employee emp : employeeDao.getAll()) {
            // FILTER: Show subordinates OR self record
            if (emp.getSupervisor().equalsIgnoreCase(myNameInCSV) || emp.getEmpNo() == currentUser.getEmpNo()) {
                tableModel.addRow(new Object[]{
                    emp.getEmpNo(), 
                    emp.getLastName(), 
                    emp.getFirstName(),
                    emp.getStatus(), // Added data for Status column
                    emp.getPosition(), 
                    emp.getSupervisor(), 
                    emp.getBasicSalary(), 
                    emp.getGrossRate()
                });
            }
        }
    }
}