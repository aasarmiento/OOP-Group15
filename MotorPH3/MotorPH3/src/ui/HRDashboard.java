package ui;

import dao.EmployeeDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;

public class HRDashboard extends JFrame {

    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    
    // Form Fields
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;

    public HRDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;

        setTitle("MotorPH HR Portal - " + currentUser.getFirstName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(createEmployeeDirectory(), BorderLayout.CENTER);
        mainContent.add(createBottomForm(), BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);
        
        // Final call to populate table
        refreshTableData();
    }

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0));
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        
        JLabel lblName = new JLabel("Welcome, " + currentUser.getFirstName() + "!");
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(lblName);

        nav.add(Box.createVerticalStrut(40));

        JButton btnAddEmp = createSidebarButton("Add New Employee");
        JButton btnLeaveReq = createSidebarButton("Leave Request");
        
        nav.add(btnAddEmp);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnLeaveReq);

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

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 40));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createEmployeeDirectory() {
        JPanel panel = new JPanel(new BorderLayout());
        // COLUMN ORDER: 0:ID, 1:Last, 2:First, 3:Status, 4:Position, 5:Supervisor
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        employeeTable = new JTable(tableModel);
        
        // Selection Listener
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Prevents double-firing
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

        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomForm() {
        JPanel form = new JPanel(new GridLayout(3, 4, 10, 10));
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

    // This method is now final to avoid constructor warnings and matches table columns
    public final void refreshTableData() {
        tableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAll()) {
            tableModel.addRow(new Object[]{
                emp.getEmpNo(),         // Index 0
                emp.getLastName(),      // Index 1
                emp.getFirstName(),     // Index 2
                emp.getStatus(),        // Index 3
                emp.getPosition(),      // Index 4
                emp.getSupervisor()     // Index 5
            });
        }
    }
}