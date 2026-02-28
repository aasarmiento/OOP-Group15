package motorph3; 

import ui.LoginPanel; 

public class Main {
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(() -> {
            new LoginPanel().setVisible(true);
        });
    }
}