package service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import model.Employee;
import model.LeaveRequest;

public class LeaveStuff {

    public boolean processLeaveRequest(Employee emp, String type, String start, String end) {
        
        if (type == null || type.isEmpty()) {
            System.err.println("Error: Leave type is required.");
            return false;
        }

        try {
            
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            
            LeaveRequest newRequest = new LeaveRequest(emp.getEmpNo(), type, startDate, endDate);

            
            emp.applyLeave(newRequest);

            System.out.println("System: Leave request processed for " + emp.getFirstName());
            return true;

        } catch (DateTimeParseException e) {
            
            System.err.println("Error: Invalid date format. Please use YYYY-MM-DD.");
            return false;
        }
    }
}