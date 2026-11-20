package com.chatsystem.admin.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing an accepted direct friend entry for a user.
 */
public class UserFriendRecord {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final long friendUserId;
    private final String friendUsername;
    private final String friendFullName;
    private final String friendEmail;
    private final LocalDate friendshipCreatedAt;
    private final boolean friendLocked;
    private final boolean friendOnline;

    public UserFriendRecord(
            long friendUserId,
            String friendUsername,
            String friendFullName,
            String friendEmail,
            LocalDate friendshipCreatedAt,
            boolean friendLocked,
            boolean friendOnline
    ) {
        this.friendUserId = friendUserId;
        this.friendUsername = friendUsername;
        this.friendFullName = friendFullName;
        this.friendEmail = friendEmail;
        this.friendshipCreatedAt = friendshipCreatedAt;
        this.friendLocked = friendLocked;
        this.friendOnline = friendOnline;
    }

    public long getFriendUserId() {
        return friendUserId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public String getFriendFullName() {
        return friendFullName;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public LocalDate getFriendshipCreatedAt() {
        return friendshipCreatedAt;
    }

    public String getFormattedFriendshipDate() {
        return friendshipCreatedAt != null ? friendshipCreatedAt.format(DATE_FORMATTER) : "";
    }

    public boolean isFriendLocked() {
        return friendLocked;
    }

    public boolean isFriendOnline() {
        return friendOnline;
    }

    public String getAccountStatusLabel() {
        return friendLocked ? User.ACCOUNT_LOCKED : User.ACCOUNT_ACTIVE;
    }

    public String getPresenceStatusLabel() {
        return friendOnline ? User.PRESENCE_ONLINE : User.PRESENCE_OFFLINE;
    }
}
