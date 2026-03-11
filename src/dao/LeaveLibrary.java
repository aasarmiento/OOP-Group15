package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;



public class LeaveLibrary {
   private static final String LEAVE_FILE = "resources/MotorPH_LeaveRequests.csv";

    public List<String[]> fetchAllLeaves() {
        List<String[]> data = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            String header = br.readLine(); // Skip header
            String line;
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Regex to split by comma but ignore commas inside double quotes
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // Clean up quotes from the reason column if they exist
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "").trim();
                }
                
                data.add(row);
            }
        } catch (IOException e) { 
            System.err.println("File Error: " + e.getMessage()); 
        }
        return data;
    }

public boolean updateLeaveStatus(String requestId, String newStatus) {
    List<String[]> allData = fetchAllLeaves(); 
    boolean updated = false;

    try (PrintWriter pw = new PrintWriter(new FileWriter(LEAVE_FILE))) {
        // Write Header
        pw.println("RequestID,EmployeeID,LastName,FirstName,Type,Start,End,Reason,Status");

        for (String[] row : allData) {
            if (row.length >= 9 && row[0].trim().equals(requestId.trim())) {
                row[8] = newStatus; 
                updated = true;
            }
            
            // FIX: Ensure the "Reason" (index 7) is wrapped in quotes if it contains commas
            if (row[7].contains(",")) {
                row[7] = "\"" + row[7] + "\"";
            }
            
            pw.println(String.join(",", row));
        }
    } catch (IOException e) {
        return false;
    }
    return updated;
}



    public void saveLeave(String csvLine) {
        try (FileWriter fw = new FileWriter(LEAVE_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(csvLine);
        } catch (IOException e) { 
            System.err.println("Save Error: " + e.getMessage()); 
        }
    }
}