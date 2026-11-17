package com.chatsystem.admin.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Typed domain model representing a user account in the ChatSystem Admin UI.
 * Keeps {@code online} and {@code locked} as independent boolean properties.
 */
public class User {

    public static final String STATUS_LOCKED = "Đã khóa";
    public static final String STATUS_ONLINE = "Đang hoạt động";
    public static final String STATUS_OFFLINE = "Ngoại tuyến";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final long id;
    private final String username;
    private final String fullName;
    private final String address;
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String email;
    private final LocalDate createdAt;
    private final boolean online;
    private final boolean locked;

    public User(
            long id,
            String username,
            String fullName,
            String address,
            LocalDate dateOfBirth,
            String gender,
            String email,
            LocalDate createdAt,
            boolean online,
            boolean locked
    ) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.createdAt = createdAt;
        this.online = online;
        this.locked = locked;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFormattedDateOfBirth() {
        return dateOfBirth != null ? dateOfBirth.format(DATE_FORMATTER) : "";
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_FORMATTER) : "";
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * Resolves display status according to AGENTS.md precedence:
     * locked -> "Đã khóa", else online -> "Đang hoạt động", else -> "Ngoại tuyến".
     */
    public String getDisplayStatus() {
        if (locked) {
            return STATUS_LOCKED;
        }
        if (online) {
            return STATUS_ONLINE;
        }
        return STATUS_OFFLINE;
    }
}
