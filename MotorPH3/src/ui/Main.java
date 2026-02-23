package ui;

import java.time.LocalDate;
import javax.swing.*;
import model.Admin;
import model.Employee;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            AppController controller = new AppController();

            Employee testUser = new Admin(
                    1,
                    "Admin",
                    "Test",
                    LocalDate.of(2000, 1, 1),
                    30000.0
            );

            controller.setCurrentUser(testUser);

            JFrame frame = new JFrame("MotorPH System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);

            frame.setContentPane(new DashboardPanel(controller));
            frame.setVisible(true);
        });
    }
}