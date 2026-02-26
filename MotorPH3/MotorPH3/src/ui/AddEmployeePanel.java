package ui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import dao.EmployeeDAO; 

public class AddEmployeePanel extends JPanel {

   
    private final EmployeeDAO dao;

  
    private final JTextField empNo = new JTextField();
    private final JTextField lastName = new JTextField();
    private final JTextField firstName = new JTextField();
    private final JTextField status = new JTextField();
    private final JTextField position = new JTextField();
    private final JTextField supervisor = new JTextField();
    private final JTextField birthday = new JTextField();
    private final JTextField address = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField sss = new JTextField();
    private final JTextField philhealth = new JTextField();
    private final JTextField tin = new JTextField();
    private final JTextField pagibig = new JTextField();
    private final JTextField basicSalary = new JTextField();
    private final JTextField riceSubsidy = new JTextField();
    private final JTextField phoneAllowance = new JTextField();
    private final JTextField clothingAllowance = new JTextField();
    private final JTextField grossRate = new JTextField();
    private final JTextField hourlyRate = new JTextField();

    public AddEmployeePanel(EmployeeDAO dao) {
        this.dao = dao;
        
        setLayout(new BorderLayout());

        // Header
        JLabel addHeading = UIUtils.createHeaderLabel("Add Employee");
        add(addHeading, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttons = new JPanel();
        JButton btnAddEmployee = UIUtils.createButton("Add New Employee", new Color(0, 180, 0), Color.WHITE);
        JButton btnClear = UIUtils.createButton("Clear", Color.GRAY, Color.WHITE);
        buttons.add(btnAddEmployee);
        buttons.add(btnClear);

        // Form Panels
        JPanel formPanel = new JPanel(new GridLayout(3, 1, 5, 2));
        formPanel.add(UIUtils.createEmployeeInfoPanel(empNo, lastName, firstName, status, position, supervisor));
        formPanel.add(UIUtils.createPersonalInfoPanel(birthday, address, phone, sss, philhealth, tin, pagibig));
        formPanel.add(UIUtils.createFinancialInfoPanel(basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossRate, hourlyRate));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add to Main Panel
        add(buttons, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.CENTER);

        // Logic Initialization
        empNo.setEditable(false);
        setNextEmployeeNumber();

        // Listeners
        btnAddEmployee.addActionListener(e -> addEmployeeToCSV());
        btnClear.addActionListener(e -> clearFields());
    }

    private void clearFields() {
        lastName.setText("");
        firstName.setText("");
        status.setText("");
        position.setText("");
        supervisor.setText("");
        birthday.setText("");
        address.setText("");
        phone.setText("");
        sss.setText("");
        philhealth.setText("");
        tin.setText("");
        pagibig.setText("");
        basicSalary.setText("");
        riceSubsidy.setText("");
        phoneAllowance.setText("");
        clothingAllowance.setText("");
        grossRate.setText("");
        hourlyRate.setText("");
    }

    private void setNextEmployeeNumber() {
        empNo.setText(String.valueOf(getLastEmployeeNumber() + 1));
    }

    private int getLastEmployeeNumber() {
        
        int maxEmpNo = 10000;
        String path = "resources/MotorPH_EmployeeData.csv"; 
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length > 0) {
                    try {
                        int currentNo = Integer.parseInt(parts[0].trim());
                        if (currentNo > maxEmpNo) maxEmpNo = currentNo;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Note: Could not read CSV for next ID.");
        }
        return maxEmpNo;
    }

    private void addEmployeeToCSV() {
        if (lastName.getText().trim().isEmpty() || firstName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Name.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] newData = {
            empNo.getText(), lastName.getText(), firstName.getText(), birthday.getText(),
            address.getText(), phone.getText(), sss.getText(), phil