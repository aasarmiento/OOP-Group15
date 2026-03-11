package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Attendance {
    private final int empNo;
    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;
    
    // New fields to hold results calculated by the Service Layer
    private double hoursWorked;
    private boolean isLate;
    private int lateMinutes;

    public Attendance(int empNo, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.empNo = empNo;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Getters and Setters for the Service Layer to use
    public int getEmpNo() { return empNo; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }

    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }

    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }

    public int getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(int lateMinutes) { this.lateMinutes = lateMinutes; }
}