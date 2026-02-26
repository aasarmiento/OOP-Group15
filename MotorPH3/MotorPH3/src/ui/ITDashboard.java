package ui;

import dao.EmployeeDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;

public class ITDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    
    // UI Components matching your screenshot layout
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;

    public ITDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;

        setTitle("MotorPH IT Portal - " + user.getFirstName());
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar - Exact Red Branding from your screenshot
        add(createSidebar(), BorderLayout.WEST);

        // 2. Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(createTablePanel(), BorderLayout.CENTER);
        mainContent.add(createBottomForm(), BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);

        // Logic: Load filtered data so it's not full of everyone's info
        refreshTableData();
    }

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0)); // The specific Dark Red in your screenshot
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

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

        JLabel portalTitle = new JLabel("IT Portal");
        portalTitle.setForeground(Color.WHITE);
        portalTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        portalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(portalTitle);

        nav.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Log out");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(190, 40));
        btnLogout.addActionListener(e -> {
            new LoginPanel().setVisible(true); 
            this.dispose();
        });
        
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));
        return nav;
    }

    private JPanel createTablePanel() {
        // Restricted columns - Salary/Gross Rate REMOVED for IT security
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(tableModel);
        
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = userTable.getSelectedRow();
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
            add(new JScrollPane(userTable), BorderLayout.CENTER);
        }};
    }

    private JPanel createBottomForm() {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10)); // Matches 2-column look in screenshot
        form.setBorder(BorderFactory.createTitledBorder("Employee Information"));
        
        txtEmpNo = new JTextField(); txtLastName = new JTextField();
        txtFirstName = new JTextField(); txtStatus = new JTextField();
        txtPosition = new JTextField(); txtSupervisor = new JTextField();

        form.add(new JLabel("EmployeeNo:")); form.add(txtEmpNo);
        form.add(new JLabel("LastName:")); form.add(txtLastName);
        form.add(new JLabel("FirstName:")); form.add(txtFirstName);
        form.add(new JLabel("Status:")); form.add(txtStatus);
        form.add(new JLabel("Position:")); form.add(txtPosition);
        form.add(new JLabel("Supervisor:")); form.add(txtSupervisor);

        return form;
    }

    public final void refreshTableData() {
        tableModel.setRowCount(0);
        
        // This is the "Empty" logic from Accounting: 
        // Only show the logged-in user's record or their subordinates
        String myName = currentUser.getLastName() + ", " + currentUser.getFirstName();
        
        for (Employee emp : employeeDao.getAll()) {
            // FILTER: Show ONLY Eduard's own record to keep it "empty" like Roderick's
            if (emp.getEmpNo() == currentUser.getEmpNo() || 
               (emp.getSupervisor() != null && emp.getSupervisor().equalsIgnoreCase(myName))) {
                
                tableModel.addRow(new Object[]{
                    emp.getEmpNo(), 
                    emp.getLastName(), 
                    emp.getFirstName(),
                    emp.getStatus(), 
                    emp.getPosition(),
                    emp.getSupervisor()
                });
            }
        }
    }
}