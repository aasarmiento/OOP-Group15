package model;


public enum Role {
    HR_STAFF("HR Staff"),
    ADMIN("Admin"),
    ACCOUNTING("Accounting"),
    REGULAR_STAFF("Regular Staff"),
    IT_STAFF("ITStaff"); 

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

   
    public static Role fromString(String text) {
        if (text == null) return REGULAR_STAFF;
        
        for (Role r : Role.values()) {
            if (r.label.equalsIgnoreCase(text.trim())) {
                return r;
            }
        }
       
        try {
            return Role.valueOf(text.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return REGULAR_STAFF; 
        }
    }
}