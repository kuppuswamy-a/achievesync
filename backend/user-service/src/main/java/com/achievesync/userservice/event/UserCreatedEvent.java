package com.achievesync.userservice.event;

import java.time.Instant;

public class UserCreatedEvent {
    private final String userId;
    private final String name;
    private final String email;
    private final Instant createdAt;

    public UserCreatedEvent(String userId, String name, String email, Instant createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
}