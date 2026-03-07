package model;


public enum Role {
   
 HR_STAFF("HR Staff"),
    ADMIN("Admin"),
    ACCOUNTING("Accounting"),
    REGULAR_STAFF("Regular Staff"),
    IT_STAFF("IT Staff");

    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }

    // Add this back to satisfy the CSVHandler error
    public static Role fromString(String text) {
    if (text == null || text.isEmpty()) return REGULAR_STAFF;

    for (Role r : Role.values()) {
        // Check if it matches "IT_STAFF" OR "IT Staff"
        if (r.name().equalsIgnoreCase(text.trim()) || 
            r.label.equalsIgnoreCase(text.trim())) {
            return r;
        }
    }
    return REGULAR_STAFF;
}
}
