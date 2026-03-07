package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.EmployeeManagementService;

public class TimePanel extends JPanel {
    private DefaultTableModel model;
    private JComboBox<String> monthPicker, yearPicker;
    private JButton btnIn, btnOut;
    
    private final EmployeeManagementService service;
    private final Employee currentUser;

    public TimePanel(EmployeeManagementService service, Employee user) {
        this.service = service;
        this.currentUser = user;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Build the front-end ichura
        add(createTimeTrackingPanel(), BorderLayout.CENTER);
        
        refreshUI();
    }

    // FIX: Added back to resolve "cannot find symbol" in DashboardPanel
    public void setLoggedIn(String id, String lastName, String firstName) {
        // No logic here - it just tells the UI to refresh its view
        refreshUI();
    }

    private JPanel createTimeTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setOpaque(false);

        monthPicker = new JComboBox<>(new String[]{"All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        yearPicker = new JComboBox<>(new String[]{"All", "2024", "2025", "2026"});
        yearPicker.setSelectedItem("2026");

        // UI Components - Buttons
        btnIn = new JButton("Check In");
        btnOut = new JButton("Check Out");
        
        // FIX: Applied the specific Green style for Check In as seen in your screenshot
        styleButton(btnIn, new Color(34, 139, 34)); 
        styleButton(btnOut, new Color(178, 34, 34)); 

        header.add(new JLabel("Month:"));
        header.add(monthPicker);
        header.add(new JLabel("Year:"));
        header.add(yearPicker);
        header.add(btnIn); 
        header.add(btnOut);

        model = new DefaultTableModel(new String[]{"Date", "Log In", "Log Out"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);

     // --- UI Interactions inside TimePanel.java ---

// Inside createTimeTrackingPanel
// INSIDE createTimeTrackingPanel - CLEAN VERSION
// Clean up: Use ONLY these listeners
btnIn.addActionListener(e -> {
    int confirm = JOptionPane.showConfirmDialog(this, "Check In?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        service.recordTimeLog(currentUser.getEmpNo(), "Check-in");
        refreshUI(); // This forces the button states to recalculate
    }
});

btnOut.addActionListener(e -> {
    int confirm = JOptionPane.showConfirmDialog(this, "Check Out?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        service.recordTimeLog(currentUser.getEmpNo(), "Check-out");
        refreshUI(); 
    }
});
// DELETE THE THIRD BTNOUT BLOCK COMPLETELY!




        monthPicker.addActionListener(e -> refreshUI());
        yearPicker.addActionListener(e -> refreshUI());

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

  // Inside TimePanel.java

private void refreshUI() {
    model.setRowCount(0);
    this.revalidate();
    this.repaint();
    
    // Must pass 3 arguments to match your AttendanceCSVHandler
    String month = (String) monthPicker.getSelectedItem();
    String year = (String) yearPicker.getSelectedItem();
    
    Object[][] data = service.getAttendanceLogs(currentUser.getEmpNo(), month, year);
    if (data != null) {
        for (Object[] row : data) {
            model.addRow(row);
        }
    }

    // Update the grey-out "ichura" based on Service logic
    boolean[] states = service.getButtonStates(currentUser.getEmpNo());
    btnIn.setEnabled(states[0]);
    btnOut.setEnabled(states[1]);
}
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }
}