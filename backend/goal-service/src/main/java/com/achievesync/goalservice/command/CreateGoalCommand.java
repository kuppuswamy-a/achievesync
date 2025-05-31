package com.achievesync.goalservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDate;
import java.util.List;

public class CreateGoalCommand {
    @TargetAggregateIdentifier
    private final String goalId;
    private final String userId;
    private final String description;
    private final LocalDate targetDate;
    private final String category;
    private final List<String> tags;

    public CreateGoalCommand(String goalId, String userId, String description, LocalDate targetDate) {
        this(goalId, userId, description, targetDate, null, null);
    }

    public CreateGoalCommand(String goalId, String userId, String description, LocalDate targetDate, String category, List<String> tags) {
        this.goalId = goalId;
        this.userId = userId;
        this.description = description;
        this.targetDate = targetDate;
        this.category = category;
        this.tags = tags;
    }

    public String getGoalId() { return goalId; }
    public String getUserId() { return userId; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
}