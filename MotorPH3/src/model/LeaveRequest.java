/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package model;

import java.time.LocalDate;


public class LeaveRequest {
    
    private String requestId;           //  "LR-EMP001-1734567890123"

    // Who requested it
    private String empID;               
    // Leave period
    private LocalDate startDate;
    private LocalDate endDate;

    // Type and reason
    private String leaveType;           //  "Vacation", "Sick", "Emergency", "Maternity"
    private String reason;              // Employee's explanation

    // Status & approval info
    private String status;              // "PENDING", "APPROVED", "REJECTED"
    private final LocalDate submittedDate;   
    private LocalDate approvalDate;     
    private String approvedBy;          

   
    private double deductedDays;        
public LeaveRequest(int empNo, String leaveType, LocalDate startDate, LocalDate endDate) {
    this.empID = String.valueOf(empNo); 
    this.leaveType = leaveType;
    this.startDate = startDate;
    this.endDate = endDate;
    
    //  (Encapsulation logic)
    this.status = "PENDING";
    this.submittedDate = LocalDate.now();
    this.requestId = "LR-" + this.empID + "-" + System.currentTimeMillis();
    this.reason = "N/A"; 
    calculateDeductedDays();
}

    
    public LeaveRequest(String requestId, String empID, LocalDate startDate, LocalDate endDate,
                        String leaveType, String reason) {
        this.requestId = requestId;
        this.empID = empID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = "PENDING";
        this.submittedDate = LocalDate.now();

        this.deductedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getEmpID() { return empID; }
    public void setEmpID(String empID) { this.empID = empID; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getSubmittedDate() { return submittedDate; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public double getDeductedDays() { return deductedDays; }

    public final void calculateDeductedDays() {
        this.deductedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    

    @Override
    public String toString() {
        return "LeaveRequest{" +
               "requestId='" + requestId + '\'' +
               ", empID='" + empID + '\'' +
               ", " + startDate + " to " + endDate +
               ", type='" + leaveType + '\'' +
               ", status='" + status + '\'' +
               ", deductedDays=" + deductedDays +
               ", submitted=" + submittedDate +
               (approvalDate != null ? ", approved by " + approvedBy + " on " + approvalDate : "") +
               '}';
    }
}