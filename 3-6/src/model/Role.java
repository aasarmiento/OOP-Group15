package model;

public enum Role {
    HR_STAFF("HR Staff"),
    ADMIN("Admin"),
    ACCOUNTING("Accounting"),
    REGULAR_STAFF("Regular Staff"),
    IT_STAFF("IT Staff");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Role fromString(String text) {
        if (text == null || text.trim().isEmpty()) return REGULAR_STAFF;

        String normalized = text.trim().toLowerCase();

        switch (normalized) {
            case "admin":
            case "administrator":
                return ADMIN;

            case "hr":
            case "hr staff":
            case "human resources":
                return HR_STAFF;

            case "it":
            case "it staff":
            case "systems":
            case "operations":
                return IT_STAFF;

            case "accounting":
            case "finance":
            case "account":
                return ACCOUNTING;

            case "regular":
            case "regular staff":
            case "employee":
                return REGULAR_STAFF;

            default:
                for (Role r : Role.values()) {
                    if (r.name().equalsIgnoreCase(text.trim()) ||
                        r.getLabel().equalsIgnoreCase(text.trim())) {
                        return r;
                    }
                }
                return REGULAR_STAFF;
        }
    }
}