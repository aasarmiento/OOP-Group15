package dao;

import model.Attendance;
import java.util.List;

public interface AttendanceDAO {
    List<Attendance> getAttendanceByEmployee(int empNo);
}