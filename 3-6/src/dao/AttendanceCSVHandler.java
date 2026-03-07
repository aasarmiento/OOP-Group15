package dao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attendance;

public class AttendanceCSVHandler implements AttendanceDAO {
    
    private static final String FILE_PATH = "resources/MotorPH_AttendanceRecord.csv";
    // MATCHING YOUR WORKING CODE: dd/MM/yyyy
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    
@Override
public List<Attendance> getAttendanceByEmployee(int empNo) {
    List<Attendance> list = new ArrayList<>();
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    try (BufferedReader br = new BufferedReader(new FileReader("resources/MotorPH_AttendanceRecord.csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            // Skip header or short lines
            if (p.length < 6 || p[0].trim().startsWith("Employee")) continue;

            try {
                int id = Integer.parseInt(p[0].trim());
                if (id == empNo) {
                    // 1. Parse Date (dd/MM/yyyy)
                    LocalDate date = LocalDate.parse(p[3].trim(), dateFmt);
                    
                    // 2. Parse Times safely using the helper method below
                    LocalTime timeIn = parseTime(p[4].trim());
                    LocalTime timeOut = parseTime(p[5].trim());

                    // 3. Create the object using your specific constructor
                    // ID, Date, TimeIn, TimeOut
                    Attendance a = new Attendance(id, date, timeIn, timeOut);
                    list.add(a);
                }
            } catch (Exception e) {
                // This logs which line is breaking the code (e.g., format issues)
                System.out.println("Skipping bad line: " + line + " Error: " + e.getMessage());
            }
        }
    } catch (IOException e) { 
        e.printStackTrace(); 
    }
    return list;
}

/**
 * Helper method to handle "N/A", "00:00", or single-digit hours like "2:24"
 */
private LocalTime parseTime(String timeStr) {
    if (timeStr == null || timeStr.equalsIgnoreCase("N/A") || timeStr.equals("00:00") || timeStr.isEmpty()) {
        return null; 
    }
    try {
        // "H:mm" handles both "2:24" and "14:24"
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
    } catch (Exception e) {
        return null;
    }
}



@Override
public Object[][] getAttendanceByMonth(int empNo, String month, String year) {
    List<Attendance> allLogs = getAttendanceByEmployee(empNo);
    List<Object[]> filteredRows = new ArrayList<>();

    for (Attendance a : allLogs) {
        // Get the month name (e.g., MARCH) and year (e.g., 2026)
        String logMonth = a.getDate().getMonth().name(); 
        String logYear = String.valueOf(a.getDate().getYear());
        
        // LOGIC: If month is "All", skip month check. If year is "All", skip year check.
        boolean monthMatch = month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month);
        boolean yearMatch = year.equalsIgnoreCase("All") || logYear.equals(year);

        if (monthMatch && yearMatch) {
            filteredRows.add(new Object[]{
                a.getDate().format(dateFormat), // Use dd/MM/yyyy
                a.getTimeIn() != null ? a.getTimeIn().toString() : "N/A",
                a.getTimeOut() != null ? a.getTimeOut().toString() : "N/A"
            });
        }
    }
    return filteredRows.toArray(new Object[0][]);
}

 

@Override
public void recordAttendance(int empNo, String lastName, String firstName, String type) {
    File file = new File("resources/MotorPH_AttendanceRecord.csv");
    List<String> lines = new ArrayList<>();
    
    // FORMAT: dd/MM/yyyy (Matches your successful admin logic)
    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
    boolean foundToday = false;

    // 1. READ and check for today's record
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            
            // Keep the header
            if (line.startsWith("Employee #")) {
                lines.add(line);
                continue;
            }

            String[] parts = line.split(",");
            // parts[0] is ID, parts[3] is Date
            if (parts.length >= 4 && parts[0].trim().equals(String.valueOf(empNo)) 
                && parts[3].trim().equals(todayStr)) {
                
                foundToday = true;
                if (type.equalsIgnoreCase("Check-out")) {
                    // Update only Log Out (parts[5]), keep Log In (parts[4])
                    line = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + timeStr;
                }
                // If it's a Check-in but foundToday is already true, do nothing (block duplicate)
            }
            lines.add(line);
        }
    } catch (IOException e) { e.printStackTrace(); }

    // 2. Add NEW Check-in if not found
    if (type.equalsIgnoreCase("Check-in") && !foundToday) {
        lines.add(empNo + "," + lastName + "," + firstName + "," + todayStr + "," + timeStr + ",N/A");
    }

    // 3. WRITE back to file
    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
        for (String l : lines) pw.println(l);
        pw.flush();
    } catch (IOException e) { e.printStackTrace(); }
}



}