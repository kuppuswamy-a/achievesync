package com.achievesync.goalservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CompleteGoalCommand {
    @TargetAggregateIdentifier
    private final String goalId;

    public CompleteGoalCommand(String goalId) {
        this.goalId = goalId;
    }

    public String getGoalId() { return goalId; }
}