package com.jobrecruitment.backend.enums;

public enum JobStatus {
    PENDING("Chờ xét duyệt"),
    WAIT("Chưa mở"),
    ACTIVE("Đang mở"),
    CLOSED("Đã đóng"),
    HIDDEN("Tạm ẩn");

    private String displayName;
    JobStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static JobStatus fromDisplayName(String displayName) {
        for (JobStatus status : JobStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid job status: " + displayName);
    }
}
