package ui;


import model.Employee;


public class AppController {

    private Employee currentUser;
    
    public void setCurrentUser(Employee user) {
        this.currentUser = user;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public void logout() {
    currentUser = null;  
}

}
