package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing a user's membership within a chat group ({@code group_members}).
 * Keeps the group-level role ({@code group_members.role}: {@code ADMIN} vs {@code MEMBER})
 * explicitly separate from the system-level account role ({@code users.role}: {@code USER} vs {@code ADMIN}).
 */
public class GroupMemberRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final long groupId;
    private final long userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String groupRole;
    private final String systemRole;
    private final boolean accountLocked;
    private final boolean online;
    private final LocalDateTime joinedAt;
    private final LocalDateTime leftAt;

    public GroupMemberRecord(
            long groupId,
            long userId,
            String username,
            String fullName,
            String email,
            String groupRole,
            String systemRole,
            boolean accountLocked,
            boolean online,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        this.groupId = groupId;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.groupRole = groupRole;
        this.systemRole = systemRole;
        this.accountLocked = accountLocked;
        this.online = online;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public long getGroupId() {
        return groupId;
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

    public String getGroupRole() {
        return groupRole;
    }

    public boolean isGroupAdmin() {
        return "ADMIN".equalsIgnoreCase(groupRole);
    }

    public String getGroupRoleDisplay() {
        return isGroupAdmin() ? "Quản trị nhóm (ADMIN)" : "Thành viên (MEMBER)";
    }

    public String getSystemRole() {
        return systemRole;
    }

    public String getSystemRoleDisplay() {
        return "ADMIN".equalsIgnoreCase(systemRole) ? "Quản trị hệ thống (ADMIN)" : "Người dùng (USER)";
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public String getAccountStatusDisplay() {
        return accountLocked ? User.ACCOUNT_LOCKED : User.ACCOUNT_ACTIVE;
    }

    public boolean isOnline() {
        return online;
    }

    public String getPresenceDisplay() {
        return online ? User.PRESENCE_ONLINE : User.PRESENCE_OFFLINE;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public String getFormattedJoinedAt() {
        return joinedAt != null ? joinedAt.format(DATE_TIME_FORMATTER) : "";
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public boolean isCurrentMember() {
        return leftAt == null;
    }
}
