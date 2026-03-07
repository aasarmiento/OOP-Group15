package dao;

import java.util.List;
import model.Attendance;

public interface AttendanceDAO {
    // Returns list of models for logic
    List<Attendance> getAttendanceByEmployee(int empNo);
    
    // Returns formatted data for JTable
    Object[][] getAttendanceByMonth(int empNo, String month, String year);
    
    // Action to save to CSV
    

    
    public void recordAttendance(int empNo, String lastName, String firstName, String type);
}