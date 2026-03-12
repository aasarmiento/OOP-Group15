package model;

public class AttendanceSummary {
    private final int presentDays;
    private final int expectedWorkDays;
    private final int absentDays;
    private final int lateMinutes;
    private final int undertimeMinutes;
    private final int workedMinutes;

    public AttendanceSummary(
            int presentDays,
            int expectedWorkDays,
            int absentDays,
            int lateMinutes,
            int undertimeMinutes,
            int workedMinutes
    ) {
        this.presentDays = presentDays;
        this.expectedWorkDays = expectedWorkDays;
        this.absentDays = absentDays;
        this.lateMinutes = lateMinutes;
        this.undertimeMinutes = undertimeMinutes;
        this.workedMinutes = workedMinutes;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public int getExpectedWorkDays() {
        return expectedWorkDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public int getUndertimeMinutes() {
        return undertimeMinutes;
    }

    public int getWorkedMinutes() {
        return workedMinutes;
    }

    public double getWorkedHours() {
        return workedMinutes / 60.0;
    }
}