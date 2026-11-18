package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing a single authentication session record
 * in {@code login_history} for per-user and system-wide login history views.
 * Distinct from client application launch events ({@code app_open_events}).
 */
public class LoginRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final long id;
    private final long userId;
    private final String username;
    private final String fullName;
    private final LocalDateTime loginAt;
    private final LocalDateTime logoutAt;
    private final String ipAddress;
    private final String sessionNote;

    public LoginRecord(
            long id,
            long userId,
            String username,
            String fullName,
            LocalDateTime loginAt,
            LocalDateTime logoutAt,
            String ipAddress
    ) {
        this(id, userId, username, fullName, loginAt, logoutAt, ipAddress, "");
    }

    public LoginRecord(
            long id,
            long userId,
            String username,
            String fullName,
            LocalDateTime loginAt,
            LocalDateTime logoutAt,
            String ipAddress,
            String sessionNote
    ) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.loginAt = loginAt;
        this.logoutAt = logoutAt;
        this.ipAddress = ipAddress;
        this.sessionNote = sessionNote != null ? sessionNote : "";
    }

    public long getId() {
        return id;
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

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public String getFormattedLoginAt() {
        return loginAt != null ? loginAt.format(DATE_TIME_FORMATTER) : "";
    }

    public LocalDateTime getLogoutAt() {
        return logoutAt;
    }

    public String getFormattedLogoutAt() {
        return logoutAt != null ? logoutAt.format(DATE_TIME_FORMATTER) : "Đang kết nối";
    }

    public String getIpAddress() {
        return ipAddress != null ? ipAddress : "—";
    }

    public String getSessionNote() {
        return sessionNote;
    }
}
