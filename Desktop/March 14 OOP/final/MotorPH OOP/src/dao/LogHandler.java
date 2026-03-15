package dao;

import java.io.*;
import java.time.LocalDateTime;

public class LogHandler {
    
    private static final String LOGIN_CSV_PATH = "resources/MotorPH_EmployeeLogin.csv";

    public String[] verifyCredentials(String username, String password) {
        
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_CSV_PATH))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                
                
                if (data.length >= 3) {
                    if (data[1].equals(username) && data[2].equals(password)) {
                        return data; 
                    }
                }
            }
        } catch (IOException e) {
            
            System.err.println("Login File Error: " + e.getMessage());
        }
        return null; 
    }

    public void logAction(String message) {
        System.out.println("[" + LocalDateTime.now() + "] LOG: " + message);
    }
}