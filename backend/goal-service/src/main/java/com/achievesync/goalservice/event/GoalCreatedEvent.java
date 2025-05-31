package com.achievesync.goalservice.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class GoalCreatedEvent {
    private final String goalId;
    private final String userId;
    private final String description;
    private final LocalDate targetDate;
    private final Instant createdAt;
    private final String category;
    private final List<String> tags;

    public GoalCreatedEvent(String goalId, String userId, String description, LocalDate targetDate, Instant createdAt) {
        this(goalId, userId, description, targetDate, createdAt, null, null);
    }

    public GoalCreatedEvent(String goalId, String userId, String description, LocalDate targetDate, Instant createdAt, String category, List<String> tags) {
        this.goalId = goalId;
        this.userId = userId;
        this.description = description;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
        this.category = category;
        this.tags = tags;
    }

    public String getGoalId() { return goalId; }
    public String getUserId() { return userId; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
}