/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Abstract Admin Class
 * Principle: Single Responsibility - Focuses on System and User Management.
 */
public abstract class Admin extends Employee implements IAdminOperations {

    // You don't need to write the logic here because the class is ABSTRACT.
    // By declaring them as abstract, you are "passing the buck" to the child class.

    @Override
    public abstract void createEmployee(Employee emp);

    @Override
    public abstract int generateNextEmpNo();

    @Override
    public abstract boolean isEmployeeValid(Employee emp);

    @Override
    public abstract void updateEmployee(String empNo);

    @Override
    public abstract void removeEmployee(String empNo);

    // IMPORTANT: You must also account for IUserOperations from the parent!
    // Since Admin is abstract, we just declare them abstract here too.
    @Override public abstract boolean login();
    @Override public abstract void logout();
}