package ui;

import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.*;
import model.AttendanceSummary;
import model.Employee;
import model.PayrollBreakdown;
import service.EmployeeManagementService;
import service.PayrollCalculator;
import service.PayrollService;

public class MyPayslip extends JPanel {
    private final EmployeeManagementService service;
    private final PayrollCalculator calc;
    private final PayrollService payrollService;
    private final Employee currentUser;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private final Color MOTORPH_MAROON = new Color(128, 0, 0);
    private final Color BG_LIGHT = new Color(245, 245, 245);
    private final Color BORDER_COLOR = new Color(225, 225, 225);
    private final Color DIRT_WHITE = new Color(245, 245, 240);

    private final Font sectionFont = new Font("DM Sans Bold", Font.BOLD, 14);
    private final Font labelFont = new Font("DM Sans Regular", Font.PLAIN, 13);
    private final Font valueFont = new Font("DM Sans Medium", Font.BOLD, 13);

    private JComboBox<String> monthPicker, yearPicker;
    private JPanel paper;

    private JLabel lblGross, lblSss, lblPhilhealth, lblTax, lblPagibig, lblNetPay;
    private JLabel lblEmployeeId, lblEmployeeName, lblPosition, lblStatus;

    private JLabel lblExpectedDays, lblDaysPresent, lblAbsentDays;
    private JLabel lblLateMinutes, lblUndertimeMinutes;
    private JLabel lblLateDeduction, lblUndertimeDeduction, lblAbsenceDeduction, lblAttendanceDeduction;

    private JLabel lblBasic, lblAdjustedBasic, lblAllowances, lblTaxableIncome;

    public MyPayslip(EmployeeManagementService service, PayrollCalculator calc, PayrollService payrollService, Employee user) {
        this.service = service;
        this.calc = calc;
        this.payrollService = payrollService;
        this.currentUser = user;

        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        add(createPayslipHeader(), BorderLayout.NORTH);

        paper = createPayslipContent();

        JPanel centeringWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        centeringWrapper.setBackground(BG_LIGHT);
        centeringWrapper.add(paper);

        JScrollPane scrollPane = new JScrollPane(centeringWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        calculateSalary();
    }

    public MyPayslip(EmployeeManagementService service, PayrollCalculator calc, PayrollService payrollService,
                 Employee user, String selectedMonth, String selectedYear) {
    this(service, calc, payrollService, user);
    monthPicker.setSelectedItem(selectedMonth);
    yearPicker.setSelectedItem(selectedYear);
    calculateSalary();
    }



    private JPanel createPayslipHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JLabel lblPeriod = new JLabel("Period: ");
        lblPeriod.setFont(new Font("DM Sans Bold", Font.BOLD, 12));

        monthPicker = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        monthPicker.setFont(labelFont);
        monthPicker.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);

        yearPicker = new JComboBox<>(new String[]{"2024", "2025", "2026"});
        yearPicker.setFont(labelFont);
        yearPicker.setSelectedItem("2024");

        JButton btnCalculate = new JButton("Generate Payslip") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(MOTORPH_MAROON.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(150, 0, 0));
                } else {
                    g2.setColor(MOTORPH_MAROON);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btnCalculate.setFont(new Font("DM Sans Bold", Font.BOLD, 12));
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setPreferredSize(new Dimension(160, 36));
        btnCalculate.setContentAreaFilled(false);
        btnCalculate.setBorderPainted(false);
        btnCalculate.setFocusPainted(false);
        btnCalculate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalculate.addActionListener(e -> calculateSalary());

        JButton btnPrint = new JButton("Print PDF");
        btnPrint.setBackground(Color.WHITE);
        btnPrint.setForeground(MOTORPH_MAROON);
        btnPrint.setFocusPainted(false);
        btnPrint.setFont(new Font("DM Sans Bold", Font.BOLD, 12));
        btnPrint.setBorder(BorderFactory.createLineBorder(MOTORPH_MAROON, 1));
        btnPrint.setPreferredSize(new Dimension(100, 32));
        btnPrint.addActionListener(e -> printToPDF());

        header.add(lblPeriod);
        header.add(monthPicker);
        header.add(yearPicker);
        header.add(Box.createHorizontalStrut(15));
        header.add(btnCalculate);
        header.add(Box.createHorizontalStrut(5));
        header.add(btnPrint);

        return header;
    }

