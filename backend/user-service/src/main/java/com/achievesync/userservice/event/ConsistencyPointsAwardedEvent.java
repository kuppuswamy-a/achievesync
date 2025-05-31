package com.achievesync.userservice.event;

import java.time.Instant;

public class ConsistencyPointsAwardedEvent {
    private final String userId;
    private final int pointsAwarded;
    private final int totalPoints;
    private final String reason;
    private final Instant awardedAt;

    public ConsistencyPointsAwardedEvent(String userId, int pointsAwarded, int totalPoints, String reason, Instant awardedAt) {
        this.userId = userId;
        this.pointsAwarded = pointsAwarded;
        this.totalPoints = totalPoints;
        this.reason = reason;
        this.awardedAt = awardedAt;
    }

    public String getUserId() { return userId; }
    public int getPointsAwarded() { return pointsAwarded; }
    public int getTotalPoints() { return totalPoints; }
    public String getReason() { return reason; }
    public Instant getAwardedAt() { return awardedAt; }
}