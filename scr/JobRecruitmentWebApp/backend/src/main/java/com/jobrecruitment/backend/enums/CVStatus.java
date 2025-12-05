package com.jobrecruitment.backend.enums;

public enum CVStatus {
    ACTIVE("Đang hoạt động"),
    HIDDEN("Tạm ẩn");

    private final String displayName;

    CVStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CVStatus fromDisplayName(String displayName) {
        for (CVStatus status : CVStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CV status: " + displayName);
    }
}
