package com.achievesync.userservice.projection;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "consistency_points")
public class ConsistencyPointsProjection {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "total_points", nullable = false)
    private int totalPoints;
    
    @Column(name = "last_updated")
    private Instant lastUpdated;

    public ConsistencyPointsProjection() {}

    public ConsistencyPointsProjection(String userId, int totalPoints, Instant lastUpdated) {
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}