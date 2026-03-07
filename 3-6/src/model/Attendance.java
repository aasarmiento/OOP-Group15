package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class Attendance {
    
    private final int empNo;
    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;

    public Attendance(int empNo, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.empNo = empNo;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public double getHoursWorked() {
        if (timeIn == null || timeOut == null) return 0.0;

        long minutes = Duration.between(timeIn, timeOut).toMinutes();
        
        
        double hours = (minutes - 60) / 60.0;

        return Math.max(0, hours);
    }

    
    public int getEmpNo() { return empNo; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
}