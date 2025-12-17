package com.jobrecruitment.backend.enums;

/**
 * JobStatus - Enum đại diện cho trạng thái của tin tuyển dụng
 * 
 * Mô tả:
 * - Trạng thái của tin tuyển dụng trong hệ thống
 * - PENDING: Chờ xét duyệt bởi Admin
 * - WAIT: Tin đã được duyệt nhưng chưa đến ngày mở
 * - ACTIVE: Tin đang mở và nhận đơn ứng tuyển
 * - CLOSED: Tin đã đóng, không nhận đơn nữa
 * - HIDDEN: Tin tạm ẩn, không hiển thị với ứng viên
 */

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
