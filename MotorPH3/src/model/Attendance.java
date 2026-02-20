package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class Attendance {
    // Marked as final because the ID, Date, and Times shouldn't change after loading
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
        
        // Using 60.0 to ensure double precision (e.g., 7.5 hours)
        // Standard MotorPH rule: Deduct 1 hour (60 mins) for lunch
        double hours = (minutes - 60) / 60.0;

        return Math.max(0, hours);
    }

    // Getters (Setters are not needed because fields are final)
    public int getEmpNo() { return empNo; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
}