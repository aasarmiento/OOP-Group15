package dao;

import java.io.*;

public class LogHandler {
    // Using your absolute path
    private static final String LOGIN_CSV = "/Users/abigail/NetBeansProjects/MotorPH3/src/MotorPH_EmployeeLogin.csv";

    public String[] verifyCredentials(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_CSV))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Assume CSV: EmployeeID, Username, Password, Role
                if (data[1].equals(username) && data[2].equals(password)) {
                    return data; // Return the whole row (ID, User, Pass, Role)
                }
            }
        } catch (IOException e) {
            System.err.println("Login File Error: " + e.getMessage());
        }
        return null; // Login failed
    }
} 