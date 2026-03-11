package model;

public class Deduction {
    private double sss;
    private double philHealth;
    private double pagIbig;
    private double tax;
    private double lateAmount;
    private int lateMinutes;
    private double totalAllowances;
    private double grossPay;
    private double netPay;

    // 1. DEFAULT CONSTRUCTOR (Fixes: "cannot be applied to given types")
    public Deduction() {}

    // 2. FULL CONSTRUCTOR (For the Calculator)
    public Deduction(double sss, double ph, double pagibig, double tax, double lateAmount, int lateMinutes) {
        this.sss = sss;
        this.philHealth = ph;
        this.pagIbig = pagibig;
        this.tax = tax;
        this.lateAmount = lateAmount;
        this.lateMinutes = lateMinutes;
    }

    // 3. SETTERS (Fixes: "cannot find symbol: method setSss", etc.)
    public void setSss(double sss) { this.sss = sss; }
    public void setPhilHealth(double philHealth) { this.philHealth = philHealth; }
    public void setPagIbig(double pagIbig) { this.pagIbig = pagIbig; }
    public void setTax(double tax) { this.tax = tax; }
    public void setLateAmount(double lateAmount) { this.lateAmount = lateAmount; }
    public void setLateMinutes(int lateMinutes) { this.lateMinutes = lateMinutes; }
    public void setTotalAllowances(double totalAllowances) { this.totalAllowances = totalAllowances; }
    public void setGrossPay(double grossPay) { this.grossPay = grossPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

    // 4. GETTERS (For the UI labels)
    public double getSss() { return sss; }
    public double getPhilHealth() { return philHealth; }
    public double getPagIbig() { return pagIbig; }
    public double getTax() { return tax; }
    public double getLateAmount() { return lateAmount; }
    public int getLateMinutes() { return lateMinutes; }
    public double getTotalAllowances() { return totalAllowances; }
    public double getGrossPay() { return grossPay; }
    public double getNetPay() { return netPay; }

    public double getTotal() {
        return sss + philHealth + pagIbig + tax + lateAmount;
    }
}