package model;

public interface ISupportOperations {
    void resolveTicket(int empNo);
    String issueTemporaryPassword(int empNo);
    boolean unlockAccount(int empNo);
}