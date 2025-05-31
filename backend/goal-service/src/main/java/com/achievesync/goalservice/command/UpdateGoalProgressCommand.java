package com.achievesync.goalservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class UpdateGoalProgressCommand {
    @TargetAggregateIdentifier
    private final String goalId;
    private final double progressPercentage;
    private final String notes;

    public UpdateGoalProgressCommand(String goalId, double progressPercentage, String notes) {
        this.goalId = goalId;
        this.progressPercentage = progressPercentage;
        this.notes = notes;
    }

    public String getGoalId() { return goalId; }
    public double getProgressPercentage() { return progressPercentage; }
    public String getNotes() { return notes; }
}