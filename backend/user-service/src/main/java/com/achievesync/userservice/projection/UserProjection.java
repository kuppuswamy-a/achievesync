package com.achievesync.userservice.projection;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserProjection {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    public UserProjection() {}

    public UserProjection(String userId, String name, String email, Instant createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}