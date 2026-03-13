package ui;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.EmployeeManagementService;
import service.PayrollCalculator;
import service.PayrollService;

public class PayrollFinances extends JPanel {
    private final EmployeeManagementService empService;
    private final PayrollService payrollService;
    private final PayrollCalculator calc;
    
    private JTable payrollTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthPicker, yearPicker;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private JLabel lblTotalSalary, lblTotalAllowances, lblTotalGross, lblTotalNet;

    public PayrollFinances(EmployeeManagementService empService, PayrollService payrollService, PayrollCalculator calc, Employee user) {
        this.empService = empService;
        this.payrollService = payrollService;
        this.calc = calc;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);
        topPanel.add(createFilterPanel(), BorderLayout.NORTH);
        topPanel.add(createKPIRow(), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);

        JPanel chartPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartPanel.setOpaque(false);
        chartPanel.add(createChartPlaceholder("Payroll Overview  ginagawa pa lang "));
        chartPanel.add(createChartPlaceholder("Payroll Breakdown (Doughnut) ginagawa pa lang "));
        
        centerPanel.add(chartPanel, BorderLayout.NORTH);
        centerPanel.add(createPayrollListTable(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        
        refreshData();
    }

    private JPanel createFilterPanel() {
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setOpaque(false);

        monthPicker = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        });
        yearPicker = new JComboBox<>(new String[]{"2024", "2025", "2026"});
        
        JButton btnRefresh = new JButton("Generate Report");
        btnRefresh.setBackground(new Color(128, 0, 0));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshData());

        filter.add(new JLabel("Payroll Period:"));
        filter.add(monthPicker);
        filter.add(yearPicker);
        filter.add(btnRefresh);
        
        return filter;
    }

    private JPanel createKPIRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);
        
        KPICard card1 = new KPICard("Total Basic", "₱0.00", new Color(128, 0, 0));
        lblTotalSalary = card1.getValueLabel();
        
        KPICard card2 = new KPICard("Total Allowances", "₱0.00", new Color(128, 0, 0));
        lblTotalAllowances = card2.getValueLabel();
        
        KPICard card3 = new KPICard("Total Gross", "₱0.00", new Color(128, 0, 0));
        lblTotalGross = card3.getValueLabel();
        
        KPICard card4 = new KPICard("Total Net Pay", "₱0.00", new Color(0, 100, 0));
        lblTotalNet = card4.getValueLabel();

        row.add(card1);
        row.add(card2);
        row.add(card3);
        row.add(card4);
        return row;
    }

    private JPanel createPayrollListTable() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Employee Payroll Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        container.add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Position", "Basic Salary", "Allowances", "Gross Pay", "Net Pay", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        payrollTable = new JTable(tableModel);
        payrollTable.setRowHeight(35);
        payrollTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        payrollTable.setSelectionBackground(new Color(128, 0, 0)); 
        payrollTable.setSelectionForeground(Color.WHITE);          
        payrollTable.setShowGrid(false);
        payrollTable.setIntercellSpacing(new Dimension(0, 0));

        payrollTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = payrollTable.columnAtPoint(evt.getPoint());
                int row = payrollTable.rowAtPoint(evt.getPoint());

                if (row != -1 && column == 8) {
                    try {
                        Object idValue = tableModel.getValueAt(row, 0);
                        int empId = Integer.parseInt(idValue.toString());
                        showDetailedRecord(empId);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        container.add(scrollPane, BorderLayout.CENTER);
        
        return container;
    }

    private void showDetailedRecord(int empId) {
        Employee selectedEmp = empService.getEmployeeDao().findById(empId);
        if (selectedEmp != null) {
            MyPayslip payslipPanel = new MyPayslip(empService, calc, payrollService, selectedEmp);
            
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow instanceof Frame ? (Frame)parentWindow : null, "Detailed Record", true);
            dialog.add(new JScrollPane(payslipPanel));
            dialog.setSize(700, 850); 
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0); 
        String month = (String) monthPicker.getSelectedItem();
        String year = (String) yearPicker.getSelectedItem();

        try {
            List<Object[]> report = payrollService.getFullPayrollReport(month, year);
            double totalNet = 0;
            for (Object[] row : report) {
                Object[] rowData = new Object[9];
                rowData[0] = row[0]; 
                rowData[1] = row[1]; 
                rowData[2] = row[2]; 
                rowData[3] = df.format(row[3]); 
                rowData[4] = df.format(row[4]); 
                rowData[5] = df.format(row[5]); 
                rowData[6] = df.format(row[6]); 
                rowData[7] = row[7]; 
                rowData[8] = "View"; 
                
                tableModel.addRow(rowData);
                totalNet += (double) row[6];
            }
            lblTotalNet.setText("₱" + df.format(totalNet));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JPanel createChartPlaceholder(String titleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(0, 250));
        p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JLabel label = new JLabel(titleText, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.ITALIC, 12));
        label.setForeground(Color.LIGHT_GRAY);
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private static class KPICard extends JPanel {
        private final JLabel valueLabel;
        public KPICard(String title, String value, Color iconColor) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblTitle.setForeground(Color.GRAY);
            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            textPanel.add(lblTitle);
            textPanel.add(valueLabel);
            JLabel iconLabel = new JLabel("₱", SwingConstants.CENTER);
            iconLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            iconLabel.setPreferredSize(new Dimension(45, 45));
            iconLabel.setOpaque(true);
            iconLabel.setBackground(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30));
            iconLabel.setForeground(iconColor);
            add(textPanel, BorderLayout.CENTER);
            add(iconLabel, BorderLayout.EAST);
        }
        public JLabel getValueLabel() { return valueLabel; }
    }
}