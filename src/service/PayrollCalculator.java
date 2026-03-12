package service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import model.AttendanceSummary;
import model.Employee;
import model.PayrollBreakdown;
import model.PeriodSummary;

public class PayrollCalculator {

    private final PayrollPolicy policy;
    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final int GRACE_MINUTES = 10;
    private static final LocalTime LATE_CUTOFF = SHIFT_START.plusMinutes(GRACE_MINUTES);
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PayrollCalculator() {
        this.policy = new DolePolicy();
    }

    // New OOP-based method
    public PayrollBreakdown calculateBreakdown(Employee emp, double hoursWorked) {
        return policy.compute(emp, hoursWorked);
    }

    // Keep old UI-compatible method
    public double calculateGrossIncome(Employee emp, double hoursWorked) {
        return calculateBreakdown(emp, hoursWorked).getGrossPay();
    }

    // Keep old UI-compatible method
    public PeriodSummary calculateFullSummary(Employee emp, double grossIncome) {
        if (emp == null) {
            return new PeriodSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        double hoursWorked = estimateHoursWorkedFromGross(emp, grossIncome);
        PayrollBreakdown breakdown = calculateBreakdown(emp, hoursWorked);

        return new PeriodSummary(
                breakdown.getGrossPay(),
                breakdown.getSss(),
                breakdown.getPhilhealth(),
                breakdown.getPagibig(),
                breakdown.getWithholdingTax(),
                breakdown.getNetPay()
        );
    }

    // Keep wrapper methods too, in case other parts of the system still call them
    public double getSSSDeduction(double monthlySalary) {
        return DolePolicy.getSSSDeduction(monthlySalary);
    }

    public double getPhilHealthDeduction(double monthlySalary) {
        return DolePolicy.getPhilHealthDeduction(monthlySalary);
    }

    public double getPagIBIGDeduction(double monthlySalary) {
        return DolePolicy.getPagIBIGDeduction(monthlySalary);
    }

    public double getWithholdingTax(double taxableIncome) {
        return DolePolicy.getWithholdingTax(taxableIncome);
    }

    private double estimateHoursWorkedFromGross(Employee emp, double grossIncome) {
        if (emp == null || emp.getHourlyRate() <= 0) {
            return 0.0;
        }

        double allowances =
                safe(emp.getRiceSubsidy()) +
                safe(emp.getPhoneAllowance()) +
                safe(emp.getClothingAllowance());

        double estimatedBasicEarned = Math.max(0.0, grossIncome - allowances);
        return estimatedBasicEarned / emp.getHourlyRate();
    }

    public int calculateLateMinutes(Object[][] rawLogs) {
    if (rawLogs == null || rawLogs.length == 0) {
        return 0;
    }

    Map<LocalDate, LocalTime> firstLogInPerDay = new HashMap<>();

    for (Object[] row : rawLogs) {
        if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
            continue;
        }

        String dateStr = row[0].toString().trim();
        String logInStr = row[1].toString().trim();

        if (logInStr.isEmpty() || "N/A".equalsIgnoreCase(logInStr)) {
            continue;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, UI_DATE_FORMAT);
            LocalTime logIn = LocalTime.parse(logInStr);

            LocalTime existing = firstLogInPerDay.get(date);
            if (existing == null || logIn.isBefore(existing)) {
                firstLogInPerDay.put(date, logIn);
            }
        } catch (Exception ignored) {
        }
    }

    int totalLateMinutes = 0;

    for (LocalTime firstLogIn : firstLogInPerDay.values()) {
        if (firstLogIn.isAfter(LATE_CUTOFF)) {
            totalLateMinutes += (int) Duration.between(LATE_CUTOFF, firstLogIn).toMinutes();
        }
    }

    return totalLateMinutes;
    }

    public double calculateDailyRate(Employee emp) {
    double basicSalary = safe(emp.getBasicSalary());
    return round2((basicSalary * 12.0) / 365.0);
    }

    public double calculateMinuteRate(Employee emp) {
        return calculateDailyRate(emp) / 8.0 / 60.0;
    }

    public double calculateLateDeduction(Employee emp, int lateMinutes) {
        return round2(calculateMinuteRate(emp) * lateMinutes);
    }

    public double calculateUndertimeDeduction(Employee emp, int undertimeMinutes) {
        return round2(calculateMinuteRate(emp) * undertimeMinutes);
    }

    public double calculateAbsenceDeduction(Employee emp, int absentDays) {
        return round2(calculateDailyRate(emp) * absentDays);
    }


    public PayrollBreakdown calculateBreakdown(Employee emp, AttendanceSummary attendance) {
    if (emp == null || attendance == null) {
        return new PayrollBreakdown(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    double basicSalary = safe(emp.getBasicSalary());

    double lateDeduction = calculateLateDeduction(emp, attendance.getLateMinutes());
    double undertimeDeduction = calculateUndertimeDeduction(emp, attendance.getUndertimeMinutes());
    double absenceDeduction = calculateAbsenceDeduction(emp, attendance.getAbsentDays());

    double attendanceDeduction = round2(lateDeduction + undertimeDeduction + absenceDeduction);
    double adjustedBasicPay = round2(Math.max(0.0, basicSalary - attendanceDeduction));

    double rice = safe(emp.getRiceSubsidy());
    double phone = safe(emp.getPhoneAllowance());
    double clothing = safe(emp.getClothingAllowance());

    double totalAllowances = round2(rice + phone + clothing);

    double nonTaxableRice = Math.min(rice, 2000.00);
    double nonTaxableClothing = Math.min(clothing, 7000.00 / 12.0);

    double taxableRice = round2(rice - nonTaxableRice);
    double taxableClothing = round2(clothing - nonTaxableClothing);
    double taxablePhone = phone;

    double nonTaxableAllowances = round2(nonTaxableRice + nonTaxableClothing);
    double taxableAllowances = round2(taxableRice + taxableClothing + taxablePhone);

    double grossPay = round2(adjustedBasicPay + totalAllowances);

    // Continue using monthly basic salary as contribution base
    double contributionBase = basicSalary;

    double sss = DolePolicy.getSSSDeduction(contributionBase);
    double philhealth = DolePolicy.getPhilHealthDeduction(contributionBase);
    double pagibig = DolePolicy.getPagIBIGDeduction(contributionBase);

    double taxableIncome = round2(
            Math.max(0.0, adjustedBasicPay + taxableAllowances - (sss + philhealth + pagibig))
    );

    double withholdingTax = DolePolicy.getWithholdingTax(taxableIncome);
    double netPay = round2(grossPay - (sss + philhealth + pagibig + withholdingTax));

    return new PayrollBreakdown(
            attendance.getWorkedHours(),
            adjustedBasicPay,
            totalAllowances,
            taxableAllowances,
            nonTaxableAllowances,
            grossPay,
            taxableIncome,
            sss,
            philhealth,
            pagibig,
            withholdingTax,
            netPay
        );
    }

    private double safe(double value) {
    return Double.isFinite(value) ? value : 0.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


}