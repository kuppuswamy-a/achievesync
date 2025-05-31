package com.achievesync.goalservice.event;

import java.time.Instant;

public class GoalCompletedEvent {
    private final String goalId;
    private final String userId;
    private final Instant completedAt;

    public GoalCompletedEvent(String goalId, String userId, Instant completedAt) {
        this.goalId = goalId;
        this.userId = userId;
        this.completedAt = completedAt;
    }

    public String getGoalId() { return goalId; }
    public String getUserId() { return userId; }
    public Instant getCompletedAt() { return completedAt; }
}