package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing a chat group row for read-only administrative inspection,
 * aligned with {@code chat_groups} and active {@code group_members} counts in {@code script/02_seed.sql}.
 */
public class ChatGroupRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final long id;
    private final String groupName;
    private final LocalDateTime createdAt;
    private final long creatorUserId;
    private final String creatorUsername;
    private final String creatorFullName;
    private final boolean encrypted;
    private final int currentMemberCount;
    private final int currentAdminCount;
    private final int departedMemberCount;

    public ChatGroupRecord(
            long id,
            String groupName,
            LocalDateTime createdAt,
            long creatorUserId,
            String creatorUsername,
            String creatorFullName,
            boolean encrypted,
            int currentMemberCount,
            int currentAdminCount,
            int departedMemberCount
    ) {
        this.id = id;
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.creatorUserId = creatorUserId;
        this.creatorUsername = creatorUsername;
        this.creatorFullName = creatorFullName;
        this.encrypted = encrypted;
        this.currentMemberCount = currentMemberCount;
        this.currentAdminCount = currentAdminCount;
        this.departedMemberCount = departedMemberCount;
    }

    public long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    public long getCreatorUserId() {
        return creatorUserId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public String getCreatorDisplay() {
        return creatorFullName + " (@" + creatorUsername + ")";
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public String getEncryptionLabel() {
        return encrypted ? "Đã bật mã hóa (E2EE)" : "Tiêu chuẩn (Plaintext)";
    }

    public int getCurrentMemberCount() {
        return currentMemberCount;
    }

    public int getCurrentAdminCount() {
        return currentAdminCount;
    }

    public int getDepartedMemberCount() {
        return departedMemberCount;
    }
}
