package com.jobrecruitment.backend.enums;

public enum CompanyStatus {
    PENDING("Chờ xét duyệt"),
    ACTIVE("Đang hoạt động"),
    BLOCKED("Bị khóa");

    private final String displayName;

    CompanyStatus() {
        this.displayName = this.name();
    }
    
    CompanyStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public static CompanyStatus fromString(String status) {
        for (CompanyStatus cs : CompanyStatus.values()) {
            if (cs.name().equalsIgnoreCase(status)) {
                return cs;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + status);
    }
}