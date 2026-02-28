package dao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attendance;

public class AttendanceCSVHandler implements AttendanceDAO {
    private static final String FILE_PATH = "resources/MotorPH_AttendanceRecord.csv";
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public List<Attendance> getAttendanceByEmployee(int empNo) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                
               
                if (parts.length >= 6 && Integer.parseInt(parts[0].trim()) == empNo) {
                    try {
                        LocalDate date = LocalDate.parse(parts[3].trim(), dateFormat);
                        LocalTime logIn = LocalTime.parse(parts[4].trim(), timeFormat);
                       
                        LocalTime logOut = parts[5].trim().isEmpty() ? logIn : LocalTime.parse(parts[5].trim(), timeFormat);

                        attendanceList.add(new Attendance(empNo, date, logIn, logOut));
                    } catch (Exception e) {
                       
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading attendance: " + e.getMessage());
        }
        return attendanceList;
    }
}