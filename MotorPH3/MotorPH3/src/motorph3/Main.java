package motorph3; // Or whatever your base package is named

import ui.LoginPanel; // Import the LoginPanel

public class Main {
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(() -> {
            new LoginPanel().setVisible(true);
        });
    }
}