private void printToPDF() {
    JOptionPane.showMessageDialog(this, "Exporting to PDF... (Functionality to be linked with PDF Library)");
}

private JPanel createPayslipContent() {
    JPanel paperPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
        }
    };

    paperPanel.setLayout(new BoxLayout(paperPanel, BoxLayout.Y_AXIS));
    paperPanel.setPreferredSize(new Dimension(800, 1100));
    paperPanel.setBackground(Color.WHITE);
    paperPanel.setOpaque(false);
    paperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

    JPanel headerBrand = new JPanel();
    headerBrand.setLayout(new BoxLayout(headerBrand, BoxLayout.Y_AXIS));
    headerBrand.setOpaque(true);
    headerBrand.setBackground(DIRT_WHITE);
    headerBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
    headerBrand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    headerBrand.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

    JLabel title = new JLabel("PAYSLIP OVERVIEW");
    title.setFont(new Font("DM Sans Bold", Font.BOLD, 22));
    title.setForeground(MOTORPH_MAROON);
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    headerBrand.add(title);

    paperPanel.add(headerBrand);

    JPanel bodyPadding = new JPanel();
    bodyPadding.setLayout(new BoxLayout(bodyPadding, BoxLayout.Y_AXIS));
    bodyPadding.setOpaque(false);
    bodyPadding.setBorder(BorderFactory.createEmptyBorder(25, 50, 20, 50));
    bodyPadding.setAlignmentX(Component.CENTER_ALIGNMENT);

    JSeparator sep = new JSeparator();
    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    bodyPadding.add(sep);
    bodyPadding.add(Box.createVerticalStrut(25));

    addSectionHeader(bodyPadding, "EMPLOYEE INFORMATION");
    lblEmployeeId = createLabeledRow(bodyPadding, "Employee #:");
    lblEmployeeName = createLabeledRow(bodyPadding, "Full Name:");
    lblPosition = createLabeledRow(bodyPadding, "Position:");
    lblStatus = createLabeledRow(bodyPadding, "Employment Status:");
    bodyPadding.add(Box.createVerticalStrut(20));

    addSectionHeader(bodyPadding, "ATTENDANCE SUMMARY");
    lblExpectedDays = createLabeledRow(bodyPadding, "Expected Work Days:");
    lblDaysPresent = createLabeledRow(bodyPadding, "Days Present:");
    lblAbsentDays = createLabeledRow(bodyPadding, "Absent Days:");
    lblLateMinutes = createLabeledRow(bodyPadding, "Late Minutes:");
    lblUndertimeMinutes = createLabeledRow(bodyPadding, "Undertime Minutes:");
    lblLateDeduction = createLabeledRow(bodyPadding, "Late Deduction:");
    lblUndertimeDeduction = createLabeledRow(bodyPadding, "Undertime Deduction:");
    lblAbsenceDeduction = createLabeledRow(bodyPadding, "Absence Deduction:");
    lblAttendanceDeduction = createLabeledRow(bodyPadding, "Total Attendance Deduction:");
    bodyPadding.add(Box.createVerticalStrut(20));

    addSectionHeader(bodyPadding, "EARNINGS");
    lblBasic = createLabeledRow(bodyPadding, "Monthly Basic Salary:");
    lblAdjustedBasic = createLabeledRow(bodyPadding, "Adjusted Basic Pay:");
    lblAllowances = createLabeledRow(bodyPadding, "Total Monthly Allowances:");
    lblGross = createLabeledRow(bodyPadding, "Gross Pay (Adjusted):");
    bodyPadding.add(Box.createVerticalStrut(20));

    addSectionHeader(bodyPadding, "STATUTORY DEDUCTIONS & TAX");
    lblSss = createLabeledRow(bodyPadding, "SSS Contribution:");
    lblPhilhealth = createLabeledRow(bodyPadding, "PhilHealth Contribution:");
    lblPagibig = createLabeledRow(bodyPadding, "Pag-IBIG Contribution:");
    lblTaxableIncome = createLabeledRow(bodyPadding, "Computed Taxable Income:");
    lblTax = createLabeledRow(bodyPadding, "Withholding Tax:");
    bodyPadding.add(Box.createVerticalStrut(20));

    JPanel netPayPanel = new JPanel(new BorderLayout());
    netPayPanel.setOpaque(true);
    netPayPanel.setBackground(new Color(248, 248, 248));
    netPayPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    netPayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    JLabel netLabel = new JLabel("TOTAL NET PAY");
    netLabel.setFont(sectionFont);

    lblNetPay = new JLabel("PHP 0.00");
    lblNetPay.setFont(new Font("DM Sans Bold", Font.BOLD, 24));
    lblNetPay.setForeground(MOTORPH_MAROON);

    netPayPanel.add(netLabel, BorderLayout.WEST);
    netPayPanel.add(lblNetPay, BorderLayout.EAST);

    bodyPadding.add(netPayPanel);
    paperPanel.add(bodyPadding);

    return paperPanel;
}


