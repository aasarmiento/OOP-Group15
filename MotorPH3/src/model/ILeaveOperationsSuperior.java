package model;

/**
 * Interface for leave operations handled by a superior/manager.
 */
public interface ILeaveOperationsSuperior {
    // Updates the status of a specific leave request (e.g., "Approved", "Rejected")
    void updateLeaveStatus(LeaveRequest request, String newStatus);
}