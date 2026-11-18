package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing an account registration record for the
 * New Registrations screen (Requirement 5 / Step G - A) and Annual Registration Chart (Requirement 6).
 * Preserves historical registrations including soft-deleted accounts per Rule 8.
 */
public class RegistrationRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final long userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final LocalDateTime createdAt;
    private final String systemRole;
    private final boolean locked;
    private final boolean softDeleted;

    public RegistrationRecord(
            long userId,
            String username,
            String fullName,
            String email,
            LocalDateTime createdAt,
            String systemRole,
            boolean locked,
            boolean softDeleted
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.systemRole = systemRole;
        this.locked = locked;
        this.softDeleted = softDeleted;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    public String getSystemRole() {
        return systemRole;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public String getAccountLifecycleLabel() {
        if (softDeleted) {
            return "Đã xóa mềm (Lưu lịch sử)";
        }
        if (locked) {
            return User.ACCOUNT_LOCKED;
        }
        return User.ACCOUNT_ACTIVE;
    }
}
