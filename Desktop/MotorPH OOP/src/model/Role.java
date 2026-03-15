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

    public static Role fromString(String text) {
        if (text == null || text.isEmpty()) return REGULAR_STAFF;
        
        String input = text.trim();
        String upper = input.toUpperCase();

        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(input) || r.label.equalsIgnoreCase(input)) {
                return r;
            }
        }

        if (upper.contains("CHIEF") || upper.contains("OFFICER")) {
            return ADMIN;
        }

        if (upper.contains("HR")) {
            return HR_STAFF;
        }
        
        if (upper.contains("IT") || upper.contains("SYSTEMS")) {
            return IT_STAFF;
        }

        if (upper.contains("ACCOUNTING") || upper.contains("PAYROLL")) {
            return ACCOUNTING;
        }

        return REGULAR_STAFF;
    }
}