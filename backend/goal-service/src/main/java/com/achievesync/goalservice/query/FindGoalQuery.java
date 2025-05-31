package com.achievesync.goalservice.query;

public class FindGoalQuery {
    private final String goalId;

    public FindGoalQuery(String goalId) {
        this.goalId = goalId;
    }

    public String getGoalId() { return goalId; }
}