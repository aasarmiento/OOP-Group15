package dao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attendance;

public class AttendanceCSVHandler implements AttendanceDAO {
    
    private static final String FILE_PATH = "resources/MotorPH_AttendanceRecord.csv";
    
    // FIXED: Your logs show 12/25/2024, which is MM/dd/yyyy
    private final DateTimeFormatter fileDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public List<Attendance> getAttendanceByEmployee(int empNo) {
        List<Attendance> list = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] p = line.split(",");
                // Skip header or short lines
                if (p.length < 6 || p[0].trim().startsWith("Employee")) continue;

                try {
                    // Remove BOM or hidden spaces
                    int id = Integer.parseInt(p[0].replace("\uFEFF", "").trim());
                    
                    if (id == empNo) {
                        // 1. Parse Date using the MM/dd/yyyy formatter (fileDateFormat)
                        LocalDate date = LocalDate.parse(p[3].trim(), fileDateFormat);
                        
                        // 2. Parse Times safely using the helper method below
                        LocalTime timeIn = parseTime(p[4].trim());
                        LocalTime timeOut = parseTime(p[5].trim());

                        // 3. Create the object using your specific constructor
                        Attendance a = new Attendance(id, date, timeIn, timeOut);
                        list.add(a);
                    }
                } catch (Exception e) {
                    // This will no longer skip lines for dates like 12/25/2024
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
            return LocalTime.parse(timeStr, timeFormat);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object[][] getAttendanceByMonth(int empNo, String month, String year) {
        List<Attendance> allLogs = getAttendanceByEmployee(empNo);
        List<Object[]> filteredRows = new ArrayList<>();

        for (Attendance a : allLogs) {
            // Get the month name (e.g., MARCH) and year
            String logMonth = a.getDate().getMonth().name(); 
            String logYear = String.valueOf(a.getDate().getYear());
            
            // LOGIC: If month is "All", skip month check. If year is "All", skip year check.
            boolean monthMatch = month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month);
            boolean yearMatch = year.equalsIgnoreCase("All") || logYear.equals(year);

            if (monthMatch && yearMatch) {
                filteredRows.add(new Object[]{
                    a.getDate().format(dateFormat), // Displays to UI as dd/MM/yyyy
                    a.getTimeIn() != null ? a.getTimeIn().toString() : "N/A",
                    a.getTimeOut() != null ? a.getTimeOut().toString() : "N/A"
                });
            }
        }
        return filteredRows.toArray(new Object[0][]);
    }

    @Override
    public void recordAttendance(int empNo, String lastName, String firstName, String type) {
        File file = new File(FILE_PATH);
        List<String> lines = new ArrayList<>();
        
        // Use MM/dd/yyyy to match the existing file data format
        String todayStr = LocalDate.now().format(fileDateFormat);
        String timeStr = LocalTime.now().format(timeFormat);
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
                if (parts.length >= 4 && parts[0].replace("\uFEFF", "").trim().equals(String.valueOf(empNo)) 
                    && parts[3].trim().equals(todayStr)) {
                    
                    foundToday = true;
                    if (type.equalsIgnoreCase("Check-out")) {
                        // Update only Log Out (parts[5]), keep Log In (parts[4])
                        line = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + timeStr;
                    }
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





    // Inside your AttendanceDAO implementation class
@Override
public String getLastStatus(int empId) {
    String lastStatus = "Check-out"; // Default state
    File file = new File("resources/attendance.csv");
    
    if (!file.exists()) return lastStatus;

    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            // Assuming CSV format: EmpID, LastName, FirstName, Date, Time, Action
            if (data.length >= 6 && Integer.parseInt(data[0].trim()) == empId) {
                lastStatus = data[5].trim(); // Get the "Action" column
            }
        }
    } catch (Exception e) {
        System.err.println("Error reading last status: " + e.getMessage());
    }
    
    return lastStatus;
}


@Override // Add this
public Map<String, Integer> countWorkingDaysPerMonth() {
    // Use FILE_PATH constant instead of hardcoded string to avoid errors
    Map<String, Set<String>> monthDays = new HashMap<>();
    
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length < 4) continue; 
            
            String date = data[3].trim(); // Example: 12/25/2024
            
            // Safety check: ensure the date string is long enough
            if (date.length() >= 10) {
                String monthYear = date.substring(0, 2) + "/" + date.substring(6); 
                monthDays.computeIfAbsent(monthYear, k -> new HashSet<>()).add(date);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    Map<String, Integer> report = new HashMap<>();
    monthDays.forEach((k, v) -> report.put(k, v.size()));
    return report;
}



}