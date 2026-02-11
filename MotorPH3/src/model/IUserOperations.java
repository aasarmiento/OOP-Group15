/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template


/**
 * @author abigail
 */

package model;

/**
 * @author abigail
 */
public interface IUserOperations {
    public boolean login();
    
    public Role getRole(); 
    
    public boolean isPasswordValid();
    public int getPasswordStrength();
    public void resetPassword();
    public void logout();
} // Make sure this closing brace exists!