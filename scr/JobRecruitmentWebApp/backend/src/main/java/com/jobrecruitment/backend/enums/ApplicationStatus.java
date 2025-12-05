package com.jobrecruitment.backend.enums;

public enum ApplicationStatus {
    PENDING("Đang chờ"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ApplicationStatus fromDisplayName(String displayName) {
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid application status: " + displayName);
    }
}