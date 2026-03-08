package dao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attendance;

public class AttendanceCSVHandler implements AttendanceDAO {
    
    private static final String FILE_PATH = "resources/MotorPH_AttendanceRecord.csv";
    // Correct format for your CSV data: MM/dd/yyyy
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public List<Attendance> getAttendanceByEmployee(int empNo) {
        List<Attendance> list = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                // Skip header or short lines
                if (p.length < 6 || p[0].trim().startsWith("Employee")) continue;

                try {
                    int id = Integer.parseInt(p[0].trim());
                    if (id == empNo) {
                        LocalDate date = LocalDate.parse(p[3].trim(), dateFmt);
                        LocalTime timeIn = parseTime(p[4].trim());
                        LocalTime timeOut = parseTime(p[5].trim());

                        Attendance a = new Attendance(id, date, timeIn, timeOut);
                        list.add(a);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping bad line: " + line + " Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.equalsIgnoreCase("N/A") || timeStr.equals("00:00") || timeStr.isEmpty()) {
            return null;
        }
        try {
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
            String logMonth = a.getDate().getMonth().name();
            String logYear = String.valueOf(a.getDate().getYear());

            boolean monthMatch = month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month);
            boolean yearMatch = year.equalsIgnoreCase("All") || logYear.equals(year);

            if (monthMatch && yearMatch) {
                filteredRows.add(new Object[]{
                    a.getDate().format(dateFormat),
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

        // Keep this consistent with the CSV: MM/dd/yyyy
        String todayStr = LocalDate.now().format(dateFormat);
        String timeStr = LocalTime.now().format(timeFormat);
        boolean foundToday = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.startsWith("Employee #")) {
                    lines.add(line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].trim().equals(String.valueOf(empNo))
                        && parts[3].trim().equals(todayStr)) {

                    foundToday = true;
                    if (type.equalsIgnoreCase("Check-out")) {
                        line = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + timeStr;
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (type.equalsIgnoreCase("Check-in") && !foundToday) {
            lines.add(empNo + "," + lastName + "," + firstName + "," + todayStr + "," + timeStr + ",N/A");
        }

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
            for (String l : lines) pw.println(l);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}