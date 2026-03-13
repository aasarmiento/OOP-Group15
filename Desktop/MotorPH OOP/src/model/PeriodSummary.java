package model;

public class PeriodSummary {
    private double grossIncome, sss, philhealth, pagibig, tax, netPay;

    public PeriodSummary(double gross, double sss, double ph, double pi, double tax, double net) {
        this.grossIncome = gross;
        this.sss = sss;
        this.philhealth = ph;
        this.pagibig = pi;
        this.tax = tax;
        this.netPay = net;
    }

    public double getGrossIncome() { return grossIncome; }
    public double getSss() { return sss; }
    public double getPhilhealth() { return philhealth; }
    public double getPagibig() { return pagibig; }
    public double getTax() { return tax; }
    public double getNetPay() { return netPay; }
}