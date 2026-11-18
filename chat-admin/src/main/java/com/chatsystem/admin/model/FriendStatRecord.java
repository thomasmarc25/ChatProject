package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing friend statistics for an active (non-deleted) user account
 * (Requirement 7 / Step G - C), aligned with {@code QUERY 8} in {@code script/03_admin_queries.sql}.
 */
public class FriendStatRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final long userId;
    private final String username;
    private final String fullName;
    private final LocalDateTime createdAt;
    private final boolean locked;
    private final int directFriendsCount;
    private final int distinctFriendsWithFofCount;
    private final String reachableUsernamesSummary;

    public FriendStatRecord(
            long userId,
            String username,
            String fullName,
            LocalDateTime createdAt,
            boolean locked,
            int directFriendsCount,
            int distinctFriendsWithFofCount,
            String reachableUsernamesSummary
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.locked = locked;
        this.directFriendsCount = directFriendsCount;
        this.distinctFriendsWithFofCount = distinctFriendsWithFofCount;
        this.reachableUsernamesSummary = reachableUsernamesSummary != null ? reachableUsernamesSummary : "";
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    public boolean isLocked() {
        return locked;
    }

    public String getAccountStatusLabel() {
        return locked ? User.ACCOUNT_LOCKED : User.ACCOUNT_ACTIVE;
    }

    public int getDirectFriendsCount() {
        return directFriendsCount;
    }

    public int getDistinctFriendsWithFofCount() {
        return distinctFriendsWithFofCount;
    }

    public int getSecondHopOnlyCount() {
        return Math.max(0, distinctFriendsWithFofCount - directFriendsCount);
    }

    public String getReachableUsernamesSummary() {
        return reachableUsernamesSummary;
    }
}
