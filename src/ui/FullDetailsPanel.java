package ui;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import model.Employee;
import service.EmployeeManagementService;

public class FullDetailsPanel extends JPanel {
    private final EmployeeManagementService service;
    private final Employee employee;

    // UI Components - Declarations
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;
    private JLabel lblProfilePic;

    public FullDetailsPanel(EmployeeManagementService service, Employee currentUser) {
        // 1. Assign final fields
        this.service = service;
        this.employee = currentUser;

        // 2. Set the Layout of THIS panel
        this.setLayout(new BorderLayout());

        // 3. Build the UI and ADD it (This initializes the TextFields)
        this.add(createHomePanel(), BorderLayout.CENTER); 

        // 4. Fill with data (Only now that TextFields exist)
        if (this.employee != null) {
            loadPersonalDetails(this.employee);
        }
    }

    private void loadPersonalDetails(Employee emp) {
        if (emp == null || txtEmpNo == null) return; 

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        // Identity Info
        txtEmpNo.setText(String.valueOf(emp.getEmpNo()));
        txtLastName.setText(emp.getLastName());
        txtFirstName.setText(emp.getFirstName());
        txtStatus.setText(emp.getStatus());
        txtPosition.setText(emp.getPosition());
        txtSupervisor.setText(emp.getSupervisor());

        // Dates and Contact
        if (emp.getBirthday() != null) {
            txtBirthday.setText(emp.getBirthday().format(dateFormatter));
        }
        
        txtAddress.setText(emp.getAddress());
        txtPhone.setText(emp.getPhone());
        txtSss.setText(emp.getSss());
        txtPhilHealth.setText(emp.getPhilhealth());
        txtTin.setText(emp.getTin());
        txtPagibig.setText(emp.getPagibig());

        // Financial Data
        txtSalary.setText(String.format("%.2f", emp.getBasicSalary()));
        txtRice.setText(String.format("%.2f", emp.getRiceSubsidy()));
        txtPhoneAllowance.setText(String.format("%.2f", emp.getPhoneAllowance()));
        txtClothing.setText(String.format("%.2f", emp.getClothingAllowance()));
        txtGross.setText(String.format("%.2f", emp.getGrossRate()));
        txtHourly.setText(String.format("%.2f", emp.getHourlyRate()));

        // Photo Display
        displayEmployeePhoto(lblProfilePic);
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 1. Info Section
        JPanel infoPanel = new JPanel(new BorderLayout(15, 0));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Employee Information"));

        lblProfilePic = new JLabel("No Image");
        lblProfilePic.setPreferredSize(new Dimension(150, 150));
        lblProfilePic.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblProfilePic.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(lblProfilePic, BorderLayout.WEST);

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        txtEmpNo = addField(fieldsPanel, "Employee No:");
        txtLastName = addField(fieldsPanel, "Last Name:");
        txtFirstName = addField(fieldsPanel, "First Name:");
        txtStatus = addField(fieldsPanel, "Status:");
        txtPosition = addField(fieldsPanel, "Position:");
        txtSupervisor = addField(fieldsPanel, "Supervisor:");
        infoPanel.add(fieldsPanel, BorderLayout.CENTER);

        // 2. Personal Section
        JPanel personalPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        personalPanel.setBorder(BorderFactory.createTitledBorder("Personal Details"));
        txtBirthday = addField(personalPanel, "Birthday:");
        txtAddress = addField(personalPanel, "Address:");
        txtPhone = addField(personalPanel, "Phone:");
        txtSss = addField(personalPanel, "SSS:");
        txtPhilHealth = addField(personalPanel, "PhilHealth:");
        txtTin = addField(personalPanel, "TIN:");
        txtPagibig = addField(personalPanel, "Pag-IBIG:");

        // 3. Financial Section
        JPanel financePanel = new JPanel(new GridLayout(3, 2, 10, 5));
        financePanel.setBorder(BorderFactory.createTitledBorder("Financial Information"));
        txtSalary = addField(financePanel, "Basic Salary:");
        txtRice = addField(financePanel, "Rice Subsidy:");
        txtPhoneAllowance = addField(financePanel, "Phone Allowance:");
        txtClothing = addField(financePanel, "Clothing Allowance:"); // Fixed: Added this back
        txtGross = addField(financePanel, "Gross Rate:");
        txtHourly = addField(financePanel, "Hourly Rate:");

        // 4. Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnUpdate = new JButton("Update Info");
        btnUpdate.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Update functionality coming soon.");
        });
        actionPanel.add(btnUpdate);

        mainPanel.add(infoPanel);
        mainPanel.add(personalPanel);
        mainPanel.add(financePanel);
        mainPanel.add(actionPanel);

        return mainPanel;
    }

    private JTextField addField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField();
        field.setEditable(false); 
        panel.add(field);
        return field;
    }

    private void displayEmployeePhoto(JLabel photoLabel) {
        if (photoLabel == null || txtEmpNo == null) return;
        try {
            String imagePath = "resources/photos/" + txtEmpNo.getText() + ".png";
            java.io.File file = new java.io.File(imagePath);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(img));
                photoLabel.setText("");
            } else {
                photoLabel.setIcon(null);
                photoLabel.setText("No Photo");
            }
        } catch (Exception e) {
            photoLabel.setText("Error");
        }
    }
}