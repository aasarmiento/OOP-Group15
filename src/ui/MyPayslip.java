package ui;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import model.Attendance;
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

    private JComboBox<String> monthPicker, yearPicker;
    private JLabel lblGross, lblSss, lblPhilhealth, lblTax, lblPagibig, lblNetPay;
    private JLabel lblEmployeeId, lblEmployeeName, lblPosition, lblStatus, lblDaysPresent;
    private JLabel lblBasic, lblAllowances, lblLateMinutes, lblLateDeduction, lblTaxableIncome;

    public MyPayslip(EmployeeManagementService service, PayrollCalculator calc, PayrollService payrollService, Employee user) {
        

        this.service = service;
        this.calc = calc;
        this.payrollService = payrollService;
        this.currentUser = user;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header with Controls
        add(createPayslipHeader(), BorderLayout.NORTH);

        // Main Content (Vertical List)
        JPanel contentContainer = createPayslipContent();
        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Initial Calculation
        calculateSalary();
    }



    private JPanel createPayslipHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setBackground(new Color(245, 245, 245));

        monthPicker = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });

        yearPicker = new JComboBox<>(new String[]{"2024", "2025", "2026"});
        yearPicker.setSelectedItem("2026");

        JButton btnCalculate = new JButton("Calculate Salary");
        btnCalculate.addActionListener(e -> calculateSalary());

        // New Print Button
        JButton btnPrint = new JButton("Print Record");
        btnPrint.addActionListener(e -> printRecord());

        header.add(new JLabel("Month:"));
        header.add(monthPicker);
        header.add(new JLabel("Year:"));
        header.add(yearPicker);
        header.add(btnCalculate);
        header.add(btnPrint); // Added to UI

        return header;
    }

    private void printRecord() {
        // We create a PrinterJob
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Payslip - " + lblEmployeeName.getText());

        job.setPrintable((Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) -> {
            if (pageIndex > 0) {
                return java.awt.print.Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            // Move to the printable area
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            // Scale the content to fit the width of the page if it's too large
            double pageWidth = pageFormat.getImageableWidth();
            double panelWidth = this.getWidth();
            double scale = pageWidth / panelWidth;
            if (scale < 1.0) {
                g2d.scale(scale, scale);
            }

            // Paint the panel (this) onto the graphics object
            // Note: We hide the header controls during print for a cleaner look
            this.paint(g2d);

            return java.awt.print.Printable.PAGE_EXISTS;
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (java.awt.print.PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage());
            }
        }
    }

   private JPanel createPayslipContent() {
        JPanel mainContent = new JPanel(new GridBagLayout()); // Changed to GridBagLayout
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = -1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0); // Vertical spacing between rows

        // 1. Employee Information
        addSectionHeader(mainContent, "Employee Information", gbc);
        lblEmployeeId = createLabeledRow(mainContent, "Employee #:", gbc);
        lblEmployeeName = createLabeledRow(mainContent, "Name:", gbc);
        lblPosition = createLabeledRow(mainContent, "Position:", gbc);
        lblStatus = createLabeledRow(mainContent, "Status:", gbc);
        addSpacer(mainContent, gbc);

        // 2. Attendance Summary
        addSectionHeader(mainContent, "Attendance Summary (10-min grace)", gbc);
        lblDaysPresent = createLabeledRow(mainContent, "Days Present:", gbc);
        lblLateMinutes = createLabeledRow(mainContent, "Late Minutes:", gbc);
        lblLateDeduction = createLabeledRow(mainContent, "Late Deduction:", gbc);
        addSpacer(mainContent, gbc);

        // 3. Earnings
        addSectionHeader(mainContent, "Earnings", gbc);
        lblBasic = createLabeledRow(mainContent, "Monthly Basic Salary:", gbc);
        lblAllowances = createLabeledRow(mainContent, "Allowances (Monthly):", gbc);
        lblGross = createLabeledRow(mainContent, "Gross Pay (after late):", gbc);
        addSpacer(mainContent, gbc);

        // 4. Deductions
        addSectionHeader(mainContent, "Deductions", gbc);
        lblSss = createLabeledRow(mainContent, "SSS:", gbc);
        lblPhilhealth = createLabeledRow(mainContent, "PhilHealth (employee):", gbc);
        lblPagibig = createLabeledRow(mainContent, "Pag-IBIG (employee):", gbc);
        addSpacer(mainContent, gbc);

        // 5. Tax & Net Pay
        addSectionHeader(mainContent, "Tax & Net Pay", gbc);
        lblTaxableIncome = createLabeledRow(mainContent, "Taxable Income:", gbc);
        lblTax = createLabeledRow(mainContent, "Withholding Tax:", gbc);
        
        // Net Pay Section
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 0, 0, 0);

        JPanel netPayPanel = new JPanel(new BorderLayout());
        netPayPanel.setOpaque(false);
        netPayPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        lblNetPay = new JLabel("NET PAY: 0.00");
        lblNetPay.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblNetPay.setForeground(Color.BLACK);
        lblNetPay.setHorizontalAlignment(SwingConstants.RIGHT);

        netPayPanel.add(lblNetPay, BorderLayout.CENTER);
        mainContent.add(netPayPanel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainContent.add(Box.createVerticalGlue(), gbc);


        return mainContent;
    }

    private void addSectionHeader(JPanel parent, String text, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 8, 0);

        JLabel header = new JLabel(text);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setForeground(new Color(46, 125, 50));

        parent.add(header, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel createLabeledRow(JPanel parent, String labelText, GridBagConstraints gbc) {
    gbc.gridy++;

    gbc.gridx = 0;
    gbc.weightx = 0.45;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(4, 0, 4, 40);

    JLabel label = new JLabel(labelText);
    label.setForeground(Color.DARK_GRAY);
    label.setFont(new Font("SansSerif", Font.PLAIN, 13));
    parent.add(label, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.55;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(4, 0, 4, 0);

    JLabel value = new JLabel("0.00");
    value.setForeground(Color.BLACK);
    value.setFont(new Font("SansSerif", Font.PLAIN, 13));
    value.setHorizontalAlignment(SwingConstants.RIGHT);
    value.setPreferredSize(new Dimension(180, 22));

    parent.add(value, gbc);

    return value;
    }

    private void addSpacer(JPanel parent, GridBagConstraints gbc) {
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 0, 8, 0);

        parent.add(Box.createVerticalStrut(10), gbc);

        gbc.gridwidth = 1;
    }

    public final void calculateSalary() {
        try {
            String month = (String) monthPicker.getSelectedItem();
            String year = (String) yearPicker.getSelectedItem();

            Object[][] rawLogs = service.getAttendanceLogs(currentUser.getEmpNo(), month, year);
            if (rawLogs == null) {
                rawLogs = new Object[0][];
            }

            double totalHours = 0.0;

            for (Object[] row : rawLogs) {
                try {
                    Attendance record = new Attendance(
                        currentUser.getEmpNo(),
                        LocalDate.parse(row[0].toString().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "N/A".equalsIgnoreCase(row[1].toString().trim()) ? null : LocalTime.parse(row[1].toString().trim()),
                        "N/A".equalsIgnoreCase(row[2].toString().trim()) ? null : LocalTime.parse(row[2].toString().trim())
                    );

                    payrollService.processAttendance(record);
                    totalHours += record.getHoursWorked();

                } catch (Exception rowEx) {
                    System.err.println("Skipping bad attendance row: " + rowEx.getMessage());
                }
            }

            AttendanceSummary attendance = payrollService.summarizeAttendance(
                currentUser.getEmpNo(),
                month,
                year
            );

            PayrollBreakdown breakdown = calc.calculateBreakdown(currentUser, attendance);

            // Employee info
            lblEmployeeId.setText(String.valueOf(currentUser.getEmpNo()));
            lblEmployeeName.setText(currentUser.getFullName());
            lblPosition.setText(currentUser.getPosition() != null ? currentUser.getPosition() : "N/A");
            lblStatus.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "N/A");

            // Attendance summary
            lblDaysPresent.setText(String.valueOf(attendance.getPresentDays()));
            lblLateMinutes.setText(String.valueOf(attendance.getLateMinutes()));
            lblLateDeduction.setText(df.format(calc.calculateLateDeduction(currentUser, attendance.getLateMinutes())));

            // Earnings
            lblBasic.setText(df.format(currentUser.getBasicSalary()));
            lblAllowances.setText(df.format(breakdown.getTotalAllowances()));
            lblGross.setText(df.format(breakdown.getGrossPay()));

            // Deductions
            lblSss.setText(df.format(breakdown.getSss()));
            lblPhilhealth.setText(df.format(breakdown.getPhilhealth()));
            lblPagibig.setText(df.format(breakdown.getPagibig()));

            // Tax + Net
            lblTaxableIncome.setText(df.format(breakdown.getTaxableIncome()));
            lblTax.setText(df.format(breakdown.getWithholdingTax()));
            lblNetPay.setText("NET PAY: " + df.format(breakdown.getNetPay()));

            revalidate();
            repaint();

            SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
            });

            System.out.println("Payslip recalculated for emp " + currentUser.getEmpNo());
            System.out.println("Total hours: " + totalHours);
            System.out.println("Gross: " + breakdown.getGrossPay());
            System.out.println("Taxable: " + breakdown.getTaxableIncome());
            System.out.println("Net: " + breakdown.getNetPay());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calculating payroll: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private int countDistinctDays(Object[][] rawLogs) {
    Set<String> uniqueDays = new HashSet<>();

    for (Object[] row : rawLogs) {
        if (row != null && row.length > 0 && row[0] != null) {
            uniqueDays.add(row[0].toString().trim());
        }
    }

    return uniqueDays.size();
    }
}