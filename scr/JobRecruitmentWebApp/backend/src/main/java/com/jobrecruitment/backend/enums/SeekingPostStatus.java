package com.jobrecruitment.backend.enums;

/**
 * SeekingPostStatus - Enum đại diện cho trạng thái của Tin tìm việc
 * 
 * Mô tả:
 * - Trạng thái của tin đăng tìm việc trong hệ thống
 * - ACTIVE: Tin đăng đang công khai, nhà tuyển dụng có thể xem
 * - HIDDEN: Tin đăng bị ẩn tạm thời bởi ứng viên
 * - CLOSED: Tin đăng đã đóng (ứng viên đã tìm được việc)
 * 
 * Tham khảo: Section 3.1 (Table 10) - SeekingPost Table
 */
public enum SeekingPostStatus {
    ACTIVE("Đang hoạt động"),
    HIDDEN("Tạm ẩn"),
    CLOSED("Đã đóng");

    private final String displayName;

    SeekingPostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SeekingPostStatus fromDisplayName(String displayName) {
        for (SeekingPostStatus status : SeekingPostStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid seeking post status: " + displayName);
    }
}
