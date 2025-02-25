package com.upsilon.TCMP.enums;

public enum Role {
    ROLE_STUDENT("Student"),
    ROLE_TUTOR("Tutor"),
    ROLE_ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleName() {
        return this.name();
    }

    public static Role fromDisplayName(String displayName) {
        for (Role role : Role.values()) {
            if (role.getDisplayName().equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found for display name: " + displayName);
    }
}