private void addSectionHeader(JPanel parent, String text) {
    JLabel header = new JLabel(text);
    header.setFont(sectionFont);
    header.setForeground(MOTORPH_MAROON);
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    parent.add(header);
    parent.add(Box.createVerticalStrut(10));
}

private JLabel createLabeledRow(JPanel parent, String labelText) {
    JPanel row = new JPanel(new BorderLayout());
    row.setOpaque(false);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    row.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(labelText);
    label.setFont(labelFont);
    label.setForeground(Color.GRAY);

    JLabel value = new JLabel("0.00");
    value.setFont(valueFont);
    value.setForeground(Color.BLACK);
    value.setHorizontalAlignment(SwingConstants.RIGHT);

    row.add(label, BorderLayout.WEST);
    row.add(value, BorderLayout.EAST);
    row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)));

    parent.add(row);
    parent.add(Box.createVerticalStrut(5));
    return value;
}


public final void calculateSalary() {
    try {
        String month = (String) monthPicker.getSelectedItem();
        String year = (String) yearPicker.getSelectedItem();

        AttendanceSummary attendance = payrollService.summarizeAttendance(
            currentUser.getEmpNo(),
            month,
            year
        );

        PayrollBreakdown breakdown = calc.calculateBreakdown(currentUser, attendance);

        double lateDeduction = calc.calculateLateDeduction(currentUser, attendance.getLateMinutes());
        double totalAllowances = breakdown.getTotalAllowances();

        lblEmployeeId.setText(String.valueOf(currentUser.getEmpNo()));
        lblEmployeeName.setText(currentUser.getFullName());
        lblPosition.setText(currentUser.getPosition() != null ? currentUser.getPosition() : "N/A");
        lblStatus.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "N/A");

        lblDaysPresent.setText(String.valueOf(attendance.getPresentDays()));
        lblLateMinutes.setText(String.valueOf(attendance.getLateMinutes()));
        lblLateDeduction.setText(df.format(lateDeduction));

        lblBasic.setText(df.format(currentUser.getBasicSalary()));
        lblAllowances.setText(df.format(totalAllowances));
        lblGross.setText(df.format(breakdown.getGrossPay()));

        lblSss.setText(df.format(breakdown.getSss()));
        lblPhilhealth.setText(df.format(breakdown.getPhilhealth()));
        lblPagibig.setText(df.format(breakdown.getPagibig()));
        lblTaxableIncome.setText(df.format(breakdown.getTaxableIncome()));
        lblTax.setText(df.format(breakdown.getWithholdingTax()));
        lblNetPay.setText("PHP " + df.format(breakdown.getNetPay()));

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error calculating: " + ex.getMessage());
        ex.printStackTrace();
        }
    }
}