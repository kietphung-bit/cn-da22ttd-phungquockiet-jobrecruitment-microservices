package com.jobrecruitment.backend.enums;

/** Trạng thái của đơn ứng tuyển 
 * Mô tả:
 * - PENDING: Đang chờ (Ứng viên đã nộp đơn, chờ nhà tuyển dụng duyệt)
 * - APPROVED: Đã duyệt (Nhà tuyển dụng đã chấp nhận đơn)
 * - REJECTED: Đã từ chối (Nhà tuyển dụng đã từ chối đơn)
*/

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