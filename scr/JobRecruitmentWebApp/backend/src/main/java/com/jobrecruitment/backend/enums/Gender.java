package com.jobrecruitment.backend.enums;

/** 
 * Gender - Enum đại diện cho giới tính của ứng viên
 * Mô tả:
 * - MALE: Nam
 * - FEMALE: Nữ
 * - OTHER: Khác
 */

public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromDisplayName(String displayName) {
        for (Gender gender : Gender.values()) {
            if (gender.displayName.equals(displayName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + displayName);
    }
}
