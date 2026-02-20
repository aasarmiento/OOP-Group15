import ui.PayrollFrame; // Import your starting UI class

public class Main {
    public static void main(String[] args) {
        // Launch the UI thread
        java.awt.EventQueue.invokeLater(() -> {
            new PayrollFrame().setVisible(true);
        });
    }
}