package util;

import javax.swing.JPanel;

public interface DashboardRole {
JPanel getSidebar(CardLayout cardLayout, JPanel cardPanel);
    void setupCards(JPanel cardPanel); // Adds the specific JPanels to the CardLayout
}
