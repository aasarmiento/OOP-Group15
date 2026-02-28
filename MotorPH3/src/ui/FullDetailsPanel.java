package ui;

import java.awt.*;
import javax.swing.*;
import model.Employee;
import service.EmployeeSerbisyo;


public class FullDetailsPanel extends JPanel {
    private final EmployeeSerbisyo service = new EmployeeSerbisyo();
    private String employeeNum;

    private final JTextField empNo = UIUtils.createTextField(false);
    private final JTextField lastName = UIUtils.createTextField(true);
    private final JTextField firstName = UIUtils.createTextField(true);
    private final JTextField position = UIUtils.createTextField(true);
    private final JTextField basicSalary = UIUtils.createTextField(true);
    private final JLabel lblNetPay = new JLabel("Net Salary: ₱0.00");
    private final JComboBox<String> comboMonth = new JComboBox<>(new String[]{"JANUARY 2022", "FEBRUARY 2022", "MARCH 2022"});

    public FullDetailsPanel() {
        setLayout(new BorderLayout());
        setupLayout();
    }

    public FullDetailsPanel(String employeeNo) {
        this();
        this.employeeNum = employeeNo;
        refreshData();
    }
    
    
    public void setEmployeeNo(String employeeNo) {
        this.employeeNum = employeeNo;
        refreshData();
    }

    private void setupLayout() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        formPanel.add(new JLabel("Employee #:")); formPanel.add(empNo);
        formPanel.add(new JLabel("Last Name:")); formPanel.add(lastName);
        formPanel.add(new JLabel("First Name:")); formPanel.add(firstName);
        formPanel.add(new JLabel("Position:")); formPanel.add(position);
        formPanel.add(new JLabel("Basic Salary:")); formPanel.add(basicSalary);

        JButton btnCalculate = new JButton("Calculate Monthly Pay");
        btnCalculate.addActionListener(e -> handleCalculation());

        add(formPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel();
        southPanel.add(comboMonth);
        southPanel.add(btnCalculate);
        southPanel.add(lblNetPay);
        add(southPanel, BorderLayout.SOUTH);
    }

   
    public final void refreshData() {
        if (employeeNum == null || employeeNum.isEmpty()) return;
        
        try {
            int id = Integer.parseInt(this.employeeNum);
            Employee emp = service.findById(id); 

            if (emp != null) {
                empNo.setText(String.valueOf(emp.getEmpNo()));
                lastName.setText(emp.getLastName());
                firstName.setText(emp.getFirstName());
                position.setText(emp.getPosition());
                basicSalary.setText(String.format("%.2f", emp.getBasicSalary()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid Employee ID format");
        }
    }

    private void handleCalculation() {
        if (employeeNum == null) return;
        
        int id = Integer.parseInt(this.employeeNum);
        String selectedMonth = (String) comboMonth.getSelectedItem();

        double result = service.calculateMonthlySalary(id, selectedMonth);
        lblNetPay.setText(String.format("Net Salary: ₱%,.2f", result));
    }
}