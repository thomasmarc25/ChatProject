package com.chatsystem.admin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable model representing a spam report entry ({@code spam_reports}),
 * including reporter details, reported user details, report timestamp, reason,
 * synthetic direct-message snapshot, and resolution status.
 */
public class SpamReportRecord {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final long id;
    private final long reporterUserId;
    private final String reporterUsername;
    private final String reporterFullName;
    private final long reportedUserId;
    private final String reportedUsername;
    private final String reportedFullName;
    private final boolean reportedUserLocked;
    private final long directMessageId;
    private final LocalDateTime messageSentAt;
    private final String reason;
    private final String messageSnapshot;
    private final String status;
    private final LocalDateTime createdAt;
    private final String resolvedByUsername;
    private final LocalDateTime resolvedAt;

    public SpamReportRecord(
            long id,
            long reporterUserId,
            String reporterUsername,
            String reporterFullName,
            long reportedUserId,
            String reportedUsername,
            String reportedFullName,
            boolean reportedUserLocked,
            long directMessageId,
            LocalDateTime messageSentAt,
            String reason,
            String messageSnapshot,
            String status,
            LocalDateTime createdAt,
            String resolvedByUsername,
            LocalDateTime resolvedAt
    ) {
        this.id = id;
        this.reporterUserId = reporterUserId;
        this.reporterUsername = reporterUsername;
        this.reporterFullName = reporterFullName;
        this.reportedUserId = reportedUserId;
        this.reportedUsername = reportedUsername;
        this.reportedFullName = reportedFullName;
        this.reportedUserLocked = reportedUserLocked;
        this.directMessageId = directMessageId;
        this.messageSentAt = messageSentAt;
        this.reason = reason;
        this.messageSnapshot = messageSnapshot;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedByUsername = resolvedByUsername;
        this.resolvedAt = resolvedAt;
    }

    public long getId() {
        return id;
    }

    public long getReporterUserId() {
        return reporterUserId;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public String getReporterFullName() {
        return reporterFullName;
    }

    public String getReporterDisplay() {
        return reporterFullName + " (@" + reporterUsername + ")";
    }

    public long getReportedUserId() {
        return reportedUserId;
    }

    public String getReportedUsername() {
        return reportedUsername;
    }

    public String getReportedFullName() {
        return reportedFullName;
    }

    public String getReportedDisplay() {
        return reportedFullName + " (@" + reportedUsername + ")";
    }

    public boolean isReportedUserLocked() {
        return reportedUserLocked;
    }

    public long getDirectMessageId() {
        return directMessageId;
    }

    public LocalDateTime getMessageSentAt() {
        return messageSentAt;
    }

    public String getFormattedMessageSentAt() {
        return messageSentAt != null ? messageSentAt.format(DATE_TIME_FORMATTER) : "";
    }

    public String getReason() {
        return reason;
    }

    public String getMessageSnapshot() {
        return messageSnapshot;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public String getStatusDisplay() {
        return isPending() ? "Chờ xử lý (PENDING)" : "Đã xử lý (RESOLVED)";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    public String getResolvedByUsername() {
        return resolvedByUsername != null ? resolvedByUsername : "—";
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getFormattedResolvedAt() {
        return resolvedAt != null ? resolvedAt.format(DATE_TIME_FORMATTER) : "Chưa xử lý";
    }

    public SpamReportRecord withResolutionAndLockState(
            boolean locked,
            String newStatus,
            String resolvedBy,
            LocalDateTime resolvedTime
    ) {
        return new SpamReportRecord(
                this.id,
                this.reporterUserId,
                this.reporterUsername,
                this.reporterFullName,
                this.reportedUserId,
                this.reportedUsername,
                this.reportedFullName,
                locked,
                this.directMessageId,
                this.messageSentAt,
                this.reason,
                this.messageSnapshot,
                newStatus,
                this.createdAt,
                resolvedBy,
                resolvedTime
        );
    }
}
