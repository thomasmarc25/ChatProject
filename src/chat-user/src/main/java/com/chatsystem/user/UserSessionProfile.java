package com.chatsystem.user;

/**
 * Immutable session identity for the currently signed-in user or administrator.
 * Prevents hardcoded personal names across reused User Swing panels.
 */
public record UserSessionProfile(
        String username,
        String fullName,
        String email,
        String address,
        String dateOfBirth,
        String gender,
        boolean adminRole
) {
    public static final UserSessionProfile DEMO_USER = new UserSessionProfile(
            ChatData.CURRENT_USERNAME,
            ChatData.CURRENT_FULL_NAME,
            ChatData.CURRENT_EMAIL,
            ChatData.CURRENT_ADDRESS,
            ChatData.CURRENT_DOB,
            "Nam",
            false
    );

    public static final UserSessionProfile DEMO_ADMIN = new UserSessionProfile(
            "admin",
            "Quản Trị Hệ Thống",
            "admin@chatsystem.vn",
            "227 Nguyễn Văn Cừ, Quận 5, TP.HCM",
            "01/01/1995",
            "Nam",
            true
    );

    public String displayLabel() {
        return fullName + " (@" + username + ")";
    }
}
