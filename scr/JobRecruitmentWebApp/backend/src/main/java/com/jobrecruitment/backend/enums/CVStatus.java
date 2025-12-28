package com.jobrecruitment.backend.enums;

/**
 * CVStatus - Enum đại diện cho trạng thái của CV
 * 
 * Mô tả:
 * - Trạng thái của CV trong hệ thống
 * - ACTIVE: CV đang được sử dụng và có thể ứng tuyển
 * - HIDDEN: CV đã bị ẩn tạm thời, ứng viên vẫn quản lý được
 * - DELETED: CV đã bị xóa vĩnh viễn, không hiển thị trong danh sách
 */

public enum CVStatus {
    ACTIVE("Đang hoạt động"),
    HIDDEN("Tạm ẩn"),
    DELETED("Đã xóa");

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
