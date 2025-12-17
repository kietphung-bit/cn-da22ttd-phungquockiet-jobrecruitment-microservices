package com.jobrecruitment.backend.enums;

/** Trạng thái của công ty
 * Mô tả:
 * - PENDING: Chờ xét duyệt (mới đăng ký)
 * - ACTIVE: Đang hoạt động (được phép đăng tin tuyển dụng)
 * - BLOCKED: Bị khóa (không thể đăng tin)
 */

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