package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing a user's activity metrics over a selected date range
 * (Requirement 8 / Step G - D), aligned with {@code QUERY 9A} & {@code QUERY 9B}
 * in {@code script/03_admin_queries.sql}.
 * Contains the 3 concrete metrics: app opens, distinct direct-message recipients,
 * and distinct groups messaged (no undefined aggregate total metric).
 */
public class UserActivityRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final long userId;
    private final String username;
    private final String fullName;
    private final LocalDateTime createdAt;
    private final boolean locked;
    private final boolean softDeleted;
    private final int appOpenCount;
    private final int distinctDirectRecipientsCount;
    private final int distinctGroupsMessagedCount;

    public UserActivityRecord(
            long userId,
            String username,
            String fullName,
            LocalDateTime createdAt,
            boolean locked,
            boolean softDeleted,
            int appOpenCount,
            int distinctDirectRecipientsCount,
            int distinctGroupsMessagedCount
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.locked = locked;
        this.softDeleted = softDeleted;
        this.appOpenCount = appOpenCount;
        this.distinctDirectRecipientsCount = distinctDirectRecipientsCount;
        this.distinctGroupsMessagedCount = distinctGroupsMessagedCount;
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

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public int getAppOpenCount() {
        return appOpenCount;
    }

    public int getDistinctDirectRecipientsCount() {
        return distinctDirectRecipientsCount;
    }

    public int getDistinctGroupsMessagedCount() {
        return distinctGroupsMessagedCount;
    }

    public boolean hasAnyActivity() {
        return appOpenCount > 0 || distinctDirectRecipientsCount > 0 || distinctGroupsMessagedCount > 0;
    }
}
