/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;
import java.util.List;

public interface IHROperations {
    void updateEmployeeDetails(String empID, Employee updatedData);
    Employee viewEmployeeProfile(String empID);
    void approveLeaveRequest(String leaveID);
    void rejectLeaveRequest(String leaveID, String reason);
    List<LeaveRequest> viewAllLeaveRequests(); 
}