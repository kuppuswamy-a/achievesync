package com.achievesync.goalservice.event;

import java.time.Instant;

public class GoalProgressUpdatedEvent {
    private final String goalId;
    private final double progressPercentage;
    private final String notes;
    private final Instant updatedAt;

    public GoalProgressUpdatedEvent(String goalId, double progressPercentage, String notes, Instant updatedAt) {
        this.goalId = goalId;
        this.progressPercentage = progressPercentage;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public String getGoalId() { return goalId; }
    public double getProgressPercentage() { return progressPercentage; }
    public String getNotes() { return notes; }
    public Instant getUpdatedAt() { return updatedAt; }
}