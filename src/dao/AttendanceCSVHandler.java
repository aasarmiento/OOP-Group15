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

        String todayStr = LocalDate.now().format(fileDateFormat);
        String timeStr = LocalTime.now().format(timeFormat);
        boolean headerFound = false;
        boolean openSessionFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.replace("\uFEFF", "").startsWith("Employee #")) {
                    headerFound = true;
                    lines.add(line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 6
                        && parts[0].replace("\uFEFF", "").trim().equals(String.valueOf(empNo))
                        && parts[3].trim().equals(todayStr)) {

                    String logIn = parts[4].trim();
                    String logOut = parts[5].trim();

                    boolean hasLogIn = !logIn.equalsIgnoreCase("N/A") && !logIn.isEmpty();
                    boolean hasOpenLogOut = logOut.equalsIgnoreCase("N/A") || logOut.isEmpty();

                    // For checkout: close the latest open row for today
                    if (type.equalsIgnoreCase("Check-out") && hasLogIn && hasOpenLogOut) {
                        line = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + timeStr;
                        openSessionFound = true;
                    }

                    // For check-in: remember if an open session already exists
                    if (type.equalsIgnoreCase("Check-in") && hasLogIn && hasOpenLogOut) {
                        openSessionFound = true;
                    }
                }

                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Attendance file not found. Creating new file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!headerFound) {
            lines.add(0, "Employee #,Last Name,First Name,Date,Log In,Log Out");
        }

        // Check-in: add a NEW row only if there is no currently open session
        if (type.equalsIgnoreCase("Check-in")) {
            if (!openSessionFound) {
                lines.add(empNo + "," + lastName + "," + firstName + "," + todayStr + "," + timeStr + ",N/A");
                System.out.println("Check-in recorded for emp " + empNo + " at " + timeStr);
            } else {
                System.out.println("Check-in ignored: open session already exists.");
            }
        }

        // Check-out: only valid if an open session was found and closed
        if (type.equalsIgnoreCase("Check-out") && !openSessionFound) {
            System.out.println("Check-out ignored: no open session found.");
        }

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
            for (String l : lines) {
                pw.println(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    @Override
    public String getLastStatus(int empId) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return "READY_FOR_CHECK_IN";

        String todayStr = LocalDate.now().format(fileDateFormat);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Employee #")) {
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 6) continue;

                int id = Integer.parseInt(data[0].replace("\uFEFF", "").trim());
                String date = data[3].trim();
                String logIn = data[4].trim();
                String logOut = data[5].trim();

                if (id == empId && todayStr.equals(date)) {
                    boolean hasLogIn = !logIn.equalsIgnoreCase("N/A") && !logIn.isEmpty();
                    boolean hasOpenLogOut = logOut.equalsIgnoreCase("N/A") || logOut.isEmpty();

                    if (hasLogIn && hasOpenLogOut) {
                        return "CHECKED_IN";
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading last status: " + e.getMessage());
        }

        return "READY_FOR_CHECK_IN";
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