package com.achievesync.userservice.event;

import java.time.Instant;

public class UserProfileUpdatedEvent {
    private final String userId;
    private final String name;
    private final String email;
    private final Instant updatedAt;

    public UserProfileUpdatedEvent(String userId, String name, String email, Instant updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.updatedAt = updatedAt;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getUpdatedAt() { return updatedAt; }
}