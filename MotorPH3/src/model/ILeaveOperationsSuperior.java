package model;

/**
 * Interface for leave operations handled by a superior/manager.
 */
public interface ILeaveOperationsSuperior {
    
    void updateLeaveStatus(LeaveRequest request, String newStatus);
}