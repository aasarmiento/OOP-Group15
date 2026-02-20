package model;
import java.util.List;



public interface ILeaveOperations {
    // Shared behavior
    void applyLeave(LeaveRequest request);
    
    // Administrative 
    List<LeaveRequest> viewAllLeaveRequests(); 
    
   
}

